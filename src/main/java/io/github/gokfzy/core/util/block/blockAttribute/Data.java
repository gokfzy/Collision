package io.github.gokfzy.core.util.block.blockAttribute;

import io.github.gokfzy.core.util.block.enums.Direction;
import io.github.gokfzy.core.util.block.enums.Type;

import java.util.*;

public record Data(Type type, Set<Direction> directionSet) {
    public Data {
        directionSet = directionSet == null || directionSet.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(EnumSet.copyOf(directionSet));
        if (directionSet.size() > 4) {
            throw new IllegalArgumentException("directionSet error:" + directionSet);
        }
    }

    public static Data of(Type type, Set<Direction> directionList) {
        return new Data(type, directionList);
    }

    public static Data empty() {
        return Data.of(Type.NONE, Set.of());
    }
}