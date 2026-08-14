---
name: deepgram-java-voice-agent
description: Use when writing or reviewing Java code in this repo that builds an interactive voice agent over `agent.deepgram.com/v1/agent/converse`. Covers `client.agent().v1().v1WebSocket()`, `AgentV1Settings`, `sendSettings`, `sendMedia`, event handlers, provider configuration, and message injection. Use `deepgram-java-text-to-speech` for one-way synthesis or the STT skills for transcription-only flows. Triggers include "voice agent", "agent converse", "full duplex", "barge in", "function call", and "agent websocket".
---

# Using Deepgram Voice Agent (Java SDK)

Run a full-duplex voice agent over a single WebSocket: user audio in, agent events + audio out.

**Use a different skill when:**
- Transcription only → `deepgram-java-speech-to-text` or `deepgram-java-conversational-stt`.
- Speech synthesis only → `deepgram-java-text-to-speech`.
- Project/admin endpoints → `deepgram-java-management-api`.

## Authentication

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

The agent WebSocket uses the SDK's `agent` environment URL and the same auth headers.

## Quick start

Workflow: 1) Create client 2) Register handlers (including onWelcome) 3) Connect 4) onWelcome fires -- sendSettings 5) Verify onSettingsApplied 6) Stream audio via sendMedia.

```java
// imports from com.deepgram.resources.agent.v1.types.* and com.deepgram.types.*
import com.deepgram.resources.agent.v1.types.*;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.OpenAiThinkProvider;
import java.util.List;
import java.util.Map;

V1WebSocketClient wsClient = client.agent().v1().v1WebSocket();

wsClient.onWelcome(welcome -> {
    OpenAiThinkProvider openAiProvider = OpenAiThinkProvider.of(Map.of("model", "gpt-4o-mini"));

    AgentV1Settings settings = AgentV1Settings.builder()
            .audio(AgentV1SettingsAudio.builder().build())
            .agent(AgentV1SettingsAgent.builder()
                    .think(AgentV1SettingsAgentThink.of(List.of(AgentV1SettingsAgentThinkOneItem.builder()
                            .provider(AgentV1SettingsAgentThinkOneItemProvider.of(openAiProvider))
                            .prompt("You are a helpful voice assistant. Keep your responses brief.")
                            .build())))
                    .greeting("Hello! How can I help you today?")
                    .build())
            .build();

    wsClient.sendSettings(settings);
});

wsClient.onConversationText(text -> System.out.printf("[%s] %s%n", text.getRole(), text.getContent()));
wsClient.onAgentStartedSpeaking(event -> System.out.println(">> Agent started speaking"));
wsClient.onAgentV1Audio(audioData -> System.out.printf("Received %d bytes%n", audioData.size()));
wsClient.onErrorMessage(err -> System.err.println("Agent error: " + err));
wsClient.onWarning(warn -> System.err.println("Agent warning: " + warn));

try {
    wsClient.connect().get(10, java.util.concurrent.TimeUnit.SECONDS);
} catch (Exception e) {
    throw new RuntimeException("Failed to connect to voice agent", e);
}
```

## Message injection / control

The repo also demonstrates:

```java
wsClient.sendInjectUserMessage(com.deepgram.resources.agent.v1.types.AgentV1InjectUserMessage.builder()
        .content("What is the capital of France?")
        .build());

wsClient.sendInjectAgentMessage(com.deepgram.resources.agent.v1.types.AgentV1InjectAgentMessage.builder()
        .message("By the way, I can also help you with math and science questions!")
        .build());
```

## Key parameters / API surface

- Connect path: `client.agent().v1().v1WebSocket()`
- Initial session config: `AgentV1Settings`
- Common send methods: `sendSettings`, `sendMedia`, `sendUpdatePrompt`, `sendUpdateSpeak`, `sendInjectUserMessage`, `sendInjectAgentMessage`, `sendFunctionCallResponse`, `sendKeepAlive`
- Event handlers: `onWelcome`, `onSettingsApplied`, `onConversationText`, `onUserStartedSpeaking`, `onAgentThinking`, `onFunctionCallRequest`, `onAgentStartedSpeaking`, `onAgentAudioDone`, `onAgentV1Audio`, `onInjectionRefused`, `onPromptUpdated`, `onSpeakUpdated`, `onErrorMessage`, `onWarning`
- Think-model discovery lives at `client.agent().v1().settings().think().models().list()`

## API reference (layered)

1. **In-repo source of truth**: `src/main/java/com/deepgram/resources/agent/v1/` and `examples/agent/`. No `reference.md` file is present.
2. **Canonical AsyncAPI**: https://developers.deepgram.com/asyncapi.yaml
3. **Context7**: `/llmstxt/developers_deepgram_llms_txt`
4. **Product docs**:
   - https://developers.deepgram.com/reference/voice-agent/voice-agent
   - https://developers.deepgram.com/docs/voice-agent
   - https://developers.deepgram.com/docs/configure-voice-agent
   - https://developers.deepgram.com/docs/voice-agent-message-flow

## Gotchas

1. **The base URL is the agent environment, not the standard API base.** The SDK routes this automatically through `environment().getAgentURL()`.
2. **Send settings first.** The repo examples wait for `onWelcome(...)` and immediately call `sendSettings(...)`.
3. **Audio is binary `ByteString`.** Playback/output is your responsibility.
4. **`sendMedia(...)` is raw audio bytes.** Match whatever audio settings you configured.
5. **Use provider wrapper types** (`OpenAiThinkProvider.of(...)`, `AnthropicThinkProvider.of(...)`, `GoogleThinkProvider.of(...)`) rather than raw JSON. The underlying payload is `Object`, so provider-field mistakes are not caught at compile time.
6. **There is no persisted agent-configuration management client shown in this checkout.** This repo exposes live agent runtime plus think-model discovery.
7. **Closing is connection-level.** The examples call `disconnect()`; there is no separate close-message flow like Speak/Listen.

## Example files in this repo

- `examples/agent/VoiceAgent.java`
- `examples/agent/InjectMessage.java`
- `examples/agent/ProviderCombinations.java`
- `examples/agent/CustomProviders.java`

## Central product skills

For cross-language Deepgram product knowledge, install `npx skills add deepgram/skills`.
