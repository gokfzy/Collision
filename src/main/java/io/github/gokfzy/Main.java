package io.github.gokfzy;

import io.github.gokfzy.core.run.Memory;
import io.github.gokfzy.core.run.Run;
import io.github.gokfzy.gui.Gui;
import javafx.application.Application;

import java.io.IOException;

import static io.github.gokfzy.core.Config.loadConfig;

public class Main {
    public static final Memory memory = new Memory();
    public static final Run run = new Run(memory);
    public static boolean enableAutoUpdate = false;
    public static void main(String[] args) {
        try { loadConfig(); } catch (IOException e) { throw new RuntimeException(e); }
        Application.launch(Gui.class, args);
    }
}