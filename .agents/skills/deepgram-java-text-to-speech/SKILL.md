---
name: deepgram-java-text-to-speech
description: Use when writing or reviewing Java code in this repo that calls Deepgram Text-to-Speech v1 (`/v1/speak`) for audio synthesis. Covers one-shot REST via `client.speak().v1().audio().generate(...)` and streaming synthesis via `client.speak().v1().v1WebSocket()`. Use `deepgram-java-voice-agent` for full-duplex assistants instead of one-way synthesis. Triggers include "tts", "text to speech", "speak", "aura", "streaming tts", and "speak websocket".
---

# Using Deepgram Text-to-Speech (Java SDK)

Convert text to audio with REST or stream audio back incrementally over WebSocket via `/v1/speak`.

## When to use this product

- **REST (`audio().generate`)** — one-shot synthesis when you already have the full text.
- **WebSocket (`v1WebSocket()`)** — lower-latency synthesis while text arrives in chunks.

**Use a different skill when:**
- You need the system to listen, think, and speak in one session → `deepgram-java-voice-agent`.

## Authentication

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

API key auth uses `Authorization: Token <apiKey>`.

## Quick start — REST

```java
import com.deepgram.resources.speak.v1.audio.requests.SpeakV1Request;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

SpeakV1Request request = SpeakV1Request.builder()
        .text("Hello! This is a text-to-speech example using the Deepgram Java SDK.")
        .build();

InputStream audioStream = client.speak().v1().audio().generate(request);
Files.copy(audioStream, Path.of("output.mp3"), StandardCopyOption.REPLACE_EXISTING);
audioStream.close();
```

REST returns an `InputStream`, not JSON.

## Quick start — WebSocket

```java
import com.deepgram.resources.speak.v1.types.SpeakV1Close;
import com.deepgram.resources.speak.v1.types.SpeakV1CloseType;
import com.deepgram.resources.speak.v1.types.SpeakV1Flush;
import com.deepgram.resources.speak.v1.types.SpeakV1FlushType;
import com.deepgram.resources.speak.v1.types.SpeakV1Text;
import com.deepgram.resources.speak.v1.websocket.V1WebSocketClient;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

Logger logger = Logger.getLogger("StreamingTts");
V1WebSocketClient wsClient = client.speak().v1().v1WebSocket();
OutputStream audioOutput = new FileOutputStream("output_streaming.wav");

// Log write failures rather than throwing from the WebSocket callback thread
// (matches examples/speak/StreamingTts.java).
wsClient.onSpeakV1Audio(audioData -> {
    try {
        audioOutput.write(audioData.toByteArray());
    } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to write streaming audio to output file.", e);
    }
});

// Close the output stream when the server disconnects so we don't leak the file handle.
wsClient.onDisconnected(message -> {
    try {
        audioOutput.close();
    } catch (IOException e) {
        logger.log(Level.WARNING, "Failed to close streaming audio output file.", e);
    }
});

wsClient.connect().get(10, TimeUnit.SECONDS);
wsClient.sendText(SpeakV1Text.builder().text("Hello, this is streaming text to speech.").build());
wsClient.sendFlush(SpeakV1Flush.builder().type(SpeakV1FlushType.FLUSH).build());
wsClient.sendClose(SpeakV1Close.builder().type(SpeakV1CloseType.CLOSE).build());
```

## Async equivalent

```java
import com.deepgram.AsyncDeepgramClient;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

AsyncDeepgramClient asyncClient = AsyncDeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();

CompletableFuture<InputStream> future = asyncClient.speak().v1().audio().generate(request);
```

## Key parameters / API surface

- REST request builder: `SpeakV1Request.builder().text(...)`
- Verified REST params: `model`, `encoding`, `sampleRate`, `bitRate`, `container`, `callback`, `callbackMethod`, `mipOptOut`, `tag`
- REST methods: `audio().generate(request)` and `audio().withRawResponse().generate(request)`
- WSS connect options: `model`, `encoding`, `sampleRate`, `mipOptOut`
- WSS send methods: `sendText(...)`, `sendFlush(...)`, `sendClear(...)`, `sendClose(...)`
- WSS handlers: `onSpeakV1Audio`, `onMetadata`, `onFlushed`, `onCleared`, `onWarning`, plus generic connection/error hooks

## API reference (layered)

1. **In-repo source of truth**: `src/main/java/com/deepgram/resources/speak/v1/` and `examples/speak/`. `reference.md` is not present in this checkout.
2. **Canonical OpenAPI (REST)**: https://developers.deepgram.com/openapi.yaml
3. **Canonical AsyncAPI (WSS)**: https://developers.deepgram.com/asyncapi.yaml
4. **Context7**: `/llmstxt/developers_deepgram_llms_txt`
5. **Product docs**:
   - https://developers.deepgram.com/reference/text-to-speech/speak-request
   - https://developers.deepgram.com/reference/text-to-speech/speak-streaming
   - https://developers.deepgram.com/docs/tts-models

## Gotchas

1. **REST returns audio bytes as `InputStream`.** Save or consume it; do not try to deserialize JSON.
2. **Flush before close on WebSocket.** The example sends `Flush` before `Close` so the tail of the audio is not lost.
3. **Streaming audio arrives as binary `ByteString`.** Convert to bytes before writing or playback.
4. **WebSocket options are narrower than REST.** `container` and `bitRate` are REST request fields, not WebSocket connect options in this checkout.
5. **TTS defaults are minimal unless you set them.** The example only sets `text`; pick an explicit model/encoding when output format matters.
6. **There is no Java `TextBuilder` helper in this repo.** That Python helper does not exist here.
7. **Async REST is `CompletableFuture<InputStream>`.** You still need to close the stream after the future resolves.

## Example files in this repo

- `examples/speak/TextToSpeech.java`
- `examples/speak/StreamingTts.java`
- `examples/agent/ProviderCombinations.java` — shows Aura model selection inside Agent configs

## Central product skills

For cross-language Deepgram product knowledge — the consolidated API reference, documentation finder, focused runnable recipes, third-party integration examples, and MCP setup — install the central skills:

```bash
npx skills add deepgram/skills
```

This SDK ships language-idiomatic code skills; `deepgram/skills` ships cross-language product knowledge (see `api`, `docs`, `recipes`, `examples`, `starters`, `setup-mcp`).
