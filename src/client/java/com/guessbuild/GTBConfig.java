package com.guessbuild;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GTBConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = net.fabricmc.loader.api.FabricLoader.getInstance()
        .getConfigDir().resolve("guessbuild.json");

    private static GTBConfig INSTANCE = new GTBConfig();

    public boolean instantSend = true;
    public boolean pasteInChat = false;
    public boolean autoSend = true;
    public int autoSendDelay = 40;
    public boolean soundEnabled = true;
    public boolean overlayAnimations = true;
    public float overlayOpacity = 1.0f;
    public boolean showHints = true;
    public boolean legacyOverlay = false;

    public static GTBConfig get() {
        return INSTANCE;
    }

    public void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, GTBConfig.class);
                System.out.println("[GTB] Config loaded from " + CONFIG_PATH);
            } else {
                save();
                System.out.println("[GTB] Config created at " + CONFIG_PATH);
            }
        } catch (Exception e) {
            System.err.println("[GTB] Failed to load config: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (Exception e) {
            System.err.println("[GTB] Failed to save config: " + e.getMessage());
        }
    }
}
