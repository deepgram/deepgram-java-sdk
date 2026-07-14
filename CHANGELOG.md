# Changelog

## [0.6.1](https://github.com/deepgram/deepgram-java-sdk/compare/v0.6.0...v0.6.1) (2026-07-14)


### Features

* **regen:** add Flux TTS batch (REST) endpoint and agent latency report ([#75](https://github.com/deepgram/deepgram-java-sdk/issues/75)) ([5bfaf73](https://github.com/deepgram/deepgram-java-sdk/commit/5bfaf739108c5e71350cda601839e2beec4c1579))
* **regen:** add Speak V2 streaming, agent update-listen, and Flux EOT tuning ([#73](https://github.com/deepgram/deepgram-java-sdk/issues/73)) ([a2b15aa](https://github.com/deepgram/deepgram-java-sdk/commit/a2b15aaa1c8e439c4a795fd77f82eaaaa97a0428))

## [0.6.0](https://github.com/deepgram/deepgram-java-sdk/compare/v0.5.0...v0.6.0) (2026-06-26)


### ⚠ BREAKING CHANGES

* (regen) breaking flux timestamps req-double + close-stream/hints; diarize_model, profanity ([#66](https://github.com/deepgram/deepgram-java-sdk/issues/66))

### Features

* (regen) breaking flux timestamps req-double + close-stream/hints; diarize_model, profanity ([#66](https://github.com/deepgram/deepgram-java-sdk/issues/66)) ([a1b9e9a](https://github.com/deepgram/deepgram-java-sdk/commit/a1b9e9a49995248cc5b18770f5ca2a3adf382209))

## [0.5.0](https://github.com/deepgram/deepgram-java-sdk/compare/v0.4.0...v0.5.0) (2026-05-18)


### ⚠ BREAKING CHANGES

* **`Environment` constructor signature changed (3-arg → 4-arg)** ([#57](https://github.com/deepgram/deepgram-java-sdk/issues/57), [#59](https://github.com/deepgram/deepgram-java-sdk/issues/59)). A new `agentRest` slot was added with a `getAgentRestURL()` getter and `Environment.Builder#agentRest(...)` method. `Environment.AGENT` was dropped in favor of the dedicated `agentRest` REST host. Callers building a custom `Environment` via `Environment.custom()...` must add the new `agentRest(...)` builder call.
* **Listen-provider wrapper types deleted** ([#57](https://github.com/deepgram/deepgram-java-sdk/issues/57)). `AgentV1SettingsAgentContextListenProvider{V1, V2, V2LanguageHint}` and `AgentV1SettingsAgentListenProvider{V1, V2}` were removed in favor of the canonical top-level types `DeepgramListenProviderV1`, `DeepgramListenProviderV2`, and `DeepgramListenProviderV2LanguageHint`. The sealed-type wrappers `AgentV1SettingsAgentContextListenProvider` and `AgentV1SettingsAgentListenProvider` remain.

### Features

* **Diarization v2 batch GA** ([#57](https://github.com/deepgram/deepgram-java-sdk/issues/57)) ([fccb3e3](https://github.com/deepgram/deepgram-java-sdk/commit/fccb3e314bdab92b97af790d69e32c18697fb8ba)). New listen/media request types `ListenV1RequestUrl`, `MediaTranscribeRequestOctetStream`, and `MediaTranscribeRequestDiarizeModel`. `AsyncRawMediaClient` and `RawMediaClient` updated to surface the new diarize-model and octet-stream request paths.

### Bug Fixes

* **Route `agent.v1.settings.think.models.list()` to `env.agentRest`** ([#59](https://github.com/deepgram/deepgram-java-sdk/issues/59)) ([f6fd3af](https://github.com/deepgram/deepgram-java-sdk/commit/f6fd3af8d9ac6bbbbbcf5f12813b3045fcb8fbdb)). `RawModelsClient` and `AsyncRawModelsClient` now resolve their base URL via `environment().getAgentRestURL()` instead of `getAgentURL()`. The endpoint was previously broken for any caller; this is a corrective fix paired with the `Environment` breaking change above.

## [0.4.0](https://github.com/deepgram/deepgram-java-sdk/compare/v0.3.0...v0.4.0) (2026-05-06)


### ⚠ BREAKING CHANGES

* sdk regeneration 2026-05-05 ([#49](https://github.com/deepgram/deepgram-java-sdk/issues/49))
* sdk regeneration 2026-04-29 ([#47](https://github.com/deepgram/deepgram-java-sdk/issues/47))

### Features

* sdk regeneration 2026-04-29 ([#47](https://github.com/deepgram/deepgram-java-sdk/issues/47)) ([0519ad3](https://github.com/deepgram/deepgram-java-sdk/commit/0519ad3eb148d696bb33a251cba27ce23df47fcd))
* sdk regeneration 2026-05-05 ([#49](https://github.com/deepgram/deepgram-java-sdk/issues/49)) ([f44678a](https://github.com/deepgram/deepgram-java-sdk/commit/f44678a8ad81e013a1f0aae8cb82d4edceeab658))


### Bug Fixes

* **reconnect:** listener bug fixes + transport factory policy hook ([#45](https://github.com/deepgram/deepgram-java-sdk/issues/45)) ([eac8ad2](https://github.com/deepgram/deepgram-java-sdk/commit/eac8ad27c93bd74e317efa229e6e4b105da8f335))

## [0.3.0](https://github.com/deepgram/deepgram-java-sdk/compare/v0.2.1...v0.3.0) (2026-04-27)


### ⚠ BREAKING CHANGES

* sdk regeneration 2026-04-27 ([#42](https://github.com/deepgram/deepgram-java-sdk/issues/42))

### Features

* sdk regeneration 2026-04-27 ([#42](https://github.com/deepgram/deepgram-java-sdk/issues/42)) ([f1bac65](https://github.com/deepgram/deepgram-java-sdk/commit/f1bac65f09ef0217c6bdc29ddc99dcf7e76dc22f))


### Bug Fixes

* align README and examples with v0.2.1 and validate them in CI ([#34](https://github.com/deepgram/deepgram-java-sdk/issues/34)) ([df02cdc](https://github.com/deepgram/deepgram-java-sdk/commit/df02cdc5968977e708c0449ad11bc52d84c3737c))

## [0.2.1](https://github.com/deepgram/deepgram-java-sdk/compare/v0.2.0...v0.2.1) (2026-04-07)


### Features

* add pluggable transport interface for SageMaker and custom transports ([#29](https://github.com/deepgram/deepgram-java-sdk/issues/29)) ([acecad6](https://github.com/deepgram/deepgram-java-sdk/commit/acecad639f4477c3b14d62e72472a39736662f04))


### Bug Fixes

* add `ClientOptions.java` to `.fernignore` to preserve release-please markers ([#25](https://github.com/deepgram/deepgram-java-sdk/issues/25)) ([69859cf](https://github.com/deepgram/deepgram-java-sdk/commit/69859cf39a8ef9ce7e1e76ff30f8a58111968dec))

## [0.2.0](https://github.com/deepgram/deepgram-java-sdk/compare/v0.1.0...v0.2.0) (2026-03-30)


### ⚠ BREAKING CHANGES

* fern regeneration with exception rename and forward-compatible enums ([#20](https://github.com/deepgram/deepgram-java-sdk/issues/20))
* fern regeneration with com.deepgram package prefix ([#17](https://github.com/deepgram/deepgram-java-sdk/issues/17))

### Features

* fern regeneration with com.deepgram package prefix ([#17](https://github.com/deepgram/deepgram-java-sdk/issues/17)) ([a665131](https://github.com/deepgram/deepgram-java-sdk/commit/a66513101681105d4b87fe7c5a405b874c1d3fdd))
* fern regeneration with exception rename and forward-compatible enums ([#20](https://github.com/deepgram/deepgram-java-sdk/issues/20)) ([8d65065](https://github.com/deepgram/deepgram-java-sdk/commit/8d65065243aab845f9de02e66428613b70bea2d8))


### Bug Fixes

* add release-please version annotations and fix ClientOptions SDK name ([#21](https://github.com/deepgram/deepgram-java-sdk/issues/21)) ([0f56e52](https://github.com/deepgram/deepgram-java-sdk/commit/0f56e52300b2c79f68c9ac045f1f04eb104cbe19))
* switch pom.xml to generic updater for release-please compatibility ([#22](https://github.com/deepgram/deepgram-java-sdk/issues/22)) ([8bff3b9](https://github.com/deepgram/deepgram-java-sdk/commit/8bff3b917c8f8fbcc74d4f2b9b588a2c73ac9e89))

## 0.1.0 (2026-03-27)


### ⚠ BREAKING CHANGES

* initial SDK with full API coverage ([60fe23a](https://github.com/deepgram/deepgram-java-sdk/commit/60fe23ae4b23bb937cb2de878280a3c310bc8098))

### Features

* configure release-please and Maven Central publishing ([f0320c1](https://github.com/deepgram/deepgram-java-sdk/commit/f0320c1cbe9c79fbd52220db1c3850c3ac03eb44))
* enable bump-minor-pre-major for 0.x versioning ([d54dcdd](https://github.com/deepgram/deepgram-java-sdk/commit/d54dcdd18c9395eb4a40007c0b5db885424764a4))

### Bug Fixes

* **changelog:** Removing changelog ([#15](https://github.com/deepgram/deepgram-java-sdk/issues/15)) ([8caef5a](https://github.com/deepgram/deepgram-java-sdk/commit/8caef5ad71d0976246246a14ae693cd62038e27c))
