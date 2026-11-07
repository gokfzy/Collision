package io.github.gokfzy.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gokfzy.core.util.block.enums.Type;
import javafx.scene.paint.Color;

import java.util.Locale;
import java.util.Map;

public class Constants {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String APP_NAME = "Collision";
    public static final String BASE_NAME = "i18n.Messages";
    public static final Map<String, Locale> LANGUAGE_AND_LOCALE = Map.of("English", Locale.US, "简体中文", Locale.SIMPLIFIED_CHINESE);
    public static final Map<Type, Color> TYPE_AND_COLOR = Map.of(Type.MOVING_BLOCK, Color.web("#009DFF"), Type.COLLISION, Color.web("#009DFF"), Type.FACTORY, Color.web("#8CFF00"), Type.PROCESSING, Color.web("#8CFF00"));
}
