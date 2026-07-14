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

import java.util.concurrent.CompletableFuture;

public class LlmCommand {
    private static final Permission MCLLM_USE = Permission.Atom.create(
        Identifier.fromNamespaceAndPath("mcllm", "use")
    );

    private final LlmService llmService;

    public LlmCommand(LlmService llmService) {
        this.llmService = llmService;
    }

    public void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("llm")
                .requires(source ->
                    source.permissions().hasPermission(MCLLM_USE) ||
                    source.permissions().hasPermission(new Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.ALL))
                )
                .then(Commands.argument("question", StringArgumentType.greedyString())
                    .executes(this::execute))
        );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String question = StringArgumentType.getString(context, "question");
        
        boolean isPlayer = source.getEntity() instanceof ServerPlayer;
        String senderName = isPlayer ? source.getTextName() : "console";
        ServerPlayer player = isPlayer ? source.getPlayer() : null;

        CompletableFuture.runAsync(() -> {
            String response = llmService.sendRequest(senderName, question);
            
            LlmEvent event = new LlmEvent(senderName, !isPlayer, question, response);
            LlmEvent.invoke(event);
            
            if (!event.isCancelled()) {
                String finalResponse = event.getResponse();
                if (player != null && player.isAlive()) {
                    player.sendSystemMessage(Component.literal(finalResponse));
                } else {
                    source.sendSystemMessage(Component.literal(finalResponse));
                }
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
