package com.ramide1.mcllm.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.ramide1.mcllm.llm.LlmEvent;
import com.ramide1.mcllm.llm.LlmService;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LlmCommand {
    private static final Permission MCLLM_USE = Permission.Atom.create(
            Identifier.fromNamespaceAndPath("mcllm", "use"));

    private final LlmService llmService;

    public LlmCommand(LlmService llmService) {
        this.llmService = llmService;
    }

    public void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("llm")
                        .requires(source -> source.permissions().hasPermission(MCLLM_USE) ||
                                source.permissions()
                                        .hasPermission(new Permission.HasCommandLevel(
                                                net.minecraft.server.permissions.PermissionLevel.ALL)))
                        .then(Commands.argument("question", StringArgumentType.greedyString())
                                .executes(this::execute)));
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String question = StringArgumentType.getString(context, "question");

        boolean isPlayer = source.getEntity() instanceof ServerPlayer;
        String senderName = isPlayer ? source.getTextName().replaceAll("[^a-zA-Z0-9_\\-]", "_") : "console";
        final ServerPlayer playerRef = isPlayer ? source.getPlayer() : null;

        CompletableFuture.runAsync(() -> {
            String response = llmService.sendRequest(senderName, question);

            LlmEvent event = new LlmEvent(senderName, !isPlayer, question, response);
            LlmEvent.invoke(event);

            if (!event.isCancelled()) {
                String finalResponse = event.getResponse();
                List<String> chunks = splitMessage(finalResponse, 200);
                if (isPlayer) {
                    if (playerRef != null && playerRef.isAlive()) {
                        for (String chunk : chunks) {
                            playerRef.sendSystemMessage(Component.literal(chunk));
                        }
                    }
                } else {
                    for (String chunk : chunks) {
                        source.sendSystemMessage(Component.literal(chunk));
                    }
                }
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static List<String> splitMessage(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        if (text.length() <= maxLength) {
            chunks.add(text);
            return chunks;
        }

        String[] paragraphs = text.split("\n");
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (current.length() + paragraph.length() + 1 > maxLength && current.length() > 0) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }

            if (paragraph.length() > maxLength) {
                // Split long paragraph by sentences
                String[] sentences = paragraph.split("(?<=[.!?])\\s+");
                for (String sentence : sentences) {
                    if (sentence.length() > maxLength) {
                        // Hard split: sentence too long, split by characters
                        if (current.length() > 0) {
                            chunks.add(current.toString().trim());
                            current = new StringBuilder();
                        }
                        for (int i = 0; i < sentence.length(); i += maxLength) {
                            chunks.add(sentence.substring(i, Math.min(i + maxLength, sentence.length())));
                        }
                    } else {
                        if (current.length() + sentence.length() + 1 > maxLength && current.length() > 0) {
                            chunks.add(current.toString().trim());
                            current = new StringBuilder();
                        }
                        if (current.length() > 0) {
                            current.append(" ");
                        }
                        current.append(sentence);
                    }
                }
            } else {
                if (current.length() > 0) {
                    current.append("\n");
                }
                current.append(paragraph);
            }
        }

        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}