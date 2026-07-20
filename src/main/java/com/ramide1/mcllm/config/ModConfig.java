package com.ramide1.mcllm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class ModConfig {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final Set<String> VALID_REASONING_EFFORTS = Set.of("", "none", "low", "medium", "high");

    private volatile String instructions = "You are a helpful assistant in Minecraft. Respond concisely and friendly.";
    private volatile String apiKey = "";
    private volatile String baseUrl = "";
    private volatile String model = "gpt-4o-mini";
    private volatile int maxTokens = 800;
    private volatile int maxHistoryMessages = 50;
    private volatile boolean hideReasoning = false;
    private volatile String reasoningEffort = "";

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
                ModConfig loaded = OBJECT_MAPPER.readValue(reader, ModConfig.class);
                this.instructions = loaded.instructions;
                this.apiKey = loaded.apiKey;
                this.baseUrl = loaded.baseUrl;
                this.model = loaded.model;
                this.maxTokens = clamp(loaded.maxTokens, 1, 128000);
                this.maxHistoryMessages = clamp(loaded.maxHistoryMessages, 0, 500);
                this.hideReasoning = loaded.hideReasoning;
                this.reasoningEffort = VALID_REASONING_EFFORTS.contains(loaded.reasoningEffort)
                        ? loaded.reasoningEffort
                        : "";
                if (!this.reasoningEffort.equals(loaded.reasoningEffort != null ? loaded.reasoningEffort : "")) {
                    logger.warn("Invalid reasoningEffort '{}' reset to '{}'", loaded.reasoningEffort,
                            this.reasoningEffort);
                }
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
        if (configFile == null)
            return;
        try (FileWriter writer = new FileWriter(configFile)) {
            OBJECT_MAPPER.writeValue(writer, this);
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public String getInstructions() {
        return instructions;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public int getMaxHistoryMessages() {
        return maxHistoryMessages;
    }

    public boolean getHideReasoning() {
        return hideReasoning;
    }

    public String getReasoningEffort() {
        return reasoningEffort;
    }
}