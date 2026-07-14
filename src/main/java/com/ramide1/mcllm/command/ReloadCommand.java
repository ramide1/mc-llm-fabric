package com.ramide1.mcllm.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.ramide1.mcllm.config.ModConfig;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ReloadCommand {
    private final ModConfig config;

    public ReloadCommand(ModConfig config) {
        this.config = config;
    }

    public void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("llmreload")
                .executes(this::execute)
        );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        config.reload();
        context.getSource().sendSystemMessage(Component.literal("Minecraft LLM configuration reloaded!"));
        return Command.SINGLE_SUCCESS;
    }
}
