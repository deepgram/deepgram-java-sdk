# v0.6 to v0.7 Migration Guide

This guide helps you migrate from Deepgram Java SDK v0.6.x to v0.7.0. The `0.7.0` release is still pre-`1.0`, and it ships one breaking source change from the July 20 SDK regeneration along with two additive features.

The breaking change is **source/compile-time** only. The on-the-wire payloads are unchanged — your requests and responses still serialize the same way; only one field was removed from the Java API surface because the API removed it from the spec.

The biggest change is:

1. `AgentV1LatencyReport.getSttLatency()` was removed — the Voice Agent `LatencyReport` no longer reports `stt_latency` ([deepgram-docs #1006](https://github.com/deepgram/deepgram-docs/pull/1006)). Drop any reads of `getSttLatency()`; the remaining latency getters are unchanged.

## Table of Contents

- [Installation](#installation)
- [Configuration Changes](#configuration-changes)
- [Authentication Changes](#authentication-changes)
- [API Method Changes](#api-method-changes)
  - [Agent V1 (WebSocket)](#agent-v1-websocket)
  - [Listen V2 (WebSocket)](#listen-v2-websocket)
- [Type Changes](#type-changes)
  - [Latency Report STT Latency Removal](#latency-report-stt-latency-removal)
  - [Other Additive Types](#other-additive-types)
- [Breaking Changes Summary](#breaking-changes-summary)

## Installation

Upgrade to `0.7.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.7.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.7.0</version>
</dependency>
```

## Configuration Changes

No required client-construction changes. Existing `DeepgramClient.builder()` usage still works.

## Authentication Changes

No changes. API key, access token, and session ID configuration all work the same as in `0.6.x`.

## API Method Changes

### Agent V1 (WebSocket)

No breaking client-method changes. The breaking change is on the `AgentV1LatencyReport` server-event **type** you receive in an agent session (see [Type Changes](#type-changes)).

### Listen V2 (WebSocket)

No breaking client-method changes. `0.7.0` adds an optional `numerals` query parameter to the V2 WebSocket connection via `V2ConnectOptions`. It is additive.

## Type Changes

### Latency Report STT Latency Removal

The Voice Agent `LatencyReport` server event no longer includes `stt_latency`, so `AgentV1LatencyReport.getSttLatency()` and the `sttLatency(...)` builder methods were removed. `LatencyReport` is a server-emitted (read-only) message, so this has **no request/wire impact** — but any call site that read `getSttLatency()` will no longer compile.

The remaining latency getters are unchanged: `getTttTokenLatency()`, `getTttTextLatency()`, `getTttToolLatency()`, `getTttThinkingLatency()`, `getTtsLatency()`, and `getTotalLatency()`.

**v0.6.x**

```java
report.getSttLatency().ifPresent(stt -> System.out.println("STT latency: " + stt));
report.getTtsLatency().ifPresent(tts -> System.out.println("TTS latency: " + tts));
```

**v0.7.0**

```java
// stt_latency is no longer reported by the server; drop the read.
report.getTtsLatency().ifPresent(tts -> System.out.println("TTS latency: " + tts));
```

If you need to tolerate an `stt_latency` value on the wire from an older server, it is still accessible through the type's additional (unknown) properties rather than a typed getter.

### Other Additive Types

`0.7.0` also adds new generated types and constants that do not require migration unless you want to use them:

- **Flux STT `numerals`**: a new `ListenV2Numerals` type (`com.deepgram.types.ListenV2Numerals`, values `TRUE` / `FALSE`) and an optional `numerals` query parameter on the Listen V2 WebSocket connection via `V2ConnectOptions.numerals(...)`. It renders spoken numbers as digits in the transcript (for example, "twenty three" → "23"). Connection-time only.

  ```java
  import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
  import com.deepgram.types.ListenV2Model;
  import com.deepgram.types.ListenV2Numerals;

  wsClient.connect(V2ConnectOptions.builder()
      .model(ListenV2Model.FLUX_GENERAL_EN)
      .numerals(ListenV2Numerals.TRUE)
      .build());
  ```

- **New Aura-2 multilingual TTS voices**: roughly 40 new voices were added across `SpeakV1Model` (streaming/`speak.v2`) and `AudioGenerateRequestModel` (REST/`speak.v1.audio.generate`), covering Italian, Dutch, Spanish, German, Japanese, and French (for example, `SpeakV1Model.AURA2AURELIA_DE`, wire name `aura-2-aurelia-de`). Purely additive — existing voice constants are unchanged.

## Breaking Changes Summary

### Major Changes

1. **Latency report STT removal**: `AgentV1LatencyReport.getSttLatency()` and the `sttLatency(...)` builder methods removed; the server no longer emits `stt_latency`.

### Removed or Renamed Features

- `AgentV1LatencyReport.getSttLatency()` and `AgentV1LatencyReport.Builder.sttLatency(...)`

### New Features in v0.7.0

- **Flux STT numerals**: `ListenV2Numerals` and the `numerals` V2 WebSocket query parameter (`V2ConnectOptions.numerals(...)`)
- **New Aura-2 multilingual TTS voices**: ~40 voices added to `SpeakV1Model` and `AudioGenerateRequestModel` (it/nl/es/de/ja/fr)

### Migration Checklist

- [ ] Upgrade to `com.deepgram:deepgram-java-sdk:0.7.0`
- [ ] Remove any reads of `AgentV1LatencyReport.getSttLatency()` and any `sttLatency(...)` builder calls
- [ ] Rebuild your project and fix any remaining references to the removed getter
- [ ] (Optional) Adopt `V2ConnectOptions.numerals(...)` and the new Aura-2 multilingual voices
