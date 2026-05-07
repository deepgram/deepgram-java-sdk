---
name: deepgram-java-audio-intelligence
description: Use when writing or reviewing Java code in this repo that enables Deepgram intelligence overlays on `/v1/listen` audio transcription - diarization, entity detection, sentiment, summarize, topics, intents, language detection, and redaction. Same endpoint as plain STT, but with extra request fields on `ListenV1RequestUrl` or `MediaTranscribeRequestOctetStream`. Use `deepgram-java-speech-to-text` for plain transcripts and `deepgram-java-text-intelligence` for analysis on existing text. Triggers include "audio intelligence", "diarize", "summarize audio", "sentiment from audio", "topic detection", and "redact".
---

# Using Deepgram Audio Intelligence (Java SDK)

Audio intelligence is not a separate client in this SDK. It is the **Listen V1 REST request surface** with additional analysis fields enabled.

**Use a different skill when:**
- Plain transcription only → `deepgram-java-speech-to-text`.
- Text (not audio) analysis → `deepgram-java-text-intelligence`.
- Turn-aware conversational streaming → `deepgram-java-conversational-stt`.

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

The concrete repo example (`examples/listen/AdvancedOptions.java`) demonstrates the same pattern for enabling higher-value Listen options via the builder. Always check the response for the intelligence fields you requested:

```java
result.visit(new MediaTranscribeResponse.Visitor<Void>() {
    @Override
    public Void visit(ListenV1Response response) {
        response.getResults().getSentiments().ifPresent(s -> System.out.println("Sentiment: " + s));
        return null;
    }
    @Override
    public Void visit(com.deepgram.types.ListenV1AcceptedResponse accepted) {
        System.out.println("Async accepted: " + accepted.getRequestId());
        return null;
    }
});
```

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

1. **In-repo source of truth**: `src/main/java/com/deepgram/resources/listen/v1/media/requests/` and `src/main/java/com/deepgram/resources/listen/v1/websocket/` plus `examples/listen/AdvancedOptions.java`.
2. **Canonical OpenAPI (REST)**: https://developers.deepgram.com/openapi.yaml
3. **Canonical AsyncAPI (WSS subset)**: https://developers.deepgram.com/asyncapi.yaml
4. **Product docs**: https://developers.deepgram.com/docs/stt-intelligence-feature-overview (links to individual feature docs for summarization, topics, intents, sentiment, language detection, redaction, diarization).

## Gotchas

1. **No separate “audio intelligence client”.** Everything hangs off Listen V1 request builders.
2. **Most intelligence fields are REST-only.** WebSocket connect options do not expose `summarize`, `topics`, `intents`, or `detectLanguage`.
3. **`summarize` on Listen V1 has its own generated type.** Do not assume the Read API shape is identical.
4. **`redact` is a single `String` field** on the REST builders -- not a list like the Python SDK.
5. **Use `NOVA3` model** unless you have verified another model supports the overlays you need.
6. **Both URL and byte-upload builders expose intelligence fields.** Pick the builder that matches your input source.

## Example files in this repo

- `examples/listen/AdvancedOptions.java`
- `examples/listen/TranscribeUrl.java`
- `examples/listen/FileUploadTypes.java`

## Central product skills

For cross-language Deepgram product knowledge, install `npx skills add deepgram/skills`.
