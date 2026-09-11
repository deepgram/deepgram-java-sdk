# v0.9 to v0.10 Migration Guide

This guide covers the breaking source changes in Deepgram Java SDK `0.10.0`. The release removes two stale TTS symbols: the `SpeakV2Speed` type wrapper (speed is a plain number) and the `AURA2PERSEO_IT` voice, which the API never served (requests return HTTP 400) and which was removed from the API specification.

## Update the dependency

Upgrade to `0.10.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.10.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.10.0</version>
</dependency>
```

## Speak V2 speed

`SpeakV2Speed` is removed. Configure the numeric `V2ConnectOptions.speed` value directly; existing numeric calls continue to compile.

```java
V2ConnectOptions options = V2ConnectOptions.builder()
    .model("flux-kelsey-en")
    .speed(1.05)
    .build();
```

Speeds range from `0.5` to `1.5` in `0.05` increments.

## Removed Aura 2 Perseo model

`SpeakV1Model.AURA2PERSEO_IT` and `AudioGenerateRequestModel.AURA2PERSEO_IT` are removed. Replace them with a supported Italian model, such as `AURA2LIVIA_IT`.

```java
SpeakV1Model model = SpeakV1Model.AURA2LIVIA_IT;
```

`SpeakV1Model.valueOf("aura-2-perseo-it")` still compiles and sends the string, but the API returns HTTP 400.

The corresponding `Visitor.visitAura2PerseoIt()` method is also removed from both model visitors. Remove that override and handle a supported model instead.

## New features

`sendForceEndTurn(...)` and Flux speak-provider `expressivity` are additive. ForceEndTurn still requires server-side enablement; deployments without it return `UNPARSABLE_CLIENT_MESSAGE` and close the connection.
