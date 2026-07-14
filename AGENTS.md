# Agent Instructions - Minecraft LLM (Fabric)

## Build

- **Platform:** Fabric mod for Minecraft 26.2
- **JDK:** Java 25
- **Build command:** `./gradlew build` (generates JAR in `build/libs/`)
- **Loom:** `net.fabricmc.fabric-loom` 1.17 (non-obfuscated MC versions)
- **Mappings:** None (MC 26.2 is unobfuscated, uses Mojang names natively)

## Dependencies

- `com.openai:openai-java:4.0.0` - OpenAI SDK
- `org.xerial:sqlite-jdbc:3.45.1.0` - SQLite for chat history
- `net.fabricmc.fabric-api:fabric-api:0.152.1+26.2`

## Architecture

- **Type:** Server-side only mod (vanilla clients can connect without installing the mod)
- **Entry point:** `McLlm.java` implements `ModInitializer`
- **Config:** `config/mcllm/mcllm.json` (Gson-based, not cloth config)
- **Database:** SQLite at `config/mcllm/history.db` (table: `history` with user_id, role, content, timestamp)
- **Commands:** Brigadier - `/llm <question>` and `/llmreload`
- **Threading:** OpenAI API calls run via `CompletableFuture.runAsync()` (no Bukkit scheduler)

## Conventions

- Package: `com.ramide1.mcllm`
- All API calls and DB I/O must be async (never block server thread)
- `LlmEvent` uses a simple callback list pattern (not Fabric events API)
- No mixins currently configured (empty `mcllm.mixins.json`)

## Gotchas

- MC 26.2 is unobfuscated: no Yarn mappings needed, code uses Mojang names directly
- **Mojang mappings class names:** `Commands` (not `CommandManager`), `CommandSourceStack` (not `ServerCommandSource`), `ServerPlayer` (not `ServerPlayerEntity`), `Component` (not `Text`), `ChatFormatting` (not `Formatting`)
- `sendMessage()` → `sendSystemMessage()`, `Text.literal()` → `Component.literal()`, `formatted()` → `withStyle()`
- `source.getName()` → `source.getTextName()`
- Plugin ID must be `net.fabricmc.fabric-loom` (not `fabric-loom`) for MC 26.1+
- `tasks.jar {}` must be used instead of bare `jar {}` in Kotlin DSL with Gradle 9.x
- Fabric Loader 0.19.0+ required for MC 26.x
- Loom `mods` block: use string source set names (`"main"`, `"client"`) in Kotlin DSL
- `archives_base_name` → `base.archivesName.get()` in Kotlin DSL
