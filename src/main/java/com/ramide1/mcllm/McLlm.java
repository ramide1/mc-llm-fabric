package com.ramide1.mcllm;

import com.ramide1.mcllm.command.LlmCommand;
import com.ramide1.mcllm.command.ReloadCommand;
import com.ramide1.mcllm.config.ModConfig;
import com.ramide1.mcllm.database.DatabaseManager;
import com.ramide1.mcllm.llm.LlmService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class McLlm implements ModInitializer {
    public static final String MOD_ID = "mcllm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private ModConfig config;
    private DatabaseManager dbManager;
    private LlmService llmService;

    @Override
    public void onInitialize() {
        LOGGER.info("Minecraft LLM is initializing...");

        File dataFolder = new File("config/mcllm");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        config = new ModConfig();
        config.load(dataFolder, LOGGER);

        dbManager = new DatabaseManager(dataFolder, LOGGER);
        llmService = new LlmService(dbManager, config, LOGGER);

        LlmCommand llmCommand = new LlmCommand(llmService);
        ReloadCommand reloadCommand = new ReloadCommand(config);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            llmCommand.register(dispatcher);
            reloadCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (dbManager != null) {
                dbManager.close();
            }
        });

        LOGGER.info("Minecraft LLM has been enabled!");
    }
}
