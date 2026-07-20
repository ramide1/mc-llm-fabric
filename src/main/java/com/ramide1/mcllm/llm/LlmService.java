package com.ramide1.mcllm.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ramide1.mcllm.config.ModConfig;
import com.ramide1.mcllm.database.DatabaseManager;
import org.slf4j.Logger;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class LlmService {
    private final DatabaseManager dbManager;
    private final ModConfig config;
    private final Logger logger;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public LlmService(DatabaseManager dbManager, ModConfig config, Logger logger) {
        this.dbManager = dbManager;
        this.config = config;
        this.logger = logger;
    }

    public String sendRequest(String senderName, String question) {
        try {
            List<DatabaseManager.ChatMessage> history = dbManager.getHistory(senderName);

            String baseUrl = config.getBaseUrl();
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "https://api.openai.com/v1/chat/completions";
            } else {
                baseUrl = baseUrl.replaceAll("/+$", "");
                if (!baseUrl.endsWith("/chat/completions")) {
                    baseUrl += "/chat/completions";
                }
            }

            ObjectNode requestBody = OBJECT_MAPPER.createObjectNode();
            requestBody.put("model", config.getModel());

            String reasoningEffort = config.getReasoningEffort();
            boolean hasReasoning = reasoningEffort != null && !reasoningEffort.isEmpty();

            if (hasReasoning) {
                requestBody.put("max_completion_tokens", config.getMaxTokens());
                requestBody.put("reasoning_effort", reasoningEffort);
            } else {
                requestBody.put("max_tokens", config.getMaxTokens());
            }

            var messages = OBJECT_MAPPER.createArrayNode();
            if (!config.getInstructions().isEmpty()) {
                var systemMsg = OBJECT_MAPPER.createObjectNode();
                systemMsg.put("role", "system");
                systemMsg.put("content", config.getInstructions());
                messages.add(systemMsg);
            }
            for (DatabaseManager.ChatMessage msg : history) {
                var message = OBJECT_MAPPER.createObjectNode();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }
            var userMsg = OBJECT_MAPPER.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", question);
            messages.add(userMsg);
            requestBody.set("messages", messages);

            String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("OpenAI API returned status {}: {}", response.statusCode(), response.body());
                return "Error: API returned status " + response.statusCode();
            }

            JsonNode responseJson = OBJECT_MAPPER.readTree(response.body());
            JsonNode choices = responseJson.get("choices");
            if (choices == null || choices.isEmpty()) {
                logger.error("No choices in response: {}", response.body());
                return "Error: No choices in response";
            }

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message == null) {
                logger.error("No message in choice: {}", response.body());
                return "Error: Invalid API response format";
            }
            JsonNode contentNode = message.get("content");

            String text;
            if (contentNode != null && contentNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode block : contentNode) {
                    String type = block.has("type") ? block.get("type").asText() : "";
                    if (type.equals("reasoning")) {
                        if (!config.getHideReasoning()) {
                            String reasoning = block.has("reasoning") ? block.get("reasoning").asText() : "";
                            if (!reasoning.isEmpty()) {
                                sb.append(reasoning).append("\n\n");
                            }
                        }
                    } else if (type.equals("text")) {
                        String t = block.has("text") ? block.get("text").asText() : "";
                        sb.append(t);
                    }
                }
                text = sb.toString().trim();
            } else if (contentNode != null) {
                text = contentNode.asText();
            } else {
                text = "";
            }

            if (!text.isEmpty()) {
                dbManager.saveMessage(senderName, "user", question);
                dbManager.saveMessage(senderName, "assistant", text);
            }

            return text.isEmpty() ? "No response from model." : text;
        } catch (InterruptedException e) {
            logger.error("Error calling OpenAI API", e);
            Thread.currentThread().interrupt();
            return "Error: Could not reach AI service.";
        } catch (IOException e) {
            logger.error("Error calling OpenAI API", e);
            return "Error: Could not reach AI service.";
        } catch (Exception e) {
            logger.error("Error processing OpenAI API response", e);
            return "Error: Failed to process AI response.";
        }
    }
}