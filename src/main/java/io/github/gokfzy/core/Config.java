package io.github.gokfzy.core;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static io.github.gokfzy.core.util.Constants.*;

public class Config {
    public static Map<String, Object> config = new HashMap<>();
    public static Map<String, Object> defaultConfig;
    private static final Path CONFIG_PATH = resolveWritablePath();
    static {
        defaultConfig = Map.ofEntries(
                Map.entry("delete_distance", 1),
                Map.entry("updated_step", 1),
                Map.entry("update_interval", 1000),
                Map.entry("enable_traceback", true),
                Map.entry("language", "English")
        );
        config.putAll(defaultConfig);
    }

    private static Path resolveWritablePath() {
        String dir = System.getProperty("app.config.dir");
        if (dir != null && !dir.isBlank()) {
            Path candidate = Path.of(dir, "config.json");
            if (canWrite(candidate)) {
                return candidate;
            }
        }

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null) {
            return Path.of(localAppData, APP_NAME, "config.json");
        }
        String xdg = System.getenv("XDG_CONFIG_HOME");
        if (xdg != null) {
            return Path.of(xdg, APP_NAME, "config.json");
        }
        return Path.of(System.getProperty("user.home"), ".config", APP_NAME, "config.json");
    }

    private static boolean canWrite(Path file) {
        try {
            Path dir = file.getParent();
            if (dir != null) Files.createDirectories(dir);
            return dir != null && Files.isWritable(dir)
                    && (!Files.exists(file) || Files.isWritable(file));
        } catch (IOException e) {
            return false;
        }
    }

    public static void loadConfig() throws IOException {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                config.putAll(MAPPER.readValue(reader, new TypeReference<Map<String, Object>>() {}));
                if (!checkConfig()) {
                    config.clear();
                    config.putAll(defaultConfig);
                }
            }
        }
    }

    private static boolean checkConfig() {
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null) { return false; }
            if (defaultConfig.containsKey(key)) {
                if (value.getClass() != defaultConfig.get(key).getClass()) { return false; }
            } else { return false; }
            if (!checkData(key, value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkData(String key, Object value) {
        switch (key) {
            case "updated_step", "update_interval" -> { if ((Integer) value < 1) { return false; } }
            case "language" ->{ if (!LANGUAGE_AND_LOCALE.containsKey((String) value)) { return false; } }
        }
        return true;
    }

    public static void saveConfig() throws IOException {
        Path dir = CONFIG_PATH.getParent();
        if (dir == null) {
            throw new IOException("CONFIG_PATH has no parent: " + CONFIG_PATH);
        }
        Files.createDirectories(dir);

        Path tmp = Files.createTempFile(dir, "config", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, config);
            }
            try {
                Files.move(tmp, CONFIG_PATH,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}