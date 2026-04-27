---
name: deepgram-java-conversational-stt
description: Use when writing or reviewing Java code in this repo that calls Deepgram Conversational STT v2 / Flux over `/v2/listen`. Covers `client.listen().v2().v2WebSocket()`, `V2ConnectOptions`, `onTurnInfo`, and turn-aware close handling. Use `deepgram-java-speech-to-text` for standard v1 transcription and `deepgram-java-voice-agent` for fully interactive assistants. Triggers include "flux", "conversational stt", "listen v2", "turn detection", "end of turn", and "eot".
---

# Using Deepgram Conversational STT / Flux (Java SDK)

Turn-aware streaming transcription over `/v2/listen` for conversational audio.

## When to use this product

- You want explicit turn events, not just regular interim/final transcript chunks.
- You are building conversational UX where end-of-turn timing matters.

**Use a different skill when:**
- You need general-purpose STT over REST or classic streaming → `deepgram-java-speech-to-text`.
- You need a hosted interactive assistant → `deepgram-java-voice-agent`.

## Authentication

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

## Quick start

```java
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStreamType;
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.listen.v2.websocket.V2WebSocketClient;
import java.util.concurrent.TimeUnit;

V2WebSocketClient wsClient = client.listen().v2().v2WebSocket();

wsClient.onConnected(connected ->
        System.out.println("request_id=" + connected.getRequestId()));

wsClient.onTurnInfo(turnInfo -> {
    System.out.printf("[%s] turn=%.0f transcript=\"%s\"%n",
            turnInfo.getEvent(),
            turnInfo.getTurnIndex(),
            turnInfo.getTranscript());
});

wsClient.connect(V2ConnectOptions.builder()
        .model("flux-general-en")
        .build())
        .get(10, TimeUnit.SECONDS);

// wsClient.sendMedia(okio.ByteString.of(audioChunk));

wsClient.sendCloseStream(ListenV2CloseStream.builder()
        .type(ListenV2CloseStreamType.CLOSE_STREAM)
        .build());
```

## Key parameters / API surface

- Entry point: `client.listen().v2().v2WebSocket()`
- Required connect field: `model(String)`
- Verified connect options in source: `encoding`, `sampleRate`, `eagerEotThreshold`, `eotThreshold`, `eotTimeoutMs`, `keyterm`, `mipOptOut`, `tag`
- Send methods: `sendMedia(...)`, `sendCloseStream(...)`
- Event handlers: `onConnected(Consumer<ListenV2Connected>)`, `onTurnInfo(...)`, `onErrorMessage(...)`, plus generic connection/error hooks

## API reference (layered)

1. **In-repo source of truth**: `src/main/java/com/deepgram/resources/listen/v2/` and `examples/listen/LiveStreamingV2.java`. No `reference.md` exists in this checkout.
2. **Canonical AsyncAPI**: https://developers.deepgram.com/asyncapi.yaml
3. **Context7**: `/llmstxt/developers_deepgram_llms_txt`
4. **Product docs**:
   - https://developers.deepgram.com/reference/speech-to-text/listen-flux
   - https://developers.deepgram.com/docs/flux/quickstart
   - https://developers.deepgram.com/docs/flux/language-prompting

## Gotchas

1. **This is WebSocket-only in the Java SDK.** There is no REST helper for `/v2/listen` here.
2. **`model` is a plain `String`, not an enum.** Use Flux model IDs such as `flux-general-en` exactly.
3. **Close with `sendCloseStream(...)`, not Listen V1 finalize.** The message type is different from v1.
4. **The current Java connect options do not expose `language_hint`.** Do not assume the Python surface exists here.
5. **Turn events are the main payload.** Handle `onTurnInfo(...)`, not Listen V1 `onResults(...)`.
6. **You still need to stream binary audio manually.** The example only wires handlers and close flow.
7. **Wait for `connect(...).get(...)` before sending media.** The client is async but not fire-and-forget.

## Example files in this repo

- `examples/listen/LiveStreamingV2.java`

## Central product skills

For cross-language Deepgram product knowledge — the consolidated API reference, documentation finder, focused runnable recipes, third-party integration examples, and MCP setup — install the central skills:

```bash
npx skills add deepgram/skills
```

This SDK ships language-idiomatic code skills; `deepgram/skills` ships cross-language product knowledge (see `api`, `docs`, `recipes`, `examples`, `starters`, `setup-mcp`).
