package io.github.gokfzy.core.util.block.blockAttribute;

import io.github.gokfzy.core.util.block.enums.Direction;
import io.github.gokfzy.core.util.block.enums.Type;

import java.util.List;

public record Data(Type type, List<Direction> directionList) {
    public Data {
        directionList = directionList == null
                ? List.of()
                : List.copyOf(directionList);
    }

    public static Data of(Type type, List<Direction> directionList) {
        return new Data(type, directionList);
    }

    public static Data empty() {
        return Data.of(Type.NONE, List.of());
    }
}