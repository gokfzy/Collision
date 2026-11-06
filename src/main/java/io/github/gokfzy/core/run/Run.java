package io.github.gokfzy.core.run;

import io.github.gokfzy.core.Config;
import io.github.gokfzy.core.util.BigIntegerPack;
import io.github.gokfzy.core.util.block.blockAttribute.Data;
import io.github.gokfzy.core.util.block.blockAttribute.Pos;
import io.github.gokfzy.core.util.block.enums.Direction;
import io.github.gokfzy.core.util.block.enums.Type;

import java.math.BigInteger;
import java.util.*;

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

        final BigIntegerPack leftBorder = new BigIntegerPack(null, true);
        final BigIntegerPack rightBorder = new BigIntegerPack(null, false);
        final BigIntegerPack bottomBorder = new BigIntegerPack(null, true);
        final BigIntegerPack topBorder = new BigIntegerPack(null, false);

        BigInteger steps = BigInteger.valueOf(((Number) Config.config.get("delete_distance")).longValue());

        snapshot.forEach( (pos, data) -> {
            if (data.type() == Type.FACTORY) {
                if (leftBorder.compareTo(pos.x().subtract(steps)) > 0) {
                    leftBorder.setInteger(pos.x().subtract(steps));
                }
                if (rightBorder.compareTo(pos.x().add(steps)) < 0) {
                    rightBorder.setInteger(pos.x().add(steps));
                }
                if (bottomBorder.compareTo(pos.y().subtract(steps)) > 0) {
                    bottomBorder.setInteger(pos.y().subtract(steps));
                }
                if (topBorder.compareTo(pos.y().add(steps)) < 0) {
                    topBorder.setInteger(pos.y().add(steps));
                }
            }
        });

        for (Map.Entry<Pos, Data> entry : snapshot.entrySet()) {
            Pos pos = entry.getKey();
            Data data = entry.getValue();

            switch (data.type()) {
                case MOVING_BLOCK -> {
                    if (leftBorder.compareTo(pos.x()) + rightBorder.compareTo(pos.x()) != 0 ||
                            topBorder.compareTo(pos.y()) + bottomBorder.compareTo(pos.y()) != 0) {
                        continue;
                    }
                    Direction dir = data.directionList().getFirst();
                    arrivals.computeIfAbsent(move(pos, dir, new BigInteger("1")), k -> new ArrayList<>())
                            .add(data);
                }
                case COLLISION -> {
                    for (Direction dir : data.directionList()) {
                        arrivals.computeIfAbsent(move(pos, dir, new BigInteger("1")), k -> new ArrayList<>())
                                .add(Data.of(Type.MOVING_BLOCK, List.of(dir)));
                    }
                }
                case PROCESSING -> {
                    for (Direction dir : data.directionList()) {
                        arrivals.computeIfAbsent(move(pos, dir, new BigInteger("1")), k -> new ArrayList<>())
                                .add(Data.of(Type.MOVING_BLOCK, List.of(dir)));
                    }
                    baseMap.put(pos, Data.of(Type.FACTORY, List.of()));
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
            List<Direction> rotated = new ArrayList<>(count);
            List<Direction> directionList = incoming.stream()
                    .map(Data::directionList)
                    .map(List::getFirst)
                    .toList();
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
    }

    private void resolveOnFactory(Pos pos, List<Data> incoming, Map<Pos, Data> next) {
        if (incoming.size() % 2 == 1) {
            List<Direction> result = new ArrayList<>(incoming.size() * 2);
            List<Direction> directionList = incoming.stream()
                    .map(Data::directionList)
                    .map(List::getFirst)
                    .toList();
            for (Direction dir : directionList) {
                if (directionList.contains(getRotation180(dir))) {
                    continue;
                }
                result.add(getRotation90(getRotation180(dir)));
                result.add(getRotation90(dir));
            }
            next.put(pos, Data.of(Type.PROCESSING, result));
        }
    }
}