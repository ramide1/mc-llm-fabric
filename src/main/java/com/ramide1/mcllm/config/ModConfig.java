package com.ramide1.mcllm.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String instructions = "You are a helpful assistant in Minecraft. Respond concisely and friendly.";
    private String apiKey = "";
    private String baseUrl = "";
    private String model = "gpt-4o-mini";
    private int maxTokens = 800;

    private transient File configFile;
    private transient Logger logger;

    public void load(File configFolder, Logger logger) {
        this.configFile = new File(configFolder, "mcllm.json");
        this.logger = logger;

        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                this.instructions = loaded.instructions;
                this.apiKey = loaded.apiKey;
                this.baseUrl = loaded.baseUrl;
                this.model = loaded.model;
                this.maxTokens = loaded.maxTokens;
                logger.info("Configuration loaded successfully.");
            } catch (IOException e) {
                logger.error("Failed to load configuration", e);
            }
        } else {
            save();
            logger.info("Default configuration created.");
        }
    }

    public void save() {
        if (configFile == null) return;
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            if (logger != null) {
                logger.error("Failed to save configuration", e);
            }
        }
    }

    public void reload() {
        if (configFile != null && logger != null) {
            load(configFile.getParentFile(), logger);
        }
    }

    public String getInstructions() { return instructions; }
    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public int getMaxTokens() { return maxTokens; }
}
