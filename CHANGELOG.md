# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.0.0 (2026-03-26)


### Features

* configure release-please and Maven Central publishing ([f0320c1](https://github.com/deepgram/deepgram-java-sdk/commit/f0320c1cbe9c79fbd52220db1c3850c3ac03eb44))
* initial SDK with full API coverage ([60fe23a](https://github.com/deepgram/deepgram-java-sdk/commit/60fe23ae4b23bb937cb2de878280a3c310bc8098))

## [0.1.0] - Unreleased

### Features

- Initial release of Deepgram Java SDK
- Support for Speech-to-Text (Listen) REST API
- Support for Text-to-Speech (Speak) REST API
- Support for Text Intelligence (Read) API
- Support for Voice Agent configuration API
- Support for Management API (projects, keys, members, usage, billing)
- Support for Auth API (token generation)
- Support for Self-Hosted API (distribution credentials)
- WebSocket support for Listen (real-time streaming transcription)
- WebSocket support for Speak (real-time streaming TTS)
- WebSocket support for Agent (real-time voice agent)
- Synchronous and asynchronous client variants
- Automatic API key loading from DEEPGRAM_API_KEY environment variable
- Configurable timeouts and retry policies
- Custom OkHttp client support
- Raw HTTP response access for all endpoints
- Structured error handling
- Google Java Format enforcement via Spotless

### Notes

- Minimum Java version: 11
- Built on OkHttp 4.12.0 and Jackson 2.18.2
- Generated using Fern SDK generator
- Based on OpenAPI specification from [deepgram-api-specs](https://github.com/deepgram/deepgram-api-specs)
