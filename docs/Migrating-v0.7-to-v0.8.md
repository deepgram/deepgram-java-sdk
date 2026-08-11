# v0.7 to v0.8 Migration Guide

This guide helps you migrate from Deepgram Java SDK v0.7.x to v0.8.0. The `0.8.0` release is still pre-`1.0`, and it ships three breaking source changes from the latest SDK regeneration along with a set of additive features (Speak V2 interrupt/configure, Listen V2 redaction, client retry tuning, and automatic response decompression).

All three breaking changes are **source/compile-time** only — they follow the API definition, which promoted two loosely-typed fields to typed values and added one required field to a server-emitted message. On-the-wire payloads for existing requests are unchanged.

The three breaking changes are:

1. **`AgentV1UpdateListenListen.getProvider()` retyped** from `DeepgramListenProviderV2` to the new `AgentV1UpdateListenListenProvider` V1/V2 union — the API now models the `provider` field as a versioned discriminated union.
2. **`Google.getVersion()` retyped** from `Optional<String>` to `Optional<GoogleThinkProviderVersion>` — the Google think-provider `version` field is now an enum.
3. **`SpeakV2SpeechMetadataControlsApplied` gained a required `breaksApplied` field** — the builder chain now includes a `breaksApplied(int)` step between `pronunciationsApplied(...)` and `pronunciationWarnings(...)`.

## Table of Contents

- [Installation](#installation)
- [Configuration Changes](#configuration-changes)
- [Authentication Changes](#authentication-changes)
- [API Method Changes](#api-method-changes)
  - [Agent V1 (WebSocket)](#agent-v1-websocket)
  - [Listen V2 (WebSocket)](#listen-v2-websocket)
  - [Speak V2 (WebSocket)](#speak-v2-websocket)
- [Type Changes](#type-changes)
  - [Agent Update-Listen Provider Union](#agent-update-listen-provider-union)
  - [Google Think-Provider Version Enum](#google-think-provider-version-enum)
  - [Speak V2 Controls-Applied breaksApplied](#speak-v2-controls-applied-breaksapplied)
  - [Other Additive Types](#other-additive-types)
- [Breaking Changes Summary](#breaking-changes-summary)

## Installation

Upgrade to `0.8.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.8.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.8.0</version>
</dependency>
```

## Configuration Changes

No required client-construction changes. Existing `DeepgramClient.builder()` usage still works.

`0.8.0` adds optional client-level retry tuning on `ClientOptions.Builder` — all additive and defaulted:

```java
DeepgramClient.builder()
    .apiKey("YOUR_API_KEY")
    // all optional; defaults preserve prior behavior
    .initialRetryDelayMillis(1000)
    .maxRetryDelayMillis(60000)
    .retryJitterFactor(0.2)
    .build();
```

`0.8.0` also installs a response-decompression interceptor by default, so gzip/deflate-encoded HTTP responses are transparently decoded. No action required.

## Authentication Changes

No changes. API key, access token, and session ID configuration all work the same as in `0.7.x`.

## API Method Changes

### Agent V1 (WebSocket)

No breaking client-method changes. The breaking change is on the `AgentV1UpdateListenListen` **type** used when you send an `UpdateListen` message (see [Type Changes](#agent-update-listen-provider-union)).

### Listen V2 (WebSocket)

No breaking client-method changes. `0.8.0` adds an optional `redact` query parameter to the V2 WebSocket connection via `V2ConnectOptions.redact(...)` (`com.deepgram.types.ListenV2Redact`). It is additive.

### Speak V2 (WebSocket)

No breaking client-method changes. `0.8.0` adds new Speak V2 send methods and server-event handlers, all additive:

- `sendInterrupt(SpeakV2Interrupt)` and `sendConfigure(SpeakV2Configure)`
- `onSpeechInterrupted(...)`, `onConfigureSuccess(...)`, `onConfigureFailure(...)`

## Type Changes

### Agent Update-Listen Provider Union

The `provider` field on `AgentV1UpdateListenListen` changed from the bare `DeepgramListenProviderV2` to the new `AgentV1UpdateListenListenProvider` discriminated union (variants `v1` / `v2`, discriminated on `version`). Wrap your existing provider in the matching variant when building, and read it back through `getV2()` / `getV1()` (or `visit(...)`).

**v0.7.x**

```java
import com.deepgram.types.DeepgramListenProviderV2;

AgentV1UpdateListenListen listen = AgentV1UpdateListenListen.builder()
    .provider(DeepgramListenProviderV2.builder()
        // ...provider fields...
        .build())
    .build();

// reading
DeepgramListenProviderV2 provider = listen.getProvider();
```

**v0.8.0**

```java
import com.deepgram.types.DeepgramListenProviderV2;
import com.deepgram.resources.agent.v1.types.AgentV1UpdateListenListenProvider;

AgentV1UpdateListenListen listen = AgentV1UpdateListenListen.builder()
    .provider(AgentV1UpdateListenListenProvider.v2(
        DeepgramListenProviderV2.builder()
            // ...provider fields...
            .build()))
    .build();

// reading
listen.getProvider().getV2().ifPresent(v2 -> {
    // handle DeepgramListenProviderV2
});
```

### Google Think-Provider Version Enum

`Google.getVersion()` changed from `Optional<String>` to `Optional<GoogleThinkProviderVersion>`, and the `version(...)` builder methods now take a `GoogleThinkProviderVersion` instead of a `String`. Replace string literals with the corresponding constant.

Available constants (with wire values): `GoogleThinkProviderVersion.V1BETA` (`v1beta`), `AI_STUDIO_V1BETA` (`ai-studio-v1beta`), `GEMINI_ENTERPRISE_AGENT_V1` (`gemini-enterprise-agent-v1`). It is a forward-compatible enum, so unrecognized server values are preserved rather than rejected.

**v0.7.x**

```java
Google google = Google.builder()
    .version("v1beta")
    .build();

Optional<String> version = google.getVersion();
```

**v0.8.0**

```java
import com.deepgram.types.GoogleThinkProviderVersion;

Google google = Google.builder()
    .version(GoogleThinkProviderVersion.V1BETA)
    .build();

Optional<GoogleThinkProviderVersion> version = google.getVersion();
```

### Speak V2 Controls-Applied breaksApplied

`SpeakV2SpeechMetadataControlsApplied` gained a required `breaksApplied` (`int`) field, reflecting a new `breaks_applied` field in the server payload. `SpeakV2SpeechMetadataControlsApplied` is a **server-emitted (read-only)** message, so most applications only read it — a new `getBreaksApplied()` getter is now available and no migration is needed for read paths.

If you construct this type directly (uncommon — e.g. in tests), the staged builder now requires a `breaksApplied(int)` step between `pronunciationsApplied(...)` and `pronunciationWarnings(...)`.

**v0.7.x**

```java
SpeakV2SpeechMetadataControlsApplied.builder()
    .pronunciationsApplied(2)
    .pronunciationWarnings(0)
    .build();
```

**v0.8.0**

```java
SpeakV2SpeechMetadataControlsApplied.builder()
    .pronunciationsApplied(2)
    .breaksApplied(1)
    .pronunciationWarnings(0)
    .build();

// reading
int breaks = controlsApplied.getBreaksApplied();
```

### Other Additive Types

`0.8.0` also adds new generated types and constants that do not require migration unless you want to use them:

- **Speak V2 interrupt & configure**: `SpeakV2Interrupt`, `SpeakV2InterruptPlaybackOffset`, `SpeakV2Configure`, `SpeakV2ConfigureSuccess`, `SpeakV2ConfigureFailure` (+ `...Code`), and `SpeakV2SpeechInterrupted` (+ `...Metadata`, `...MetadataControlsApplied`), wired to the new client send methods and handlers above.
- **Listen V2 redaction**: `ListenV2Redact` and the `redact` V2 WebSocket query parameter (`V2ConnectOptions.redact(...)`).
- **Listen V1 diarization metadata & word speaker confidence**: `ListenV1ResponseMetadataDiarizeInfo` (+ `...Arch`) on the response metadata, plus per-word speaker-confidence fields on the words items.
- **New Deepgram Flux TTS voices**: `FLUX_*` constants added to `DeepgramSpeakProviderModel` (for example `FLUX_RUFUS_EN`). Purely additive — existing voice constants are unchanged.
- **Client retry tuning**: `ClientOptions.Builder.initialRetryDelayMillis(...)`, `maxRetryDelayMillis(...)`, `retryJitterFactor(...)`.

## Breaking Changes Summary

### Major Changes

1. **Agent update-listen provider union**: `AgentV1UpdateListenListen.getProvider()` / `provider(...)` now use `AgentV1UpdateListenListenProvider` (V1/V2 union) instead of `DeepgramListenProviderV2`.
2. **Google think-provider version enum**: `Google.getVersion()` / `version(...)` now use `GoogleThinkProviderVersion` instead of `String`.
3. **Speak V2 controls-applied field**: `SpeakV2SpeechMetadataControlsApplied` adds a required `breaksApplied` field (new builder step; new `getBreaksApplied()` getter).

### Changed Signatures

- `AgentV1UpdateListenListen.getProvider()`: `DeepgramListenProviderV2` → `AgentV1UpdateListenListenProvider`; builder `provider(DeepgramListenProviderV2)` → `provider(AgentV1UpdateListenListenProvider)`
- `Google.getVersion()`: `Optional<String>` → `Optional<GoogleThinkProviderVersion>`; builder `version(String)` / `version(Optional<String>)` → `version(GoogleThinkProviderVersion)` / `version(Optional<GoogleThinkProviderVersion>)`
- `SpeakV2SpeechMetadataControlsApplied.builder()`: `pronunciationsApplied(int)` now returns a `BreaksAppliedStage` requiring `breaksApplied(int)` before `pronunciationWarnings(int)`

### New Features in v0.8.0

- **Speak V2 interrupt/configure**: send methods (`sendInterrupt`, `sendConfigure`) and handlers (`onSpeechInterrupted`, `onConfigureSuccess`, `onConfigureFailure`) plus their message types
- **Listen V2 redaction**: `ListenV2Redact` and `V2ConnectOptions.redact(...)`
- **Listen V1 diarize info & word speaker confidence** on response metadata/words
- **New Deepgram Flux TTS voices** (`DeepgramSpeakProviderModel.FLUX_*`)
- **Client retry tuning** (`initialRetryDelayMillis`, `maxRetryDelayMillis`, `retryJitterFactor`) and automatic response decompression

### Migration Checklist

- [ ] Upgrade to `com.deepgram:deepgram-java-sdk:0.8.0`
- [ ] Wrap `AgentV1UpdateListenListen` providers in `AgentV1UpdateListenListenProvider.v2(...)` (or `.v1(...)`) and read them via `getV2()` / `getV1()`
- [ ] Replace `Google` `version` string literals with `GoogleThinkProviderVersion` constants and update any `Optional<String> getVersion()` reads
- [ ] Add a `breaksApplied(...)` step to any hand-built `SpeakV2SpeechMetadataControlsApplied` (read paths need no change)
- [ ] Rebuild your project and fix any remaining references to the changed signatures
- [ ] (Optional) Adopt Speak V2 interrupt/configure, Listen V2 `redact`, client retry tuning, and the new Flux voices
```