package com.ramide1.mcllm.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component error(String message) {
        return Component.literal(message).withStyle(ChatFormatting.RED);
    }

    public static Component success(String message) {
        return Component.literal(message).withStyle(ChatFormatting.GREEN);
    }

    public static Component info(String message) {
        return Component.literal(message).withStyle(ChatFormatting.YELLOW);
    }
}
