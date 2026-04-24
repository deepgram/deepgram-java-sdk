---
name: using-speech-to-text
description: Use when writing or reviewing Java code in this repo that calls Deepgram Speech-to-Text v1 (`/v1/listen`) for prerecorded or live transcription. Covers `client.listen().v1().media().transcribeUrl` / `transcribeFile` (REST) and `client.listen().v1().v1WebSocket()` (WebSocket). Use `using-audio-intelligence` for analytics overlays, `using-conversational-stt` for Flux `/v2/listen`, and `using-voice-agent` for full-duplex assistants. Triggers include "transcribe", "speech to text", "STT", "listen v1", "nova-3", "live transcription", and "websocket transcription".
---

# Using Deepgram Speech-to-Text (Java SDK)

Basic transcription for prerecorded audio over REST or live audio over WebSocket via `/v1/listen`.

## When to use this product

- **REST (`media().transcribeUrl` / `transcribeFile`)** — one-shot transcription of a complete URL or byte array.
- **WebSocket (`v1WebSocket()`)** — live streaming transcription with interim/final results.

**Use a different skill when:**
- You want summaries, sentiment, topics, intents, diarization, redaction, or language detection overlays on the same endpoint → `using-audio-intelligence`.
- You need turn-aware conversational streaming on `/v2/listen` → `using-conversational-stt`.
- You need a full interactive assistant with TTS + LLM orchestration → `using-voice-agent`.

## Authentication

**Gradle**
```gradle
implementation 'com.deepgram:deepgram-java-sdk:0.2.1'
```

**Maven**
```xml
<dependency>
  <groupId>com.deepgram</groupId>
  <artifactId>deepgram-java-sdk</artifactId>
  <version>0.2.1</version>
</dependency>
```

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

Default API-key auth sends `Authorization: Token <apiKey>`. `accessToken(...)` switches to `Bearer`.

## Quick start — REST (URL)

```java
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1Response;

ListenV1RequestUrl request = ListenV1RequestUrl.builder()
        .url("https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav")
        .model(MediaTranscribeRequestModel.NOVA3)
        .smartFormat(true)
        .build();

MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

result.visit(new MediaTranscribeResponse.Visitor<Void>() {
    @Override
    public Void visit(ListenV1Response response) {
        String transcript = response.getResults()
                .getChannels().get(0)
                .getAlternatives().orElse(java.util.Collections.emptyList())
                .get(0)
                .getTranscript().orElse("");
        System.out.println(transcript);
        return null;
    }

    @Override
    public Void visit(com.deepgram.types.ListenV1AcceptedResponse accepted) {
        System.out.println("Request accepted: " + accepted.getRequestId());
        return null;
    }
});
```

## Quick start — REST (file bytes)

```java
import com.deepgram.resources.listen.v1.media.requests.MediaTranscribeRequestOctetStream;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;

byte[] audioBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("audio.wav"));

MediaTranscribeRequestOctetStream request = MediaTranscribeRequestOctetStream.builder()
        .body(audioBytes)
        .model(MediaTranscribeRequestModel.NOVA3)
        .smartFormat(true)
        .build();

MediaTranscribeResponse result = client.listen().v1().media().transcribeFile(request);
```

`transcribeFile(...)` accepts either raw `byte[]` or a full `MediaTranscribeRequestOctetStream` request object.

## Quick start — WebSocket (live streaming)

```java
import com.deepgram.resources.listen.v1.types.ListenV1CloseStream;
import com.deepgram.resources.listen.v1.types.ListenV1CloseStreamType;
import com.deepgram.resources.listen.v1.websocket.V1ConnectOptions;
import com.deepgram.resources.listen.v1.websocket.V1WebSocketClient;
import com.deepgram.types.ListenV1Model;
import java.util.concurrent.TimeUnit;

V1WebSocketClient wsClient = client.listen().v1().v1WebSocket();

wsClient.onResults(result -> {
    if (result.getChannel() != null
            && result.getChannel().getAlternatives() != null
            && !result.getChannel().getAlternatives().isEmpty()) {
        String transcript = result.getChannel().getAlternatives().get(0).getTranscript();
        boolean isFinal = result.getIsFinal().orElse(false);
        System.out.printf("%s %s%n", isFinal ? "[final]" : "[interim]", transcript);
    }
});

wsClient.connect(V1ConnectOptions.builder().model(ListenV1Model.NOVA3).build())
        .get(10, TimeUnit.SECONDS);

// send raw audio chunks here
// wsClient.sendMedia(okio.ByteString.of(audioChunk));

wsClient.sendCloseStream(ListenV1CloseStream.builder()
        .type(ListenV1CloseStreamType.CLOSE_STREAM)
        .build());
```

## Async equivalents

```java
import com.deepgram.AsyncDeepgramClient;
import java.util.concurrent.CompletableFuture;

AsyncDeepgramClient asyncClient = AsyncDeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();

CompletableFuture<com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse> future =
        asyncClient.listen().v1().media().transcribeUrl(request);
```

The async REST clients return `CompletableFuture<T>`. WebSocket clients are already asynchronous and also return `CompletableFuture<Void>` from `connect(...)` and send methods.

## Key parameters / API surface

- REST request builders: `ListenV1RequestUrl.builder()` and `MediaTranscribeRequestOctetStream.builder()`
- Common REST params verified in source: `model`, `language`, `encoding`, `smartFormat`, `punctuate`, `diarize`, `detectEntities`, `multichannel`, `numerals`, `paragraphs`, `utterances`, `keywords`, `keyterm`, `replace`, `search`, `mipOptOut`, `tag`, `callback`
- REST methods: `transcribeUrl(...)`, `transcribeFile(byte[])`, `transcribeFile(MediaTranscribeRequestOctetStream)`
- WSS connect options: `model`, `encoding`, `sampleRate`, `endpointing`, `interimResults`, `vadEvents`, `utteranceEndMs`, `diarize`, `detectEntities`, `redact`, `keywords`, `keyterm`, `language`
- WSS send methods: `sendMedia(...)`, `sendFinalize(...)`, `sendKeepAlive(...)`, `sendCloseStream(...)`
- WSS handlers: `onResults`, `onMetadata`, `onUtteranceEnd`, `onSpeechStarted`, plus generic `onConnected`, `onDisconnected`, `onError`, `onMessage`
- REST responses are a union: `ListenV1Response` or `ListenV1AcceptedResponse`, handled via `MediaTranscribeResponse.Visitor`

## API reference (layered)

1. **In-repo source of truth**: generated clients and request/response models under `src/main/java/com/deepgram/resources/listen/v1/` plus examples under `examples/listen/`. This checkout does **not** include `reference.md`.
2. **Canonical OpenAPI (REST)**: https://developers.deepgram.com/openapi.yaml
3. **Canonical AsyncAPI (WSS)**: https://developers.deepgram.com/asyncapi.yaml
4. **Context7**: `/llmstxt/developers_deepgram_llms_txt`
5. **Product docs**:
   - https://developers.deepgram.com/reference/speech-to-text/listen-pre-recorded
   - https://developers.deepgram.com/reference/speech-to-text/listen-streaming

## Gotchas

1. **API key auth is `Token`, not `Bearer`.** `Bearer` only happens when you use `accessToken(...)`.
2. **REST responses are a union.** Handle both `ListenV1Response` and `ListenV1AcceptedResponse` with the visitor.
3. **`transcribeFile(byte[])` reads the whole file into memory.** Use the request builder only when you need extra params.
4. **The Java REST request currently exposes `redact` as a single `String`.** Do not assume Python-style list support in this checkout.
5. **WebSocket audio must match declared encoding/sample rate.** If you set `encoding`, the bytes must actually match it.
6. **Live sessions should end explicitly.** Use `sendFinalize(...)` or `sendCloseStream(...)`; otherwise trailing audio can be lost.
7. **WebSocket handlers should be registered before `connect(...)`.** The examples do this consistently.
8. **`V1WebSocketClient` is async already.** Wait on `connect(...).get(...)` before sending audio.

## Example files in this repo

- `examples/listen/TranscribeUrl.java`
- `examples/listen/FileUploadTypes.java`
- `examples/listen/AdvancedOptions.java`
- `examples/listen/LiveStreaming.java`
- `examples/listen/TranscribeCallback.java`
- `examples/listen/Captions.java`
