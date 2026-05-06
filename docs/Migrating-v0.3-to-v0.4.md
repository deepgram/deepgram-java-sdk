# v0.3 to v0.4 Migration Guide

This guide helps you migrate from Deepgram Java SDK v0.3.0 to v0.4.0. The `0.4.0` release is still pre-`1.0`, and it includes breaking source changes from the May 5 SDK regeneration plus a small transport-layer improvement for custom WebSocket transports.

The biggest changes are:

1. Agent settings are now wrapped with `AgentV1SettingsAgent.of(...)`, and the nested settings shape moved into `AgentV1SettingsAgentContext`
2. Agent history and context message helper types were renamed around the new nested `...ContextContext...` schema
3. Management API key creation now uses `CreateKeyV1Request` instead of `CreateKeyV1RequestOne`
4. Agent audio output `container(...)` is now typed as `AgentV1SettingsAudioOutputContainer` instead of raw `String`

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
  - [Agent Settings Types](#agent-settings-types)
  - [Agent History and Context Message Types](#agent-history-and-context-message-types)
  - [Manage Key Request Type](#manage-key-request-type)
  - [Audio Output Types](#audio-output-types)
  - [Other Additive Types](#other-additive-types)
- [Breaking Changes Summary](#breaking-changes-summary)

## Installation

Upgrade to `0.4.0` with Gradle or Maven.

**Gradle**

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.4.0'
}
```

**Maven**

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.4.0</version>
</dependency>
```

## Configuration Changes

No required client-construction changes. Existing `DeepgramClient.builder()` usage still works.

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder().build();

DeepgramClient explicitClient = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .build();
```

If you implement a custom `DeepgramTransportFactory`, `0.4.0` adds a new optional default method:

```java
import com.deepgram.core.ReconnectingWebSocketListener;
import com.deepgram.core.transport.DeepgramTransportFactory;

public final class MyTransportFactory implements DeepgramTransportFactory {
    @Override
    public DeepgramTransport create(String url, Map<String, String> headers) {
        // existing implementation
    }

    @Override
    public ReconnectingWebSocketListener.ReconnectOptions reconnectOptions() {
        return ReconnectingWebSocketListener.ReconnectOptions.builder()
            .maxRetries(0)
            .build();
    }
}
```

This is additive only. Existing `DeepgramTransportFactory` implementations still compile because `reconnectOptions()` has a default implementation.

## Authentication Changes

No changes. API key, access token, and session ID configuration all work the same as in `0.3.0`.

## API Method Changes

### Listen V1 (REST)

No breaking client-method changes.

### Listen V2 (WebSocket)

No breaking client-method changes.

`0.4.0` adds optional language-hint support to `V2ConnectOptions`:

```java
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.types.ListenV2LanguageHint;
import com.deepgram.types.ListenV2Model;

wsClient.connect(V2ConnectOptions.builder()
    .model(ListenV2Model.FLUX_GENERAL_MULTI)
    .languageHint(ListenV2LanguageHint.valueOf("en"))
    .build());
```

`ListenV2ConfigureSuccess` also now exposes `getLanguageHints()`, but this is additive and does not require migration.

### Speak V1

No breaking client-method changes.

`SpeakV1Metadata` now exposes `getAdditionalModelUuids()` and clarifies that `getModelVersion()` / `getModelUuid()` refer to the primary model. This is additive only.

### Agent V1 (WebSocket)

This is the largest migration area in `0.4.0`.

The top-level `AgentV1SettingsAgent` type is no longer a builder-backed object with `language`, `context`, `listen`, `think`, `speak`, and `greeting` fields. It is now a wrapper union that holds either:

- an `AgentV1SettingsAgentContext`
- or an `agent_id` string

That means agent configuration now moves through `AgentV1SettingsAgent.of(...)`.

**v0.3.0**

```java
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThink;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;

OpenAiThinkProvider provider = OpenAiThinkProvider.builder()
    .model(OpenAiThinkProviderModel.GPT4O_MINI)
    .build();

AgentV1Settings settings = AgentV1Settings.builder()
    .agent(AgentV1SettingsAgent.builder()
        .think(AgentV1SettingsAgentThink.of(ThinkSettingsV1.builder()
            .provider(ThinkSettingsV1Provider.openAi(provider))
            .prompt("You are a helpful assistant.")
            .build()))
        .greeting("Hello!")
        .build())
    .build();
```

**v0.4.0**

```java
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextThink;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;

OpenAiThinkProvider provider = OpenAiThinkProvider.builder()
    .model(OpenAiThinkProviderModel.GPT4O_MINI)
    .build();

AgentV1Settings settings = AgentV1Settings.builder()
    .agent(AgentV1SettingsAgent.of(
        AgentV1SettingsAgentContext.builder()
            .think(AgentV1SettingsAgentContextThink.of(ThinkSettingsV1.builder()
                .provider(ThinkSettingsV1Provider.openAi(provider))
                .prompt("You are a helpful assistant.")
                .build()))
            .greeting("Hello!")
            .build()))
    .build();
```

If you pass an existing reusable agent configuration ID instead of inline settings, the new wrapper also supports:

```java
AgentV1SettingsAgent agent = AgentV1SettingsAgent.of("agent_id");
```

`0.4.0` also adds `AgentV1InjectAgentMessageBehavior`, which is additive only:

```java
import com.deepgram.resources.agent.v1.types.AgentV1InjectAgentMessage;
import com.deepgram.resources.agent.v1.types.AgentV1InjectAgentMessageBehavior;

wsClient.sendInjectAgentMessage(AgentV1InjectAgentMessage.builder()
    .message("I will answer after the current turn finishes.")
    .behavior(AgentV1InjectAgentMessageBehavior.QUEUE)
    .build());
```

### Read V1

No breaking client-method changes.

### Manage V1

The main breaking change is the create-key request type rename.

**v0.3.0**

```java
import com.deepgram.types.CreateKeyV1RequestOne;

client.manage().v1().projects().keys().create(
    projectId,
    CreateKeyV1RequestOne.of(Map.of("comment", "example"))
);
```

**v0.4.0**

```java
import com.deepgram.types.CreateKeyV1Request;

client.manage().v1().projects().keys().create(
    projectId,
    CreateKeyV1Request.of(Map.of("comment", "example"))
);
```

`ListProjectMembersV1ResponseMembersItem` also adds `scopes`, `firstName`, and `lastName`, but those are additive only.

### Self-Hosted V1

No breaking client-method changes.

## Type Changes

### Agent Settings Types

The largest source-level change in `0.4.0` is the Agent V1 settings schema restructure.

| v0.3.0 | v0.4.0 | Notes |
|---|---|---|
| `AgentV1SettingsAgent.builder()` | `AgentV1SettingsAgent.of(...)` | top-level agent config is now a wrapper union |
| `AgentV1SettingsAgentThink` | `AgentV1SettingsAgentContextThink` | use inside `AgentV1SettingsAgentContext.builder()` |
| `AgentV1SettingsAgentSpeak` | `AgentV1SettingsAgentContextSpeak` | use inside `AgentV1SettingsAgentContext.builder()` |
| `AgentV1SettingsAgentContext.messages(...)` | `AgentV1SettingsAgentContextContext.messages(...)` | history moved under nested `context` |
| `AgentV1SettingsAgentContextMessagesItem*` | `AgentV1SettingsAgentContextContextMessagesItem*` | renamed around nested `ContextContext` schema |

If your existing `0.3.0` code built inline agent settings directly on `AgentV1SettingsAgent.builder()`, you will need to move those fields into `AgentV1SettingsAgentContext.builder()` and then wrap the result with `AgentV1SettingsAgent.of(...)`.

If your existing code stored message history directly on `AgentV1SettingsAgentContext.builder().messages(...)`, you must now create the nested `AgentV1SettingsAgentContextContext` object.

**v0.3.0**

```java
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;

AgentV1SettingsAgentContext history = AgentV1SettingsAgentContext.builder()
    .messages(List.of(/* old AgentV1SettingsAgentContextMessagesItem values */))
    .build();
```

**v0.4.0**

```java
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextContext;

AgentV1SettingsAgentContext history = AgentV1SettingsAgentContext.builder()
    .context(AgentV1SettingsAgentContextContext.builder()
        .messages(List.of(/* new AgentV1SettingsAgentContextContextMessagesItem values */))
        .build())
    .build();
```

### Agent History and Context Message Types

Several generated helper types were renamed or removed.

| v0.3.0 | v0.4.0 |
|---|---|
| `AgentV1HistoryContent` | `ConversationHistoryMessage` |
| `AgentV1HistoryFunctionCalls` | `FunctionCallHistoryMessage` |
| `AgentV1SettingsAgentContextMessagesItem` | `AgentV1SettingsAgentContextContextMessagesItem` |
| `AgentV1SettingsAgentContextMessagesItemContentRole` | `AgentV1SettingsAgentContextContextMessagesItemContentRole` |
| `AgentV1SettingsAgentContextMessagesItemFunctionCallsFunctionCallsItem` | `AgentV1SettingsAgentContextContextMessagesItemFunctionCallsFunctionCallsItem` |
| `AgentV1SettingsAgentContextMessagesItemContent` | `ConversationHistoryMessage` |
| `AgentV1SettingsAgentContextMessagesItemFunctionCalls` | `FunctionCallHistoryMessage` |

If you reference these generated helper types directly in your own code, update the imports accordingly.

### Manage Key Request Type

`CreateKeyV1RequestOne` was renamed to `CreateKeyV1Request`.

| v0.3.0 | v0.4.0 |
|---|---|
| `CreateKeyV1RequestOne` | `CreateKeyV1Request` |
| `CreateKeyV1RequestOne.of(...)` | `CreateKeyV1Request.of(...)` |

### Audio Output Types

`AgentV1SettingsAudioOutput.container(...)` is now typed as `AgentV1SettingsAudioOutputContainer` instead of raw `String`.

**v0.3.0**

```java
outputBuilder.container("wav");
```

**v0.4.0**

```java
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudioOutputContainer;

outputBuilder.container(AgentV1SettingsAudioOutputContainer.WAV);
```

If you previously passed dynamic string values, use `AgentV1SettingsAudioOutputContainer.valueOf(...)`.

```java
AgentV1SettingsAudioOutputContainer container = AgentV1SettingsAudioOutputContainer.valueOf(dynamicValue);
```

`AgentV1SettingsAudioOutputEncoding` also widens in `0.4.0` to include additional values such as `mp3`, `opus`, `flac`, and `aac`. That change is additive.

### Other Additive Types

`0.4.0` also adds new generated types and fields that do not require migration unless you want to use them:

- `AgentV1InjectAgentMessageBehavior`
- `AgentV1SettingsAgentContextListen*` types
- `ListenV2LanguageHint`
- `OpenAiThinkProviderReasoningMode`
- `GroqThinkProviderReasoningMode`
- `AgentV1ConversationText.getLanguagesHinted()` / `getLanguages()`
- `ListenV2ConfigureSuccess.getLanguageHints()`
- `SpeakV1Metadata.getAdditionalModelUuids()`
- `ListProjectMembersV1ResponseMembersItem.getScopes()`, `getFirstName()`, and `getLastName()`

## Breaking Changes Summary

### Major Changes

1. **Agent settings schema**: inline agent settings now flow through `AgentV1SettingsAgent.of(AgentV1SettingsAgentContext)` instead of `AgentV1SettingsAgent.builder()`.
2. **Nested context history**: `messages` moved under `AgentV1SettingsAgentContextContext`.
3. **Generated type renames**: several agent history/context helper types were renamed or removed.
4. **Manage key request rename**: `CreateKeyV1RequestOne` became `CreateKeyV1Request`.
5. **Audio output container typing**: `container(...)` now takes `AgentV1SettingsAudioOutputContainer`.

### Removed or Renamed Features

- `AgentV1SettingsAgent.builder()`-based inline configuration
- direct `AgentV1SettingsAgentContext.messages(...)` storage
- `AgentV1HistoryContent`
- `AgentV1HistoryFunctionCalls`
- `AgentV1SettingsAgentContextMessagesItemContent`
- `AgentV1SettingsAgentContextMessagesItemFunctionCalls`
- `CreateKeyV1RequestOne`

### New Features in v0.4.0

- **Agent settings schema expansion**: nested context, listen provider variants, and richer voice-agent configuration types
- **Agent inject behavior control**: `AgentV1InjectAgentMessageBehavior`
- **Listen V2 language hints**: `V2ConnectOptions.languageHint(...)` and configure-success language hints
- **Think provider reasoning mode**: OpenAI and Groq reasoning mode support
- **Speak metadata expansion**: `additionalModelUuids`
- **Transport reconnect policy hook**: optional `DeepgramTransportFactory.reconnectOptions()` override for custom transports

### Migration Checklist

- [ ] Upgrade to `com.deepgram:deepgram-java-sdk:0.4.0`
- [ ] Replace `AgentV1SettingsAgent.builder()` usage with `AgentV1SettingsAgent.of(AgentV1SettingsAgentContext.builder()...)`
- [ ] Replace `AgentV1SettingsAgentThink` / `AgentV1SettingsAgentSpeak` usage inside top-level agent settings with `AgentV1SettingsAgentContextThink` / `AgentV1SettingsAgentContextSpeak`
- [ ] Move `AgentV1SettingsAgentContext.messages(...)` into `AgentV1SettingsAgentContextContext.builder().messages(...)`
- [ ] Update any imports that reference renamed agent history/context helper types
- [ ] Replace `CreateKeyV1RequestOne` with `CreateKeyV1Request`
- [ ] Update any `AgentV1SettingsAudioOutput.container("...")` calls to use `AgentV1SettingsAudioOutputContainer`
- [ ] Rebuild your project and fix any remaining imports referencing removed or renamed generated types
