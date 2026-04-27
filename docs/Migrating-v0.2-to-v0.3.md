# v0.2 to v0.3 Migration Guide

This guide helps you migrate from Deepgram Java SDK v0.2.x (versions 0.2.0 to 0.2.1) to v0.3.0. The `0.3.0` release is still pre-`1.0`, but it does include breaking source changes from the April 27 SDK regeneration.

The biggest changes are:

1. Agent think/speak configuration types were consolidated into shared top-level schemas in `com.deepgram.types`
2. `Listen V2` WebSocket connect options now use typed model values instead of raw strings
3. Several generated agent-local helper types were removed

## Table of Contents

- [Installation](#installation)
- [Configuration Changes](#configuration-changes)
- [Authentication Changes](#authentication-changes)
- [API Method Changes](#api-method-changes)
  - [Listen V1 (REST)](#listen-v1-rest)
  - [Listen V2 (WebSocket)](#listen-v2-websocket)
  - [Speak V1](#speak-v1)
  - [Agent V1 (WebSocket)](#agent-v1-websocket)
  - [Read V1](#read-v1)
  - [Manage V1](#manage-v1)
  - [Self-Hosted V1](#self-hosted-v1)
- [Type Changes](#type-changes)
  - [Agent Think and Speak Types](#agent-think-and-speak-types)
  - [Listen V2 Model Types](#listen-v2-model-types)
  - [Other Removed Generated Types](#other-removed-generated-types)
- [Breaking Changes Summary](#breaking-changes-summary)

## Installation

Upgrade to `0.3.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.3.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Configuration Changes

No changes. Client construction is unchanged between `0.2.x` and `0.3.0`.

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder().build();

DeepgramClient explicitClient = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .build();
```

## Authentication Changes

No changes. API key, access token, and session ID configuration all work the same as in `0.2.x`.

## API Method Changes

### Listen V1 (REST)

No breaking client-method changes. Existing prerecorded transcription code continues to work.

### Listen V2 (WebSocket)

The main breaking change is the `model(...)` type in `V2ConnectOptions`.

**v0.2.x**

```java
wsClient.connect(
    V2ConnectOptions.builder()
        .model("flux-general-en")
        .build());
```

**v0.3.0**

```java
import com.deepgram.types.ListenV2Model;

wsClient.connect(
    V2ConnectOptions.builder()
        .model(ListenV2Model.FLUX_GENERAL_EN)
        .build());
```

If you previously passed a dynamic string, use `ListenV2Model.valueOf(...)`.

```java
ListenV2Model model = ListenV2Model.valueOf("flux-general-en");
```

`sendConfigure(...)` and generated `ListenV2Configure*` types are new in `0.3.0`, but they do not require any migration for existing `0.2.x` code.

### Speak V1

No breaking client-method changes to the REST or WebSocket speak clients.

### Agent V1 (WebSocket)

No core WebSocket client methods were removed, but the generated think/speak configuration types changed significantly.

If your code imported agent-local generated classes from `com.deepgram.resources.agent.v1.types`, you will likely need to update those imports and builders.

`0.3.0` also adds new WebSocket capabilities:

- `sendUpdateThink(...)`
- `onThinkUpdated(...)`
- `onAgentV1History(...)`

These are additive and do not require migration unless you want to use the new events/messages.

### Read V1

No breaking client-method changes.

### Manage V1

No breaking changes to existing management client entry points.

`0.3.0` also adds new reusable voice agent configuration and variable clients under `client.voiceAgent()`.

### Self-Hosted V1

No breaking client-method changes.

## Type Changes

### Agent Think and Speak Types

The largest source-level change in `0.3.0` is a consolidation of agent think/speak schemas into shared top-level types under `com.deepgram.types`.

This mostly affects code that imported types from `com.deepgram.resources.agent.v1.types` directly.

| v0.2.x | v0.3.0 | Import from |
|---|---|---|
| `AgentV1SettingsAgentSpeakEndpoint` | `SpeakSettingsV1` | `com.deepgram.types` |
| `AgentV1SettingsAgentSpeakOneItem` | `SpeakSettingsV1` | `com.deepgram.types` |
| `AgentV1SettingsAgentSpeakEndpointEndpoint` | `SpeakSettingsV1Endpoint` | `com.deepgram.types` |
| `AgentV1SettingsAgentSpeakOneItemEndpoint` | `SpeakSettingsV1Endpoint` | `com.deepgram.types` |
| `AgentV1SettingsAgentSpeakEndpointProvider*` | `SpeakSettingsV1Provider` plus top-level provider models | `com.deepgram.types` |
| `AgentV1SettingsAgentSpeakOneItemProvider*` | `SpeakSettingsV1Provider` plus top-level provider models | `com.deepgram.types` |
| `AgentV1SettingsAgentThinkOneItem` | `ThinkSettingsV1` | `com.deepgram.types` |
| `AgentV1SettingsAgentThinkOneItemProvider` | `ThinkSettingsV1Provider` | `com.deepgram.types` |
| `AgentV1SettingsAgentThinkOneItemContextLength` | `ThinkSettingsV1ContextLength` | `com.deepgram.types` |
| `AgentV1SettingsAgentThinkOneItemEndpoint` | `ThinkSettingsV1Endpoint` | `com.deepgram.types` |
| `AgentV1SettingsAgentThinkOneItemFunctionsItem` | `ThinkSettingsV1FunctionsItem` | `com.deepgram.types` |
| `AgentV1SettingsAgentThinkOneItemFunctionsItemEndpoint` | `ThinkSettingsV1FunctionsItemEndpoint` | `com.deepgram.types` |
| `AgentV1UpdateSpeakSpeakEndpoint` | `SpeakSettingsV1` | `com.deepgram.types` |
| `AgentV1UpdateSpeakSpeakOneItem` | `SpeakSettingsV1` | `com.deepgram.types` |
| `AgentV1UpdateSpeakSpeakEndpointProvider*` | `SpeakSettingsV1Provider` plus top-level provider models | `com.deepgram.types` |
| `AgentV1UpdateSpeakSpeakOneItemProvider*` | `SpeakSettingsV1Provider` plus top-level provider models | `com.deepgram.types` |

In practice, the `AgentV1SettingsAgentThink` and `AgentV1SettingsAgentSpeak` union wrappers still exist, but the values you wrap are now shared `ThinkSettingsV1` and `SpeakSettingsV1` objects.

**v0.2.x**

```java
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeak;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeakEndpoint;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThinkOneItem;

// Old agent-local generated types
AgentV1SettingsAgentSpeak speak = AgentV1SettingsAgentSpeak.of(/* agent-local speak type */);
AgentV1SettingsAgentThink think = AgentV1SettingsAgentThink.of(
    java.util.List.of(/* AgentV1SettingsAgentThinkOneItem */));
```

**v0.3.0**

```java
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeak;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThink;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;

OpenAiThinkProvider provider = OpenAiThinkProvider.builder()
    .model(OpenAiThinkProviderModel.GPT4O_MINI)
    .build();

AgentV1SettingsAgentThink think = AgentV1SettingsAgentThink.of(
    ThinkSettingsV1.builder()
        .provider(ThinkSettingsV1Provider.openAi(provider))
        .prompt("You are a helpful assistant.")
        .build());
```

### Listen V2 Model Types

`V2ConnectOptions.model(...)` is now typed as `ListenV2Model` instead of `String`.

| v0.2.x | v0.3.0 |
|---|---|
| `.model("flux-general-en")` | `.model(ListenV2Model.FLUX_GENERAL_EN)` |
| `.model(dynamicString)` | `.model(ListenV2Model.valueOf(dynamicString))` |

### Other Removed Generated Types

These generated type families were removed or folded into shared top-level types:

- `AgentV1SettingsAgentSpeakEndpoint*`
- `AgentV1SettingsAgentSpeakOneItem*`
- `AgentV1SettingsAgentThinkOneItem*`
- `AgentV1UpdateSpeakSpeakEndpoint*`
- `AgentV1UpdateSpeakSpeakOneItem*`

`0.3.0` also adds new generated types such as:

- `AgentV1History`
- `AgentV1ThinkUpdated`
- `AgentV1UpdateThink`
- `ListenV2Configure*`

## Breaking Changes Summary

### Major Changes

1. **Agent think/speak schemas**: agent-local generated classes were consolidated into shared top-level `ThinkSettingsV1*` and `SpeakSettingsV1*` types.
2. **Listen V2 model typing**: `V2ConnectOptions.model(...)` now takes `ListenV2Model` instead of `String`.
3. **Removed generated helper types**: several agent-specific generated helper families were removed.

### Removed Features

- Agent-local speak schema families such as `AgentV1SettingsAgentSpeakEndpoint*` and `AgentV1SettingsAgentSpeakOneItem*`
- Agent-local think helper families such as `AgentV1SettingsAgentThinkOneItem*`
- Agent-local update-speak schema families such as `AgentV1UpdateSpeakSpeakEndpoint*` and `AgentV1UpdateSpeakSpeakOneItem*`

### New Features in v0.3.0

- **Agent history support**: `AgentV1History` WebSocket events
- **Agent think updates**: `sendUpdateThink(...)` and `AgentV1ThinkUpdated`
- **Listen V2 configure support**: `ListenV2Configure*` support in the V2 WebSocket client
- **Voice agent management APIs**: `client.voiceAgent().configurations()` and `client.voiceAgent().variables()`

### Migration Checklist

- [ ] Upgrade to `com.deepgram:deepgram-java-sdk:0.3.0`
- [ ] Replace any removed agent-local think/speak imports with shared `com.deepgram.types.*` types
- [ ] Update `V2ConnectOptions.model(...)` calls to use `ListenV2Model`
- [ ] If you pass dynamic model strings, wrap them with `ListenV2Model.valueOf(...)`
- [ ] Rebuild your project and fix any remaining imports referencing removed generated classes
