package io.github.gokfzy.core.util;

import io.github.gokfzy.core.util.block.blockAttribute.Data;
import io.github.gokfzy.core.util.block.enums.Direction;
import io.github.gokfzy.core.util.block.enums.Type;

import java.util.List;

public class util {

    public static Direction getRotation90(Direction dir) {
        return switch (dir) {
            case UP -> Direction.RIGHT;
            case RIGHT -> Direction.DOWN;
            case DOWN -> Direction.LEFT;
            case LEFT -> Direction.UP;
            case NONE -> Direction.NONE;
        };
    }

    public static Direction getRotation180(Direction dir) {
        return switch (dir) {
            case UP -> Direction.DOWN;
            case RIGHT -> Direction.LEFT;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case NONE -> Direction.NONE;
        };
    }

    public static Data nextData(Data data) {
        Data[] dataList = {
                Data.of(Type.NONE, List.of()),
                Data.of(Type.MOVING_BLOCK, List.of(Direction.UP)),
                Data.of(Type.MOVING_BLOCK, List.of(Direction.RIGHT)),
                Data.of(Type.MOVING_BLOCK, List.of(Direction.DOWN)),
                Data.of(Type.MOVING_BLOCK, List.of(Direction.LEFT)),
                Data.of(Type.COLLISION, List.of(Direction.UP, Direction.DOWN)),
                Data.of(Type.COLLISION, List.of(Direction.LEFT, Direction.RIGHT)),
                Data.of(Type.FACTORY, List.of()),
                Data.of(Type.PROCESSING, List.of(Direction.UP, Direction.DOWN)),
                Data.of(Type.PROCESSING, List.of(Direction.LEFT, Direction.RIGHT)),
        };
        if (data.equals(dataList[dataList.length - 1])) {
            return dataList[0];
        }
        int index = -1;
        for (int i = 0; i < dataList.length; i++) {
            if (dataList[i].equals(data)) {
                index = i;
            }
        }
        if (index == -1) {
            return dataList[0];
        }
        return dataList[index + 1];
    }
}
