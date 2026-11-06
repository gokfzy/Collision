package io.github.gokfzy.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gokfzy.core.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Translation {
    private static final Map<String, Map<String, String>> translations = new HashMap<>(
            Map.of("en_us", new HashMap<>(),
                    "zh_cn", new HashMap<>()));
    static {
        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<String, Map<String, String>> entry : translations.entrySet()) {
            String languageName = entry.getKey();
            try (InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("language/" + languageName + ".json")) {
                Map<String, String> map = mapper.readValue(in, new TypeReference<>() {});
                entry.setValue(map);
            } catch (IOException e) {
                throw new RuntimeException("File not found", e);
            }
        }
    }

    public static String translationString(String language, String text) {
        return translations.get(language).getOrDefault(text, text);
    }

    public static String translationString(String text) {
        return translationString((String) Config.config.get("language"), text);
    }
}
