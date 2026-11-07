package io.github.gokfzy.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gokfzy.core.util.block.enums.Type;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.stream.Collectors;

public class Constants {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String APP_NAME = "Collision";
    public static final String BASE_NAME = "i18n.Messages";
    public static final Map<String, String> LANGUAGE_AND_TAG = Map.of("English", "en-US", "简体中文", "zh-CN");
    public static final Map<String, String> TAG_AND_LANGUAGE = LANGUAGE_AND_TAG.entrySet().stream()
        .collect(Collectors.toMap(
                Map.Entry::getValue,
                Map.Entry::getKey
        ));
    public static final Map<Type, Color> TYPE_AND_COLOR = Map.of(Type.MOVING_BLOCK, Color.web("#009DFF"), Type.COLLISION, Color.web("#009DFF"), Type.FACTORY, Color.web("#8CFF00"), Type.PROCESSING, Color.web("#8CFF00"));
}
