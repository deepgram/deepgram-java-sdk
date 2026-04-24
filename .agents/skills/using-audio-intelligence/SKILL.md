---
name: using-audio-intelligence
description: Use when writing or reviewing Java code in this repo that enables Deepgram intelligence overlays on `/v1/listen` audio transcription - diarization, entity detection, sentiment, summarize, topics, intents, language detection, and redaction. Same endpoint as plain STT, but with extra request fields on `ListenV1RequestUrl` or `MediaTranscribeRequestOctetStream`. Use `using-speech-to-text` for plain transcripts and `using-text-intelligence` for analysis on existing text. Triggers include "audio intelligence", "diarize", "summarize audio", "sentiment from audio", "topic detection", and "redact".
---

# Using Deepgram Audio Intelligence (Java SDK)

Audio intelligence is not a separate client in this SDK. It is the **Listen V1 REST request surface** with additional analysis fields enabled.

## When to use this product

- You have **audio** and want transcript + analysis together.
- REST is the main path; the Java WebSocket client only exposes the real-time subset.

**Use a different skill when:**
- You want plain transcription only → `using-speech-to-text`.
- You already have text and only need text analysis → `using-text-intelligence`.
- You need turn-aware conversational streaming → `using-conversational-stt`.

## Authentication

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

## Quick start — REST with repo-backed example pattern

```java
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;

ListenV1RequestUrl request = ListenV1RequestUrl.builder()
        .url("https://dpgr.am/spacewalk.wav")
        .model(MediaTranscribeRequestModel.NOVA3)
        .smartFormat(true)
        .punctuate(true)
        .diarize(true)
        .language("en-US")
        .build();

MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);
```

The concrete repo example (`examples/listen/AdvancedOptions.java`) demonstrates the same pattern for enabling higher-value Listen options via the builder.

## What else the REST request surface supports

The generated `ListenV1RequestUrl` and `MediaTranscribeRequestOctetStream` classes also expose these verified analysis fields in this checkout:

- `sentiment`
- `summarize`
- `topics`
- `customTopic`
- `customTopicMode`
- `intents`
- `customIntent`
- `customIntentMode`
- `detectEntities`
- `detectLanguage`
- `diarize`
- `redact`

## Quick start — WebSocket subset

```java
import com.deepgram.resources.listen.v1.websocket.V1ConnectOptions;
import com.deepgram.resources.listen.v1.websocket.V1WebSocketClient;
import com.deepgram.types.ListenV1Model;
import java.util.concurrent.TimeUnit;

V1WebSocketClient wsClient = client.listen().v1().v1WebSocket();
wsClient.onResults(result -> System.out.println(result));

wsClient.connect(V1ConnectOptions.builder()
        .model(ListenV1Model.NOVA3)
        .diarize(true)
        .build())
        .get(10, TimeUnit.SECONDS);
```

In this Java checkout, the WebSocket connect options include `diarize`, `detectEntities`, `redact`, and the normal streaming transcription controls, but **not** `summarize`, `topics`, `intents`, or `detectLanguage`.

## Key parameters / API surface

- REST builders: `ListenV1RequestUrl` and `MediaTranscribeRequestOctetStream`
- REST analysis fields verified in source: `sentiment`, `summarize`, `topics`, `customTopic`, `customTopicMode`, `intents`, `customIntent`, `customIntentMode`, `detectEntities`, `detectLanguage`, `diarize`, `redact`
- Helpful transcription companions: `smartFormat`, `punctuate`, `paragraphs`, `utterances`, `numerals`, `keywords`, `keyterm`, `replace`, `search`
- WebSocket subset: `diarize`, `detectEntities`, `redact`, plus standard live transcription options

## API reference (layered)

1. **In-repo source of truth**: `src/main/java/com/deepgram/resources/listen/v1/media/requests/` and `src/main/java/com/deepgram/resources/listen/v1/websocket/` plus `examples/listen/AdvancedOptions.java`. `reference.md` is absent here.
2. **Canonical OpenAPI (REST)**: https://developers.deepgram.com/openapi.yaml
3. **Canonical AsyncAPI (WSS subset)**: https://developers.deepgram.com/asyncapi.yaml
4. **Context7**: `/llmstxt/developers_deepgram_llms_txt`
5. **Product docs**:
   - https://developers.deepgram.com/docs/stt-intelligence-feature-overview
   - https://developers.deepgram.com/docs/summarization
   - https://developers.deepgram.com/docs/topic-detection
   - https://developers.deepgram.com/docs/intent-recognition
   - https://developers.deepgram.com/docs/sentiment-analysis
   - https://developers.deepgram.com/docs/language-detection
   - https://developers.deepgram.com/docs/redaction
   - https://developers.deepgram.com/docs/diarization

## Gotchas

1. **There is no separate “audio intelligence client”.** Everything hangs off Listen V1.
2. **Most intelligence fields are REST-only in this SDK surface.** The WebSocket connect options do not expose `summarize`, `topics`, `intents`, or `detectLanguage`.
3. **`summarize` on Listen V1 is its own generated type.** Do not assume the Read API shape is identical.
4. **The repo example only demonstrates diarization-level options.** There is no dedicated example file for sentiment/topics/intents in this checkout.
5. **`redact` is currently a single `String` field on the REST builders.** Do not assume Python-style string-or-list support here.
6. **Model support matters.** The examples consistently use `NOVA3`; follow that unless you have verified another model supports the overlays you need.
7. **These fields live on both URL and byte-upload request builders.** Pick the builder that matches your input source.

## Example files in this repo

- `examples/listen/AdvancedOptions.java`
- `examples/listen/TranscribeUrl.java`
- `examples/listen/FileUploadTypes.java`

## Central product skills

For cross-language Deepgram product knowledge — the consolidated API reference, documentation finder, focused runnable recipes, third-party integration examples, and MCP setup — install the central skills:

```bash
npx skills add deepgram/skills
```

This SDK ships language-idiomatic code skills; `deepgram/skills` ships cross-language product knowledge (see `api`, `docs`, `recipes`, `examples`, `starters`, `setup-mcp`).
