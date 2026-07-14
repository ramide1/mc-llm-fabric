package com.ramide1.mcllm.llm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LlmEvent {
    private final String playerName;
    private final boolean isConsole;
    private final String message;
    private String response;
    private boolean cancelled;

    private static final List<Consumer<LlmEvent>> listeners = new ArrayList<>();

    public LlmEvent(String playerName, boolean isConsole, String message, String response) {
        this.playerName = playerName;
        this.isConsole = isConsole;
        this.message = message;
        this.response = response;
        this.cancelled = false;
    }

    public static void register(Consumer<LlmEvent> listener) {
        listeners.add(listener);
    }

    public static void invoke(LlmEvent event) {
        for (Consumer<LlmEvent> listener : listeners) {
            listener.accept(event);
            if (event.isCancelled()) break;
        }
    }

    public String getPlayerName() { return playerName; }
    public boolean isConsole() { return isConsole; }
    public String getMessage() { return message; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
