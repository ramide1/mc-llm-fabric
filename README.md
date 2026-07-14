# Minecraft LLM (Fabric)

Un mod de Fabric que permite chatear con IA directamente en Minecraft usando la API de OpenAI.

## Características

- Comando `/llm <pregunta>` para hacer preguntas a la IA
- Comando `/llmreload` para recargar la configuración
- Historial de conversaciones guardado en SQLite
- Configurable (API key, modelo, tokens máximos, instrucciones)
- **Server-side only** - Los clientes vanilla pueden conectarse sin instalar el mod
- Compatible con Minecraft 26.2

## Instalación

1. Instala [Fabric Loader](https://fabricmc.net/use/installer/) >= 0.19.3 para Minecraft 26.2
2. Descarga [Fabric API](https://modrinth.com/mod/fabric-api) para 26.2
3. Coloca `mcllm-1.0.0.jar` y `fabric-api` en la carpeta `mods/`
4. Inicia el servidor

## Configuración

El archivo de configuración se encuentra en `config/mcllm/mcllm.json`:

```json
{
  "instructions": "You are a helpful assistant in Minecraft. Respond concisely and friendly.",
  "apiKey": "tu-api-key-aqui",
  "baseUrl": "",
  "model": "gpt-4o-mini",
  "maxTokens": 800
}
```

### Parámetros

- `instructions`: Instrucciones del sistema para la IA
- `apiKey`: Tu API key de OpenAI
- `baseUrl`: URL base de la API (vacío = OpenAI oficial, ej: `http://localhost:11434/v1` para Ollama)
- `model`: Modelo de IA a utilizar (por defecto: gpt-4o-mini)
- `maxTokens`: Número máximo de tokens en la respuesta

## Comandos

- `/llm <pregunta>` - Hacer una pregunta a la IA
- `/llmreload` - Recargar la configuración (requiere permisos de operador)

## Dependencias

- [Fabric Loader](https://fabricmc.net/) >= 0.19.3
- [Fabric API](https://modrinth.com/mod/fabric-api) para 26.2
- [OpenAI Java SDK](https://github.com/openai/openai-java) 4.0.0
- [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) 3.45.1.0

## Desarrollo

### Build

```bash
./gradlew build
```

El JAR se generará en `build/libs/`

### Run Server

```bash
./gradlew runServer
```

## Licencia

MIT License - Ver [LICENSE](LICENSE) para más detalles.
