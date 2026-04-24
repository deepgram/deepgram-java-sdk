---
name: deepgram-java-management-api
description: Use when writing or reviewing Java code in this repo that calls Deepgram Management APIs for projects, project models, API keys, members, invites, usage, and billing. Covers `client.manage().v1().*` plus related think-model discovery under `client.agent().v1().settings().think().models()`. Use `deepgram-java-voice-agent` for live agent conversations instead of admin APIs. Triggers include "management api", "list projects", "api keys", "members", "invites", "usage", "billing", and "models".
---

# Using Deepgram Management API (Java SDK)

Administrative REST APIs for project metadata, project-scoped resources, and model discovery.

## When to use this product

- List or inspect projects.
- Manage project keys, members, invites, usage, or billing.
- Discover public or project-scoped STT/TTS models.

**Use a different skill when:**
- You want to run a live agent session → `deepgram-java-voice-agent`.
- You want speech/text inference rather than project administration → use the product skills for STT, TTS, or Read.

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
String projectId = projects.get(0).getProjectId().orElseThrow();

client.manage().v1().projects().models().list(projectId);
client.manage().v1().projects().keys().list(projectId);
client.manage().v1().projects().members().list(projectId);
client.manage().v1().projects().members().invites().list(projectId);
client.manage().v1().projects().usage().get(projectId);
client.manage().v1().projects().billing().balances().list(projectId);
```

## Key parameters / API surface

- Top-level public models: `client.manage().v1().models().list()` and `.get(modelId)`
- Projects: `projects().list()`, `get(projectId)`, `update(projectId, ...)`, `delete(projectId)`, `leave(projectId)`
- Keys: `projects().keys().list/create/get/delete`
- Members: `projects().members().list/delete`
- Invites: `projects().members().invites().list/create/delete`
- Project models: `projects().models().list(projectId)`
- Usage: `projects().usage().get(projectId)`
- Billing: `projects().billing().balances().list(projectId)`
- Requests: `projects().requests()` subtree exists in the generated API surface
- Agent think-model discovery: `client.agent().v1().settings().think().models().list()`
- Most clients expose `withRawResponse()` alongside typed methods

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

1. **Use an API key, not a temporary JWT, for Manage APIs.** The token-grant endpoint explicitly says those JWTs do not work here.
2. **Some example files are intentionally excluded from Gradle `compileExamples`.** `manage/ListModels.java`, `manage/MemberPermissions.java`, and `manage/UsageBreakdown.java` are currently excluded in `build.gradle`.
3. **Many manage examples are read-only by default.** Create/delete snippets are commented out to avoid destructive calls.
4. **Project-scoped model discovery and global model discovery are different.** `models().list()` returns public models; `projects().models().list(projectId)` returns what a project can use.
5. **This checkout does not expose the Python-style persisted voice-agent configuration client.** Do not promise `voice_agent.configurations.*` here.
6. **The SDK is highly nested.** For invites, the path is `projects().members().invites()`, not a top-level `invites()` client.

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

For cross-language Deepgram product knowledge — the consolidated API reference, documentation finder, focused runnable recipes, third-party integration examples, and MCP setup — install the central skills:

```bash
npx skills add deepgram/skills
```

This SDK ships language-idiomatic code skills; `deepgram/skills` ships cross-language product knowledge (see `api`, `docs`, `recipes`, `examples`, `starters`, `setup-mcp`).
