---
name: deepgram-java-management-api
description: Use when writing or reviewing Java code in this repo that calls Deepgram Management APIs for projects, project models, API keys, members, invites, usage, and billing. Covers `client.manage().v1().*` plus related think-model discovery under `client.agent().v1().settings().think().models()`. Use `deepgram-java-voice-agent` for live agent conversations instead of admin APIs. Triggers include "management api", "list projects", "api keys", "members", "invites", "usage", "billing", and "models".
---

# Using Deepgram Management API (Java SDK)

Administrative REST APIs for project metadata, project-scoped resources, and model discovery.

**Use a different skill when:**
- Live agent session → `deepgram-java-voice-agent`.
- Speech/text inference → use the STT, TTS, or Read product skills.

## Authentication

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder()
        .apiKey(System.getenv("DEEPGRAM_API_KEY"))
        .build();
```

Management endpoints require API-key auth. Temporary JWTs from `auth().v1().tokens().grant()` do **not** work for Manage APIs.

## Quick start — projects

```java
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import java.util.Collections;
import java.util.List;

ListProjectsV1Response response = client.manage().v1().projects().list();
List<ListProjectsV1ResponseProjectsItem> projects = response.getProjects().orElse(Collections.emptyList());

for (ListProjectsV1ResponseProjectsItem project : projects) {
    System.out.printf("%s (%s)%n",
            project.getName().orElse("unnamed"),
            project.getProjectId().orElse("unknown"));
}
```

## Quick start — project models / keys

```java
if (projects.isEmpty()) {
    throw new IllegalStateException("No Deepgram projects are visible to this API key.");
}
String projectId = projects.get(0).getProjectId().orElseThrow();

client.manage().v1().projects().models().list(projectId);
client.manage().v1().projects().keys().list(projectId);
client.manage().v1().projects().members().list(projectId);
client.manage().v1().projects().members().invites().list(projectId);
client.manage().v1().projects().usage().get(projectId);
client.manage().v1().projects().billing().balances().list(projectId);

```

## Destructive operations — validate-then-act

```java
// 1. Verify the key exists before deleting
try {
    var key = client.manage().v1().projects().keys().get(projectId, keyId);
    // 2. Confirm identity before proceeding
    System.out.printf("Deleting key: %s%n", key.getApiKeyId());
    client.manage().v1().projects().keys().delete(projectId, keyId);
} catch (Exception e) {
    System.err.println("Key not found or delete failed: " + e.getMessage());
}
```

## API surface (all under `client.manage().v1()`)

| Resource | Methods |
|----------|---------|
| `models()` | `list()`, `get(modelId)` |
| `projects()` | `list()`, `get`, `update`, `delete`, `leave` |
| `projects().keys()` | `list`, `create`, `get`, `delete` |
| `projects().members()` | `list`, `delete` |
| `projects().members().invites()` | `list`, `create`, `delete` |
| `projects().models()` | `list(projectId)` |
| `projects().usage()` | `get(projectId)` |
| `projects().billing().balances()` | `list(projectId)` |
| `projects().requests()` | subtree in generated surface |

Also: `client.agent().v1().settings().think().models().list()` for think-model discovery. Most clients expose `withRawResponse()` variants.

## API reference (layered)

1. **In-repo source of truth**: `src/main/java/com/deepgram/resources/manage/v1/`, `src/main/java/com/deepgram/resources/agent/v1/settings/think/models/`, and `examples/manage/`. There is no `reference.md` in this checkout.
2. **Canonical OpenAPI**: https://developers.deepgram.com/openapi.yaml
3. **Context7**: `/llmstxt/developers_deepgram_llms_txt`
4. **Product docs**:
   - https://developers.deepgram.com/reference/manage/projects/list
   - https://developers.deepgram.com/reference/manage/models/list
   - https://developers.deepgram.com/reference/auth/grant-token
   - https://developers.deepgram.com/reference/voice-agent/think-models

## Gotchas

1. **Some example files are excluded from Gradle `compileExamples`** (`ListModels.java`, `MemberPermissions.java`, `UsageBreakdown.java`).
2. **Global vs project-scoped model discovery differ.** `models().list()` returns public models; `projects().models().list(projectId)` returns what a project can use.
3. **No Python-style persisted voice-agent configuration client** in this checkout. Do not promise `voice_agent.configurations.*`.
4. **The SDK is highly nested.** For invites: `projects().members().invites()`, not a top-level `invites()` client.

## Example files in this repo

- `examples/manage/ListProjects.java`
- `examples/manage/ProjectModels.java`
- `examples/manage/ManageKeys.java`
- `examples/manage/ManageMembers.java`
- `examples/manage/ManageInvites.java`
- `examples/manage/GetUsage.java`
- `examples/manage/Billing.java`
- `examples/agent/ListModels.java`

## Central product skills

For cross-language Deepgram product knowledge, install `npx skills add deepgram/skills`.
