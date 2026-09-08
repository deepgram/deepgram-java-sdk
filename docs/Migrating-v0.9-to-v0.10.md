# v0.9 to v0.10 Migration Guide

This guide covers the breaking source changes in Deepgram Java SDK `0.10.0`. The release removes two stale TTS symbols that were never served by the API: requests using `aura-2-perseo-it` return HTTP 400, and the model was removed from the API specification.

## Update the dependency

Upgrade to `0.10.0` with Gradle or Maven.

## Speak V2 speed

`SpeakV2Speed` is removed. Configure the numeric `V2ConnectOptions.speed` value directly; existing numeric calls continue to compile.

```java
V2ConnectOptions options = V2ConnectOptions.builder()
    .model("flux-kelsey-en")
    .speed(1.05)
    .build();
```

The spec documents speeds from `0.5` to `1.5` in `0.05` increments. As of 2026-09-07, production accepts values outside `0.85` to `1.15` only intermittently; stay inside `0.85` to `1.15` until the wider range is announced live.

## Removed Aura 2 Perseo model

`SpeakV1Model.AURA2PERSEO_IT` and `AudioGenerateRequestModel.AURA2PERSEO_IT` are removed. Replace them with a supported Italian model, such as `AURA2LIVIA_IT`.

```java
SpeakV1Model model = SpeakV1Model.AURA2LIVIA_IT;
```

The corresponding `Visitor.visitAura2PerseoIt()` method is also removed from both model visitors. Remove that override and handle a supported model instead.

## New features

`sendForceEndTurn(...)` and Flux speak-provider `expressivity` are additive. ForceEndTurn still requires server-side enablement; deployments without it return `UNPARSABLE_CLIENT_MESSAGE` and close the connection.
