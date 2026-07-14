# Minecraft LLM (Fabric)

A Fabric mod that lets you chat with AI directly in Minecraft using the OpenAI API.

## Features

- `/llm <question>` command to ask the AI
- `/llmreload` command to reload configuration
- Conversation history saved in SQLite
- Configurable (API key, base URL, model, max tokens, instructions)
- **Server-side only** - Vanilla clients can connect without installing the mod
- Compatible with Minecraft 26.2

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) >= 0.19.3 for Minecraft 26.2
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2
3. Place `mcllm-1.0.0.jar` and `fabric-api` in the `mods/` folder
4. Start the server

## Configuration

The configuration file is located at `config/mcllm/mcllm.json`:

```json
{
  "instructions": "You are a helpful assistant in Minecraft. Respond concisely and friendly.",
  "apiKey": "your-api-key-here",
  "baseUrl": "",
  "model": "gpt-4o-mini",
  "maxTokens": 800
}
```

### Parameters

- `instructions`: System instructions for the AI
- `apiKey`: Your OpenAI API key
- `baseUrl`: API base URL (empty = official OpenAI, e.g. `http://localhost:11434/v1` for Ollama)
- `model`: AI model to use (default: gpt-4o-mini)
- `maxTokens`: Maximum number of tokens in the response

## Commands

- `/llm <question>` - Ask a question to the AI
- `/llmreload` - Reload configuration (requires operator permissions)

## Dependencies

- [Fabric Loader](https://fabricmc.net/) >= 0.19.3
- [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2
- [OpenAI Java SDK](https://github.com/openai/openai-java) 4.0.0
- [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) 3.45.1.0

## Building

```bash
./gradlew build
```

The JAR will be generated in `build/libs/`

## License

MIT License - See [LICENSE](LICENSE) for details.
