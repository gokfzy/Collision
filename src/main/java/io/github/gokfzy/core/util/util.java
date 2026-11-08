package io.github.gokfzy.core.util;

import io.github.gokfzy.core.Config;
import io.github.gokfzy.core.util.block.blockAttribute.Data;
import io.github.gokfzy.core.util.block.enums.Direction;
import io.github.gokfzy.core.util.block.enums.Type;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static io.github.gokfzy.core.util.Constants.BASE_NAME;

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
                Data.of(Type.NONE, Set.of()),
                Data.of(Type.MOVING_BLOCK, Set.of(Direction.UP)),
                Data.of(Type.FACTORY, Set.of()),
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

    public static String translationString(String string) {
        return ResourceBundle.getBundle(BASE_NAME, Locale.forLanguageTag((String) Config.config.get("language")))
                .getString(string);
    }
}