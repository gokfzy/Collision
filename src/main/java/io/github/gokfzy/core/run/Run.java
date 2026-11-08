package io.github.gokfzy.core.run;

import io.github.gokfzy.core.util.block.blockAttribute.Data;
import io.github.gokfzy.core.util.block.blockAttribute.Pos;
import io.github.gokfzy.core.util.block.enums.Direction;
import io.github.gokfzy.core.util.block.enums.Type;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.gokfzy.core.util.util.getRotation180;
import static io.github.gokfzy.core.util.util.getRotation90;

public class Run {

    Memory memory;

    public Run(Memory memory) {
        this.memory = memory;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void update() {
        Map<Pos, Data> snapshot = memory.getBlockMap();
        Map<Pos, Data> baseMap = new HashMap<>(snapshot.size());
        Map<Pos, List<Data>> arrivals = new HashMap<>();

        for (Map.Entry<Pos, Data> entry : snapshot.entrySet()) {
            Pos pos = entry.getKey();
            Data data = entry.getValue();

            switch (data.type()) {
                case MOVING_BLOCK -> {
                    Direction dir = data.directionSet().iterator().next();
                    arrivals.computeIfAbsent(move(pos, dir, new BigInteger("1")), _ -> new ArrayList<>())
                            .add(data);
                }
                case COLLISION -> {
                    for (Direction dir : data.directionSet()) {
                        arrivals.computeIfAbsent(move(pos, dir, new BigInteger("1")), _ -> new ArrayList<>())
                                .add(Data.of(Type.MOVING_BLOCK, Set.of(dir)));
                    }
                }
                case PROCESSING -> {
                    for (Direction dir : data.directionSet()) {
                        arrivals.computeIfAbsent(move(pos, dir, new BigInteger("1")), _ -> new ArrayList<>())
                                .add(Data.of(Type.MOVING_BLOCK, Set.of(dir)));
                    }
                    baseMap.put(pos, Data.of(Type.FACTORY, Set.of()));
                }
                default -> {
                    if (data.type() != Type.NONE) {
                        baseMap.put(pos, data);
                    }
                }
            }
        }

        Map<Pos, Data> next = new HashMap<>(baseMap);

        for (Map.Entry<Pos, List<Data>> entry : arrivals.entrySet()) {
            Pos pos = entry.getKey();
            List<Data> incoming = entry.getValue();
            Data occupant = baseMap.get(pos);

            if (occupant != null && occupant.type() == Type.FACTORY) {
                resolveOnFactory(pos, incoming, next);
            } else {
                resolveOnEmpty(pos, incoming, next);
            }
        }

        memory.setBlockMap(next);
    }

    private static Pos move(Pos pos, Direction dir, BigInteger distance) {
        return switch (dir) {
            case RIGHT -> pos.addX(distance);
            case LEFT  -> pos.addX(new BigInteger("0").subtract(distance));
            case UP    -> pos.addY(distance);
            case DOWN  -> pos.addY(new BigInteger("0").subtract(distance));
            case NONE  -> pos;
        };
    }

    private void resolveOnEmpty(Pos pos, List<Data> incoming, Map<Pos, Data> next) {
        int count = incoming.size();
        if (count == 1) {
            next.put(pos, incoming.getFirst());
        } else if (count % 2 == 0) {
            Set<Direction> rotated = EnumSet.noneOf(Direction.class);
            Set<Direction> directionList = incoming.stream()
                    .map(Data::directionSet)
                    .map(Set::iterator)
                    .map(Iterator::next)
                    .collect(Collectors.toUnmodifiableSet());
            for (Direction dir : directionList) {
                if (rotated.remove(getRotation180(dir))) {
                    rotated.add(getRotation90(getRotation180(dir)));
                    rotated.add(getRotation90(dir));
                } else {
                    rotated.add(dir);
                }
            }
            next.put(pos, Data.of(Type.COLLISION, rotated));
        }
        // ignore
    }

    private void resolveOnFactory(Pos pos, List<Data> incoming, Map<Pos, Data> next) {
        if (incoming.size() % 2 == 1) {
            Set<Direction> result = EnumSet.noneOf(Direction.class);
            Set<Direction> directionList = incoming.stream()
                    .map(Data::directionSet)
                    .map(Set::iterator)
                    .map(Iterator::next)
                    .collect(Collectors.toUnmodifiableSet());
            for (Direction dir : directionList) {
                if (directionList.contains(getRotation180(dir))) {
                    continue;
                }
                result.add(getRotation90(getRotation180(dir)));
                result.add(getRotation90(dir));
            }
            next.put(pos, Data.of(Type.PROCESSING, result));
        }
        // ignore
    }
}