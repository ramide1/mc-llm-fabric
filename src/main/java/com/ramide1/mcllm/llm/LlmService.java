package com.ramide1.mcllm.llm;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import com.ramide1.mcllm.config.ModConfig;
import com.ramide1.mcllm.database.DatabaseManager;
import org.slf4j.Logger;

import java.util.List;

public class LlmService {
    private final DatabaseManager dbManager;
    private final ModConfig config;
    private final Logger logger;

    public LlmService(DatabaseManager dbManager, ModConfig config, Logger logger) {
        this.dbManager = dbManager;
        this.config = config;
        this.logger = logger;
    }

    public String sendRequest(String senderName, String question) {
        try {
            OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder()
                    .apiKey(config.getApiKey());

            if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
                builder.baseUrl(config.getBaseUrl());
            }

            OpenAIClient client = builder.build();

            List<DatabaseManager.ChatMessage> history = dbManager.getHistory(senderName);

            StringBuilder inputBuilder = new StringBuilder();
            if (!config.getInstructions().isEmpty()) {
                inputBuilder.append("System: ").append(config.getInstructions()).append("\n");
            }
            for (DatabaseManager.ChatMessage msg : history) {
                inputBuilder.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
            inputBuilder.append("user: ").append(question);

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .input(inputBuilder.toString())
                    .model(config.getModel())
                    .maxOutputTokens(config.getMaxTokens())
                    .build();

            Response response = client.responses().create(params);
            String content = response.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(c -> c.outputText().stream())
                    .findFirst()
                    .map(ResponseOutputText::text)
                    .orElse("");

            dbManager.saveMessage(senderName, "user", question);
            dbManager.saveMessage(senderName, "assistant", content);

            return content;
        } catch (Exception e) {
            logger.error("Error calling OpenAI API", e);
            return "Error: " + e.getMessage();
        }
    }
}
