# v0.8 to v0.9 Migration Guide

This guide helps you migrate from Deepgram Java SDK v0.8.x to v0.9.0. The `0.9.0` release is still pre-`1.0`, and it ships a set of breaking source changes from the latest SDK regeneration along with additive features (Listen V2 force-end-turn and turn triggers, Listen V1 diarization detail, and 25 new Deepgram TTS voice constants).

All of the breaking changes are **source/compile-time only** — they are type and constant renames that follow the API definition. Every wire value is unchanged, so on-the-wire payloads for existing requests are byte-identical and no server-side behavior changes with this upgrade. In practice, migrating means updating imports and type references, then recompiling.

The breaking changes are:

1. **Nine provider and agent-history types renamed** — the generator dropped the `*ThinkProvider*` / `*SpeakProvider*` prefixes and consolidated the agent conversation-history leaf types. Wire values are identical.
2. **Two duplicate agent conversation-history types removed** — `AgentV1SettingsAgentContextContextMessagesItemContentRole` and `AgentV1SettingsAgentContextContextMessagesItemFunctionCallsFunctionCallsItem` are replaced by the canonical `ConversationHistoryMessageRole` and `FunctionCallHistoryMessageFunctionCallsItem`.
3. **`DeepgramModel.FLUX_RENEE_EN` removed** — the `flux-renee-en` voice is no longer in the model enum.

> **Note on `speak.v2` `connect(speed = ...)`:** the generator retyped this parameter from `Double` to a closed string-literal enum in this cycle. That is **not** shipped here — the SDK keeps `Optional<Double>`, so your existing `speed` calls continue to compile and behave identically. See [Speak V2 Connect Speed](#speak-v2-connect-speed).

## Table of Contents

- [Installation](#installation)
- [Configuration Changes](#configuration-changes)
- [Authentication Changes](#authentication-changes)
- [Type Changes](#type-changes)
  - [Provider Type Renames](#provider-type-renames)
  - [Agent Conversation-History Consolidation](#agent-conversation-history-consolidation)
  - [Removed Voice Constant](#removed-voice-constant)
  - [Speak V2 Connect Speed](#speak-v2-connect-speed)
- [New Features in v0.9.0](#new-features-in-v090)
  - [Listen V2 Force-End-Turn](#listen-v2-force-end-turn)
  - [Listen V2 Turn Trigger](#listen-v2-turn-trigger)
  - [Listen V1 Diarization Detail](#listen-v1-diarization-detail)
  - [New Deepgram Voice Constants](#new-deepgram-voice-constants)
- [Breaking Changes Summary](#breaking-changes-summary)

## Installation

Upgrade to `0.9.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.9.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.9.0</version>
</dependency>
```

## Configuration Changes

No changes. Existing `DeepgramClient.builder()` usage — including the retry tuning and transport options added in `0.8.0` — works unchanged.

## Authentication Changes

No changes. API key, access token, and session ID configuration all work the same as in `0.8.x`.

## Type Changes

### Provider Type Renames

Seven provider model/voice/version types lost their role prefixes, and two agent conversation-history types were renamed. **The constant names and their wire values are unchanged** — only the enclosing type name differs, so migration is a find-and-replace on imports and type references.

| v0.8.x | v0.9.0 |
| --- | --- |
| `AnthropicThinkProviderModel` | `AnthropicModel` |
| `CartesiaSpeakProviderModelId` | `CartesiaModelId` |
| `CartesiaSpeakProviderVoice` | `CartesiaVoice` |
| `DeepgramSpeakProviderModel` | `DeepgramModel` |
| `GoogleThinkProviderModel` | `GoogleModel` |
| `GoogleThinkProviderVersion` | `GoogleVersion` |
| `GroqThinkProviderReasoningMode` | `GroqReasoningMode` |
| `AgentV1HistoryContentRole` | `ConversationHistoryMessageRole` |
| `AgentV1HistoryFunctionCallsFunctionCallsItem` | `FunctionCallHistoryMessageFunctionCallsItem` |

All nine live in `com.deepgram.types` except the last two, which live in `com.deepgram.resources.agent.v1.types`.

**v0.8.x**

```java
import com.deepgram.types.AnthropicThinkProviderModel;
import com.deepgram.types.DeepgramSpeakProviderModel;
import com.deepgram.types.GoogleThinkProviderModel;
import com.deepgram.types.GoogleThinkProviderVersion;

Anthropic anthropic = Anthropic.builder()
    .model(AnthropicThinkProviderModel.CLAUDE_SONNET420250514)
    .build();

Deepgram speak = Deepgram.builder()
    .model(DeepgramSpeakProviderModel.AURA2ASTERIA_EN)
    .build();

Google google = Google.builder()
    .model(GoogleThinkProviderModel.GEMINI25FLASH)
    .version(GoogleThinkProviderVersion.V1BETA)
    .build();

Optional<GoogleThinkProviderVersion> version = google.getVersion();
```

**v0.9.0**

```java
import com.deepgram.types.AnthropicModel;
import com.deepgram.types.DeepgramModel;
import com.deepgram.types.GoogleModel;
import com.deepgram.types.GoogleVersion;

Anthropic anthropic = Anthropic.builder()
    .model(AnthropicModel.CLAUDE_SONNET420250514)
    .build();

Deepgram speak = Deepgram.builder()
    .model(DeepgramModel.AURA2ASTERIA_EN)
    .build();

Google google = Google.builder()
    .model(GoogleModel.GEMINI25FLASH)
    .version(GoogleVersion.V1BETA)
    .build();

Optional<GoogleVersion> version = google.getVersion();
```

Note that the getter return types change too, so a declared `Optional<GoogleThinkProviderVersion>` must be updated — not just the builder call.

`OpenAiThinkProvider` and `OpenAiThinkProviderModel` are **not** renamed and keep their existing names.

### Agent Conversation-History Consolidation

`0.8.x` carried four leaf types for the agent conversation-history messages, in two duplicate pairs. `0.9.0` collapses each pair into one canonical type. The removed types were structurally identical to their replacements — same constants, same wire values (`"user"` / `"assistant"`).

| v0.8.x | v0.9.0 |
| --- | --- |
| `AgentV1SettingsAgentContextContextMessagesItemContentRole` (removed) | `ConversationHistoryMessageRole` |
| `AgentV1HistoryContentRole` (renamed) | `ConversationHistoryMessageRole` |
| `AgentV1SettingsAgentContextContextMessagesItemFunctionCallsFunctionCallsItem` (removed) | `FunctionCallHistoryMessageFunctionCallsItem` |
| `AgentV1HistoryFunctionCallsFunctionCallsItem` (renamed) | `FunctionCallHistoryMessageFunctionCallsItem` |

This changes the field types on `ConversationHistoryMessage` and `FunctionCallHistoryMessage`:

**v0.8.x**

```java
ConversationHistoryMessage msg = ConversationHistoryMessage.builder()
    .role(AgentV1SettingsAgentContextContextMessagesItemContentRole.USER)
    .content("Hello")
    .build();
```

**v0.9.0**

```java
import com.deepgram.resources.agent.v1.types.ConversationHistoryMessageRole;

ConversationHistoryMessage msg = ConversationHistoryMessage.builder()
    .role(ConversationHistoryMessageRole.USER)
    .content("Hello")
    .build();
```

### Removed Voice Constant

`DeepgramModel.FLUX_RENEE_EN` (wire value `flux-renee-en`) is no longer part of the model enum. If you referenced it, pick another Flux voice — see [New Deepgram Voice Constants](#new-deepgram-voice-constants) for what was added this cycle.

`DeepgramModel` remains a forward-compatible enum, so if you need to send a value the current SDK does not model, `DeepgramModel.valueOf("some-model-name")` still works.

### Speak V2 Connect Speed

**No change required — this parameter is unchanged from `0.8.x`.** It is called out only because the generator attempted to change it and the SDK deliberately does not ship that change.

`speak.v2` `V2ConnectOptions.speed` remains `Optional<Double>`:

```java
V2ConnectOptions.builder()
    .model("flux-alexis-en")
    .speed(1.05)
    .build();
```

The generator retyped this to a closed seven-value string enum in this cycle. The SDK keeps `Double`, because the API contract is numeric: the mid-stream `SpeakV2Configure.speed` field is numeric on the same connection, and the server parses the number and range-checks it — reporting `SPEED_OUT_OF_RANGE` for a value outside `0.85`–`1.15` and `SPEED_INCREMENT_INVALID` for a value inside the range but off the `0.05` increment. A closed enum could not express a valid in-range value it happened to omit.

## New Features in v0.9.0

### Listen V2 Force-End-Turn

Listen V2 gains a control message to end the current turn on demand:

```java
V2WebSocketClient wsClient = client.listen().v2().v2WebSocket();
// ... connect and stream ...
wsClient.sendForceEndTurn(ListenV2ForceEndTurn.builder().build());
```

**This requires server-side enablement and is not yet generally available.** The typed surface ships now so callers are ready, but do not depend on it until the feature is enabled for your deployment — sending it before then is rejected by the server.

### Listen V2 Turn Trigger

`ListenV2TurnInfo` gains `getTrigger()`, returning `Optional<String>`, which identifies what ended a turn (for example `model`, `manual`, or `timeout`). It is typed as an open `String` so new server-side trigger values do not break deployed clients.

### Listen V1 Diarization Detail

Pre-recorded Listen V1 responses now expose which diarizer ran, via `getDiarizeInfo()` on the response metadata:

```java
Optional<ListenV1ResponseMetadataDiarizeInfo> info = response.getMetadata().getDiarizeInfo();
info.ifPresent(i -> System.out.println(i.getArch() + " " + i.getModelUuid()));
```

`ListenV1ResponseMetadataDiarizeInfo` and `ListenV1ResultsMetadataDiarizeInfo` each carry `getModelUuid()` and `getArch()`. Both are present only when diarization is enabled.

Pre-recorded words items also carry `getSpeakerConfidence()` (`Optional<Float>`) alongside the existing `getSpeaker()`. Per-word `speaker_confidence` is returned for pre-recorded transcription only, not for streaming.

### New Deepgram Voice Constants

`DeepgramModel` gained 25 constants this cycle, including the Flux TTS voices (`FLUX_KELSEY_EN`, `FLUX_SIENNA_EN`, `FLUX_MEENA_EN`, `FLUX_CONOR_EN`, `FLUX_BROOKE_EN`, `FLUX_RUFUS_EN`, `FLUX_WES_EN`, and others) as well as additional Aura 2 voices.

## Breaking Changes Summary

### Changed Type Names

| Old | New |
| --- | --- |
| `AnthropicThinkProviderModel` | `AnthropicModel` |
| `CartesiaSpeakProviderModelId` | `CartesiaModelId` |
| `CartesiaSpeakProviderVoice` | `CartesiaVoice` |
| `DeepgramSpeakProviderModel` | `DeepgramModel` |
| `GoogleThinkProviderModel` | `GoogleModel` |
| `GoogleThinkProviderVersion` | `GoogleVersion` |
| `GroqThinkProviderReasoningMode` | `GroqReasoningMode` |
| `AgentV1HistoryContentRole` | `ConversationHistoryMessageRole` |
| `AgentV1SettingsAgentContextContextMessagesItemContentRole` | `ConversationHistoryMessageRole` |
| `AgentV1HistoryFunctionCallsFunctionCallsItem` | `FunctionCallHistoryMessageFunctionCallsItem` |
| `AgentV1SettingsAgentContextContextMessagesItemFunctionCallsFunctionCallsItem` | `FunctionCallHistoryMessageFunctionCallsItem` |

### Removed

- `DeepgramModel.FLUX_RENEE_EN`

### Unchanged Despite Generator Churn

- `V2ConnectOptions.speed` stays `Optional<Double>`

### New Features

- `listen.v2` `sendForceEndTurn(ListenV2ForceEndTurn)` — requires server-side enablement, not yet generally available
- `ListenV2TurnInfo.getTrigger()`
- `ListenV1ResponseMetadata.getDiarizeInfo()` / `ListenV1ResultsMetadata.getDiarizeInfo()`
- `getSpeakerConfidence()` on pre-recorded words items
- 25 new `DeepgramModel` constants

### Migration Checklist

1. Bump the dependency to `0.9.0`.
2. Find-and-replace the nine renamed type names in imports and type references, including declared getter return types such as `Optional<GoogleThinkProviderVersion>`.
3. Replace `AgentV1SettingsAgentContextContextMessagesItem*` references with `ConversationHistoryMessageRole` / `FunctionCallHistoryMessageFunctionCallsItem`.
4. Replace any use of `DeepgramModel.FLUX_RENEE_EN`.
5. Recompile. Because every change is a source-level rename with identical wire values, a clean compile means the upgrade is complete — no request payloads change.
