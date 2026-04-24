---
name: using-voice-agent
description: Use when writing or reviewing Java code in this repo that builds an interactive voice agent over `agent.deepgram.com/v1/agent/converse`. Covers `client.agent().v1().v1WebSocket()`, `AgentV1Settings`, `sendSettings`, `sendMedia`, event handlers, provider configuration, and message injection. Use `using-text-to-speech` for one-way synthesis or the STT skills for transcription-only flows. Triggers include "voice agent", "agent converse", "full duplex", "barge in", "function call", and "agent websocket".
---

# Using Deepgram Voice Agent (Java SDK)

Run a full-duplex voice agent over a single WebSocket: user audio in, agent events + audio out.

## When to use this product

- You want a live conversational agent.
- You need STT + think-provider + TTS orchestration in one session.
- You may need message injection, prompt updates, or function-call handling.

**Use a different skill when:**
- You only need transcription → `using-speech-to-text` or `using-conversational-stt`.
- You only need speech synthesis → `using-text-to-speech`.
- You only need project/admin endpoints → `using-management-api`.

## Authentication

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

The agent WebSocket uses the SDK's `agent` environment URL and the same auth headers.

## Quick start

```java
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThinkOneItem;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThinkOneItemProvider;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudio;
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

wsClient.connect().get(10, java.util.concurrent.TimeUnit.SECONDS);
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
5. **Provider config is strongly typed.** Use `OpenAiThinkProvider`, `Anthropic`, `Google`, etc., not ad-hoc JSON strings.
6. **There is no persisted agent-configuration management client shown in this checkout.** This repo exposes live agent runtime plus think-model discovery.
7. **Closing is connection-level.** The examples call `disconnect()`; there is no separate close-message flow like Speak/Listen.

## Example files in this repo

- `examples/agent/VoiceAgent.java`
- `examples/agent/InjectMessage.java`
- `examples/agent/ProviderCombinations.java`
- `examples/agent/CustomProviders.java`
