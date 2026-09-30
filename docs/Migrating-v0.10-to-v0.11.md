# v0.10 to v0.11 Migration Guide

This guide covers the breaking source changes in Deepgram Java SDK `0.11.0`. The release corrects the shared Topics and Intents response shape, adds a Speak V2 Configure failure code, and includes Fern retry behavior updates. It also adds Flux TTS Controls support.

## Update the dependency

Upgrade to `0.11.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.11.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.11.0</version>
</dependency>
```

## Topics and Intents segments

`SharedTopics` and `SharedIntents` now expose their segments directly. The previous generated models reflected an extra `results.topics` or `results.intents` wrapper that is not present in the API payload.

| Old path | New path |
| --- | --- |
| `SharedTopics.getResults().get().getTopics().get().getSegments()` | `SharedTopics.getSegments()` |
| `SharedIntents.getResults().get().getIntents().get().getSegments()` | `SharedIntents.getSegments()` |
| `SharedTopicsResults` | Removed; access segments through `SharedTopics.getSegments()` |
| `SharedTopicsResultsTopics` | Removed; access segments through `SharedTopics.getSegments()` |
| `com.deepgram.types.SharedTopicsResultsTopicsSegmentsItem` | `com.deepgram.types.SharedTopicsSegmentsItem` |
| `com.deepgram.types.SharedTopicsResultsTopicsSegmentsItemTopicsItem` | `com.deepgram.types.SharedTopicsSegmentsItemTopicsItem` |
| `SharedIntentsResults` | Removed; access segments through `SharedIntents.getSegments()` |
| `SharedIntentsResultsIntents` | Removed; access segments through `SharedIntents.getSegments()` |
| `com.deepgram.types.SharedIntentsResultsIntentsSegmentsItem` | `com.deepgram.types.SharedIntentsSegmentsItem` |
| `com.deepgram.types.SharedIntentsResultsIntentsSegmentsItemIntentsItem` | `com.deepgram.types.SharedIntentsSegmentsItemIntentsItem` |

For example, replace the old Topics traversal:

```java
List<SharedTopicsResultsTopicsSegmentsItem> segments = topics.getResults().get()
    .getTopics().get()
    .getSegments().get();
```

with:

```java
List<SharedTopicsSegmentsItem> segments = topics.getSegments().get();
```

Make the equivalent replacement for `SharedIntents` and its segment item type.

## Speak V2 Configure failure visitor

`SpeakV2ConfigureFailureCode` now includes `CONTROL_COMBINATION_INVALID`. Implementations of `SpeakV2ConfigureFailureCode.Visitor<T>` must add `visitControlCombinationInvalid()`.

```java
SpeakV2ConfigureFailureCode.Visitor<String> visitor = new SpeakV2ConfigureFailureCode.Visitor<>() {
    @Override
    public String visitControlCombinationInvalid() {
        return "Change the conflicting Controls configuration.";
    }

    // Implement the remaining visitor methods.
};
```

## Retry behavior

Fern `4.21.2` and `4.22.2` change retry behavior for the SDK-created OkHttpClient without changing the public client-builder API.

- The configured HTTP timeout is now applied to each attempt, rather than to the full retry loop. A request can therefore take longer than its configured timeout when retries and backoff occur.
- Before retrying a retryable HTTP response, the SDK buffers that response. If a later retry fails with a transport error, the SDK returns the earlier HTTP response instead of discarding its status, headers, and body.

The tradeoffs are intentional: requests can occupy a caller for longer, and buffering holds a retryable response body in memory, but callers retain the actionable API error that triggered the retry instead of only observing a later connection failure. Set explicit timeouts and retry limits appropriate for latency-sensitive or large-response workloads, and continue to inspect returned HTTP errors during incident handling. Clients built with `.httpClient(...)` retain the caller-supplied client's timeouts, interceptors, and retry behavior.

## Flux TTS Controls

Flux TTS now supports inline Controls for speed, pauses, and pronunciation overrides. Controls are additive: batch requests use `SpeakV2Request.speed(...)` for speed and `SpeakV2Request.text(...)` for pause and pronunciation markers. Streaming requests use `V2ConnectOptions.speed(...)` or `SpeakV2Configure.speed(...)` for speed and `SpeakV2Speak.text(...)` for pronunciation controls; a pause marker on a streaming request fails with `DATA-0002`.

```java
String pause = "\\{pause:500ms\\}";
String pronunciation = "\\{\"word\": \"dupilumab\", \"pronounce\": \"duːˈpɪljuːmæb\"\\}";
```

Pronunciation controls are Early Access. A pronunciation control cannot be combined with a non-default speed or with a pause; the API rejects those combinations with `CONTROL_COMBINATION_INVALID`. The Flux TTS Controls guide publishes with the Controls release; consult that guide for supported syntax and availability once it is live.
