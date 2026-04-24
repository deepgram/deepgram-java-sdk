---
name: using-text-intelligence
description: Use when writing or reviewing Java code in this repo that calls Deepgram Text Intelligence / Read (`/v1/read`) for text analysis. Covers `client.read().v1().text().analyze(...)` with `ReadV1Request` or `TextAnalyzeRequest`. Use `using-audio-intelligence` when the source is audio instead of text. Triggers include "read api", "text intelligence", "analyze text", "sentiment", "topics", "intents", and "summarize text".
---

# Using Deepgram Text Intelligence (Java SDK)

Analyze text you already have — transcripts, chats, documents, or other plain text — via `/v1/read`.

## When to use this product

- You already have **text**, not audio.
- You want one-shot REST analysis for sentiment, topics, intents, or summary-like outputs.

**Use a different skill when:**
- The source is audio and you want analysis during transcription → `using-audio-intelligence`.

## Authentication

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

## Quick start

```java
import com.deepgram.resources.read.v1.text.requests.TextAnalyzeRequest;
import com.deepgram.types.ReadV1Request;
import com.deepgram.types.ReadV1RequestText;
import com.deepgram.types.ReadV1Response;

ReadV1RequestText textBody = ReadV1RequestText.builder()
        .text("Life moves pretty fast. If you don't stop and look around once in a while, you could miss it.")
        .build();

TextAnalyzeRequest request = TextAnalyzeRequest.builder()
        .body(ReadV1Request.of(textBody))
        .sentiment(true)
        .topics(true)
        .intents(true)
        .language("en")
        .build();

ReadV1Response response = client.read().v1().text().analyze(request);
response.getResults().getTopics().ifPresent(System.out::println);
response.getResults().getIntents().ifPresent(System.out::println);
response.getResults().getSentiments().ifPresent(System.out::println);
```

## Quick start — custom topics and intents

```java
import com.deepgram.resources.read.v1.text.types.TextAnalyzeRequestCustomIntentMode;
import com.deepgram.resources.read.v1.text.types.TextAnalyzeRequestCustomTopicMode;
import java.util.Arrays;

TextAnalyzeRequest request = TextAnalyzeRequest.builder()
        .body(ReadV1Request.of(textBody))
        .language("en")
        .sentiment(true)
        .topics(true)
        .intents(true)
        .customTopic(Arrays.asList("Customer Retention", "Product Quality"))
        .customTopicMode(TextAnalyzeRequestCustomTopicMode.EXTENDED)
        .customIntent(Arrays.asList("Request Refund", "Complaint"))
        .customIntentMode(TextAnalyzeRequestCustomIntentMode.EXTENDED)
        .build();
```

## Async equivalent

```java
import com.deepgram.AsyncDeepgramClient;
import java.util.concurrent.CompletableFuture;

AsyncDeepgramClient asyncClient = AsyncDeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();

CompletableFuture<com.deepgram.types.ReadV1Response> future =
        asyncClient.read().v1().text().analyze(request);
```

## Key parameters / API surface

- `TextAnalyzeRequest.builder().body(ReadV1Request.of(...))` is the high-level request path
- Body types are `ReadV1RequestText` or URL-based `ReadV1Request` variants
- Verified request fields: `language`, `sentiment`, `summarize`, `topics`, `intents`, `customTopic`, `customTopicMode`, `customIntent`, `customIntentMode`, `callback`, `callbackMethod`, `tag`
- Methods: `text().analyze(ReadV1Request)`, `text().analyze(TextAnalyzeRequest)`, plus async and `withRawResponse()` variants
- Results live under `ReadV1Response.getResults()`

## API reference (layered)

1. **In-repo source of truth**: `src/main/java/com/deepgram/resources/read/v1/text/` and `examples/read/`. No `reference.md` file is present here.
2. **Canonical OpenAPI**: https://developers.deepgram.com/openapi.yaml
3. **Context7**: `/llmstxt/developers_deepgram_llms_txt`
4. **Product docs**:
   - https://developers.deepgram.com/reference/text-intelligence/analyze-text
   - https://developers.deepgram.com/docs/text-intelligence
   - https://developers.deepgram.com/docs/text-sentiment-analysis

## Gotchas

1. **Use `body(ReadV1Request.of(...))`, not raw maps.** This SDK is strongly typed.
2. **`TextAnalyzeRequest` wraps both query-style options and the request body.** `ReadV1Request` alone only supplies the input content.
3. **Custom topics/intents need matching modes** (`STRICT` or `EXTENDED`) if you want deterministic behavior.
4. **The request surface exposes `summarize` separately from Listen V1.** Do not assume Listen's versioned summarize enum applies here.
5. **Language is explicit in examples.** Follow that pattern when using topics, intents, or sentiment.
6. **This repo has no dedicated async example for Read.** Use `AsyncDeepgramClient` and `CompletableFuture` directly.

## Example files in this repo

- `examples/read/AnalyzeText.java`
- `examples/read/AdvancedAnalysis.java`
