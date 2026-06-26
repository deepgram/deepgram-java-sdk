# v0.5 to v0.6 Migration Guide

This guide helps you migrate from Deepgram Java SDK v0.5.0 to v0.6.0. The `0.6.0` release is still pre-`1.0`, and it ships breaking source changes from the June 15 SDK regeneration along with several additive features.

The breaking changes are all **source/compile-time** changes for SDK users. The on-the-wire payloads are unchanged — your requests and responses still serialize the same way; only the Java API surface moved.

The biggest changes are:

1. `ListenV2CloseStream` no longer takes a `type` — build it with `ListenV2CloseStream.builder().build()`, and `ListenV2CloseStreamType` was removed
2. `DeepgramListenProviderV2` renamed `language_hint` to `language_hints` and made it a list — `getLanguageHint()` became `getLanguageHints()` returning `Optional<List<String>>`, and `DeepgramListenProviderV2LanguageHint` was removed
3. `ListenV2TurnInfoWordsItem.getStart()` / `getEnd()` now return `Optional<Double>` instead of `double` (Flux word timestamps)

## Table of Contents

- [Installation](#installation)
- [Configuration Changes](#configuration-changes)
- [Authentication Changes](#authentication-changes)
- [API Method Changes](#api-method-changes)
  - [Listen V2 (WebSocket)](#listen-v2-websocket)
- [Type Changes](#type-changes)
  - [Close Stream Type](#close-stream-type)
  - [Listen Provider V2 Language Hints](#listen-provider-v2-language-hints)
  - [Flux Word Timestamps](#flux-word-timestamps)
  - [Other Additive Types](#other-additive-types)
- [Breaking Changes Summary](#breaking-changes-summary)

## Installation

Upgrade to `0.6.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.6.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.6.0</version>
</dependency>
```

## Configuration Changes

No required client-construction changes. Existing `DeepgramClient.builder()` usage still works.

## Authentication Changes

No changes. API key, access token, and session ID configuration all work the same as in `0.5.0`.

## API Method Changes

### Listen V2 (WebSocket)

No breaking client-method changes. The breaking changes are all on the request/response **types** used with the V2 WebSocket client (see [Type Changes](#type-changes)).

`0.6.0` adds an optional `profanity_filter` query parameter to the V2 WebSocket connection via `V2ConnectOptions`, and a `diarize_model` option on streaming requests (with `diarize` now documented as deprecated). Both are additive.

## Type Changes

### Close Stream Type

`ListenV2CloseStream` is now a fixed control message. The `type` field was removed, `getType()` always returns the constant `"CloseStream"`, and the `ListenV2CloseStreamType` enum (with its `CLOSE_STREAM` / `KEEP_ALIVE` / `FINALIZE` values) was deleted. The builder no longer has a `.type(...)` stage.

**v0.5.0**

```java
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStreamType;

wsClient.sendCloseStream(ListenV2CloseStream.builder()
    .type(ListenV2CloseStreamType.CLOSE_STREAM)
    .build());
```

**v0.6.0**

```java
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;

wsClient.sendCloseStream(ListenV2CloseStream.builder().build());
```

Remove any imports of `ListenV2CloseStreamType` and any `.type(...)` calls on the close-stream builder. If you read the discriminator, `getType()` still works and returns `"CloseStream"` as a `String`.

### Listen Provider V2 Language Hints

`DeepgramListenProviderV2` renamed its single-value `language_hint` to a multi-value `language_hints` list, and the `DeepgramListenProviderV2LanguageHint` enum was deleted.

| v0.5.0 | v0.6.0 |
|---|---|
| `getLanguageHint() : Optional<DeepgramListenProviderV2LanguageHint>` | `getLanguageHints() : Optional<List<String>>` |
| `builder().languageHint(...)` | `builder().languageHints(List.of(...))` |
| `DeepgramListenProviderV2LanguageHint` enum | removed — use plain `String` values |

**v0.5.0**

```java
import com.deepgram.types.DeepgramListenProviderV2;
import com.deepgram.types.DeepgramListenProviderV2LanguageHint;

DeepgramListenProviderV2 provider = DeepgramListenProviderV2.builder()
    .languageHint(DeepgramListenProviderV2LanguageHint.valueOf("en"))
    .build();
```

**v0.6.0**

```java
import com.deepgram.types.DeepgramListenProviderV2;
import java.util.List;

DeepgramListenProviderV2 provider = DeepgramListenProviderV2.builder()
    .languageHints(List.of("en", "es"))
    .build();
```

Note: this is the `DeepgramListenProviderV2` type only. The unrelated `V2ConnectOptions.languageHint(...)` / `ListenV2LanguageHint` on the WebSocket query-param path is unchanged.

### Flux Word Timestamps

`ListenV2TurnInfoWordsItem` gains Flux word-level `start` and `end` timestamps (in seconds). They are modeled as `Optional<Double>`, so the getter return types changed from `double` to `Optional<Double>`. Existing call sites that read these fields must adapt.

**v0.5.0**

```java
double start = word.getStart();
double end = word.getEnd();
```

**v0.6.0**

```java
import java.util.Optional;

Optional<Double> start = word.getStart();
Optional<Double> end = word.getEnd();

// e.g. with a default when absent:
double startSeconds = word.getStart().orElse(0.0);
```

On the builder, `start`/`end` accept either a `Double` or an `Optional<Double>` and default to empty when omitted.

### Other Additive Types

`0.6.0` also adds new generated types and fields that do not require migration unless you want to use them:

- `DiarizeModel` (`v1` / `latest`) and a `diarizeModel` option on streaming requests (`diarize` is now deprecated)
- `ListenV2ProfanityFilter` and the `profanity_filter` V2 WebSocket query parameter (`V2ConnectOptions.profanityFilter(...)`)
- `DateTimeDeserializer` now also accepts space-separated timestamps (e.g. `"2025-02-15 10:30:00+00:00"`)

## Breaking Changes Summary

### Major Changes

1. **Close stream narrowing**: `ListenV2CloseStream.builder().type(...)` removed; build with `ListenV2CloseStream.builder().build()`. `ListenV2CloseStreamType` deleted.
2. **Language hints rename**: `DeepgramListenProviderV2.getLanguageHint()` → `getLanguageHints()` (`Optional<List<String>>`); `languageHint(...)` builder stage → `languageHints(...)`. `DeepgramListenProviderV2LanguageHint` deleted.
3. **Flux word timestamps**: `ListenV2TurnInfoWordsItem.getStart()` / `getEnd()` return type `double` → `Optional<Double>`.

### Removed or Renamed Features

- `ListenV2CloseStreamType` (and its `type` field on `ListenV2CloseStream`)
- `DeepgramListenProviderV2LanguageHint`
- `DeepgramListenProviderV2.getLanguageHint()` / `languageHint(...)`

### New Features in v0.6.0

- **Flux word timestamps**: `ListenV2TurnInfoWordsItem` `start` / `end`
- **Diarize model selection**: `DiarizeModel` and `diarizeModel` on streaming requests
- **Profanity filter**: `ListenV2ProfanityFilter` and the `profanity_filter` V2 WebSocket query parameter
- **Multi-value language hints**: `DeepgramListenProviderV2.languageHints(List<String>)`

### Migration Checklist

- [ ] Upgrade to `com.deepgram:deepgram-java-sdk:0.6.0`
- [ ] Replace `ListenV2CloseStream.builder().type(...)` with `ListenV2CloseStream.builder().build()` and remove `ListenV2CloseStreamType` imports
- [ ] Replace `DeepgramListenProviderV2.languageHint(...)` with `languageHints(List.of(...))` and remove `DeepgramListenProviderV2LanguageHint` imports
- [ ] Update reads of `ListenV2TurnInfoWordsItem.getStart()` / `getEnd()` to handle `Optional<Double>`
- [ ] Rebuild your project and fix any remaining imports referencing removed or renamed generated types
