# Deepgram Java SDK

[![Built with Fern](https://img.shields.io/badge/Built%20with-Fern-brightgreen)](https://buildwithfern.com)
[![Java Version](https://img.shields.io/badge/java-%3E%3D11-blue)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Official Java SDK for [Deepgram](https://www.deepgram.com/)'s automated speech recognition, text-to-speech, and language understanding APIs.

Power your applications with world-class speech and language AI models.

---

## Documentation

You can learn more about the Deepgram API at [developers.deepgram.com](https://developers.deepgram.com).

## Installation

### Gradle

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.1.0'
}
```

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.deepgram</groupId>
    <artifactId>deepgram-java-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quickstart

### Authentication

The SDK supports API Key authentication with automatic environment variable loading:

```java
import com.deepgram.DeepgramClient;

// Using environment variable (DEEPGRAM_API_KEY)
DeepgramClient client = DeepgramClient.builder().build();

// Using API key directly
DeepgramClient client = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .build();
```

Get your API key from the [Deepgram Console](https://console.deepgram.com/).

### Bearer Token Authentication

Use an access token (JWT) for Bearer authentication. When provided, the access token takes precedence over any API key:

```java
// With access token (Bearer auth)
DeepgramClient client = DeepgramClient.builder()
    .accessToken("your-jwt-token")
    .build();
```

### Session ID

Attach a custom session identifier sent as the `x-deepgram-session-id` header with every request and WebSocket connection. If not provided, a UUID is auto-generated:

```java
// With custom session ID
DeepgramClient client = DeepgramClient.builder()
    .apiKey("your-api-key")
    .sessionId("my-session-123")
    .build();
```

## Features

### Speech-to-Text (Listen)

Transcribe pre-recorded audio from files or URLs.

```java
import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;

DeepgramClient client = DeepgramClient.builder().build();

// Transcribe from URL
MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(
    ListenV1RequestUrl.builder()
        .url("https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav")
        .build()
);

// Access transcription (MediaTranscribeResponse is a union type)
result.visit(new MediaTranscribeResponse.Visitor<Void>() {
    @Override
    public Void visit(ListenV1Response response) {
        response.getResults().getChannels().get(0)
            .getAlternatives().ifPresent(alts -> {
                alts.get(0).getTranscript().ifPresent(System.out::println);
            });
        return null;
    }

    @Override
    public Void visit(ListenV1AcceptedResponse accepted) {
        System.out.println("Request accepted (callback mode)");
        return null;
    }
});
```

#### Transcribe from File

```java
import java.nio.file.Files;
import java.nio.file.Path;

byte[] audioData = Files.readAllBytes(Path.of("audio.wav"));
MediaTranscribeResponse result = client.listen().v1().media().transcribeFile(audioData);
```

### Text-to-Speech (Speak)

Convert text to natural-sounding speech.

```java
import com.deepgram.DeepgramClient;
import com.deepgram.resources.speak.v1.audio.requests.SpeakV1Request;
import java.io.InputStream;

DeepgramClient client = DeepgramClient.builder().build();

// Generate speech audio
InputStream audioStream = client.speak().v1().audio().generate(
    SpeakV1Request.builder()
        .text("Hello, world! Welcome to Deepgram.")
        .build()
);

// Write audio to file or play it
```

### Text Intelligence (Read)

Analyze text for sentiment, topics, summaries, and intents.

```java
import com.deepgram.DeepgramClient;
import com.deepgram.resources.read.v1.text.requests.TextAnalyzeRequest;
import com.deepgram.types.ReadV1Request;
import com.deepgram.types.ReadV1RequestText;
import com.deepgram.types.ReadV1Response;

DeepgramClient client = DeepgramClient.builder().build();

ReadV1Response result = client.read().v1().text().analyze(
    TextAnalyzeRequest.builder()
        .body(ReadV1Request.of(
            ReadV1RequestText.builder()
                .text("Deepgram's speech recognition is incredibly accurate and fast.")
                .build()))
        .sentiment(true)
        .language("en")
        .build()
);
```

### Management

Manage projects, API keys, members, usage, and billing.

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder().build();

// List projects
var projects = client.manage().v1().projects().list();

// List API keys for a project
var keys = client.manage().v1().projects().keys().list("project-id");

// Get usage statistics
var usage = client.manage().v1().projects().usage().get("project-id");
```

### Voice Agent

Manage voice agent configurations and models.

```java
import com.deepgram.DeepgramClient;

DeepgramClient client = DeepgramClient.builder().build();

// List available agent think models
var models = client.agent().v1().settings().think().models().list();
```

## WebSocket APIs

The SDK includes built-in WebSocket clients for real-time streaming.

### Live Transcription (Listen WebSocket)

Stream audio for real-time speech-to-text.

```java
import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.websocket.V1WebSocketClient;
import com.deepgram.resources.listen.v1.websocket.V1ConnectOptions;
import com.deepgram.types.ListenV1Model;

DeepgramClient client = DeepgramClient.builder().build();

V1WebSocketClient ws = client.listen().v1().websocket();

// Register event handlers
ws.onResults(results -> {
    String transcript = results.getChannel()
        .getAlternatives().get(0)
        .getTranscript();
    System.out.println("Transcript: " + transcript);
});

ws.onMetadata(metadata -> {
    System.out.println("Metadata received");
});

ws.onError(error -> {
    System.err.println("Error: " + error.getMessage());
});

// Connect with options (model is required)
ws.connect(V1ConnectOptions.builder()
    .model(ListenV1Model.NOVA3)
    .build());

ws.send(audioBytes);

// Close when done
ws.close();
```

### Text-to-Speech Streaming (Speak WebSocket)

Stream text for real-time audio generation.

```java
import com.deepgram.DeepgramClient;
import com.deepgram.resources.speak.v1.websocket.V1WebSocketClient;

DeepgramClient client = DeepgramClient.builder().build();

var ttsWs = client.speak().v1().websocket();

// Register event handlers
ttsWs.onAudioData(audioData -> {
    // Process audio chunks as they arrive
});

ttsWs.onMetadata(metadata -> {
    System.out.println("Metadata received");
});

ttsWs.onError(error -> {
    System.err.println("Error: " + error.getMessage());
});

// Connect and send text
ttsWs.connect();
ttsWs.send("Hello, this is streamed text-to-speech.");

// Close when done
ttsWs.close();
```

### Agent WebSocket

Connect to Deepgram's voice agent for real-time conversational AI.

```java
import com.deepgram.DeepgramClient;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;

DeepgramClient client = DeepgramClient.builder().build();

var agentWs = client.agent().v1().websocket();

// Register event handlers
agentWs.onWelcome(welcome -> {
    System.out.println("Agent connected");
});

agentWs.onError(error -> {
    System.err.println("Error: " + error.getMessage());
});

// Connect and interact
agentWs.connect();
agentWs.send(audioBytes);

// Close when done
agentWs.close();
```

## Custom Transports

The SDK supports pluggable transports for routing WebSocket connections through alternative infrastructure. Any class implementing `DeepgramTransportFactory` can replace the default OkHttp WebSocket connection.

This is primarily used for **AWS SageMaker** deployments where Deepgram models run on your own SageMaker endpoints.

### SageMaker

Use the separate [`deepgram-sagemaker`](https://github.com/deepgram/deepgram-java-sdk-transport-sagemaker) package to route audio through a SageMaker endpoint:

```groovy
dependencies {
    implementation 'com.deepgram:deepgram-java-sdk:0.2.0'
    implementation 'com.deepgram:deepgram-sagemaker:0.1.0'
}
```

```java
import com.deepgram.sagemaker.SageMakerConfig;
import com.deepgram.sagemaker.SageMakerTransportFactory;

var factory = new SageMakerTransportFactory(
    SageMakerConfig.builder()
        .endpointName("my-deepgram-endpoint")
        .region("us-west-2")
        .build()
);

DeepgramClient client = DeepgramClient.builder()
    .apiKey("unused")  // SageMaker uses AWS credentials, not Deepgram API keys
    .transportFactory(factory)
    .build();

// Use the SDK exactly as normal — the transport is transparent
var ws = client.listen().v1().websocket();
ws.onResults(results -> { /* ... */ });
ws.connect(V1ConnectOptions.builder().model(ListenV1Model.NOVA3).build());
ws.sendMedia(audioBytes);
```

See the [SageMaker example](examples/sagemaker/LiveStreamingSageMaker.java) for a complete walkthrough.

### Custom Transport Implementation

To implement your own transport (e.g. for proxies, test doubles, or other infrastructure):

```java
import com.deepgram.core.transport.DeepgramTransport;
import com.deepgram.core.transport.DeepgramTransportFactory;

DeepgramTransportFactory myFactory = (url, headers) -> {
    // Return your DeepgramTransport implementation
    return new MyCustomTransport(url, headers);
};

DeepgramClient client = DeepgramClient.builder()
    .apiKey("your-key")
    .transportFactory(myFactory)
    .build();
```

The `DeepgramTransport` interface provides bidirectional messaging: `sendText()`, `sendBinary()`, and callback registration for incoming messages, errors, and close events.

## Configuration

### Custom Timeouts

```java
DeepgramClient client = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .timeout(30)  // 30 seconds
    .build();
```

### Retry Configuration

```java
DeepgramClient client = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .maxRetries(3)
    .build();
```

### Custom HTTP Client

> **Note:** When providing a custom `OkHttpClient`, the SDK's built-in retry interceptor is not added automatically. Add your own retry logic if needed.

```java
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

OkHttpClient httpClient = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build();

DeepgramClient client = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .httpClient(httpClient)
    .build();
```

### Custom Base URL

For on-premises deployments or custom endpoints:

```java
import com.deepgram.core.Environment;

DeepgramClient client = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .environment(Environment.custom()
        .base("https://your-custom-endpoint.com")
        .build())
    .build();
```

### Environment Variable

Set your API key as an environment variable to avoid passing it in code:

```bash
export DEEPGRAM_API_KEY="your-api-key-here"
```

```java
// API key is loaded automatically from DEEPGRAM_API_KEY
DeepgramClient client = DeepgramClient.builder().build();
```

### Custom Headers

```java
DeepgramClient client = DeepgramClient.builder()
    .apiKey("YOUR_DEEPGRAM_API_KEY")
    .addHeader("X-Custom-Header", "custom-value")
    .build();
```

## Async Client

The SDK provides a fully asynchronous client for non-blocking operations:

```java
import com.deepgram.AsyncDeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import java.util.concurrent.CompletableFuture;

AsyncDeepgramClient asyncClient = AsyncDeepgramClient.builder().build();

// Async transcription
CompletableFuture<MediaTranscribeResponse> future = asyncClient.listen().v1().media()
    .transcribeUrl(ListenV1RequestUrl.builder()
        .url("https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav")
        .build());

future.thenAccept(result -> {
    System.out.println("Transcription complete!");
});
```

## Error Handling

The SDK provides structured error handling:

```java
import com.deepgram.errors.BadRequestError;

try {
    var result = client.listen().v1().media().transcribeUrl(
        ListenV1RequestUrl.builder()
            .url("https://example.com/audio.mp3")
            .build()
    );
} catch (BadRequestError e) {
    System.err.println("Bad request: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
}
```

### Raw Response Access

All client methods support raw response access via the raw client:

```java
import com.deepgram.core.DeepgramApiHttpResponse;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;

// Access raw HTTP response
DeepgramApiHttpResponse<MediaTranscribeResponse> rawResponse =
    client.listen().v1().media().withRawResponse().transcribeUrl(request);

int statusCode = rawResponse.statusCode();
var headers = rawResponse.headers();
MediaTranscribeResponse body = rawResponse.body();
```

## Complete SDK Reference

The SDK provides comprehensive access to Deepgram's APIs:

### Listen (Speech-to-Text)
```java
client.listen().v1().media().transcribeUrl(request)    // Transcribe audio from URL
client.listen().v1().media().transcribeFile(body)      // Transcribe audio from file bytes
client.listen().v1().websocket()                       // Real-time streaming transcription
```

### Speak (Text-to-Speech)
```java
client.speak().v1().audio().generate(request)          // Generate speech from text
client.speak().v1().websocket()                        // Real-time streaming TTS
```

### Read (Text Intelligence)
```java
client.read().v1().text().analyze(request)             // Analyze text content
```

### Agent (Voice Agent)
```java
client.agent().v1().settings().think().models().list() // List available agent models
client.agent().v1().websocket()                        // Real-time agent WebSocket
```

### Manage (Project Management)
```java
// Projects
client.manage().v1().projects().list()                 // List all projects
client.manage().v1().projects().get(projectId)         // Get project details
client.manage().v1().projects().update(projectId, req) // Update project
client.manage().v1().projects().delete(projectId)      // Delete project

// API Keys
client.manage().v1().projects().keys().list(projectId) // List API keys
client.manage().v1().projects().keys().create(id, req) // Create new key
client.manage().v1().projects().keys().get(id, keyId)  // Get key details
client.manage().v1().projects().keys().delete(id, key) // Delete key

// Members
client.manage().v1().projects().members().list(id)     // List members
client.manage().v1().projects().members().delete(p, m) // Remove member

// Usage
client.manage().v1().projects().usage().get(projectId) // Get usage summary

// Models
client.manage().v1().projects().models().list(id)      // List project models
client.manage().v1().models().list()                   // List all models
```

### Auth (Authentication)
```java
client.auth().v1().tokens().grant(request)             // Generate access token
```

### Self-Hosted
```java
client.selfHosted().v1().distributionCredentials().list(id)   // List credentials
client.selfHosted().v1().distributionCredentials().create(req) // Create credentials
client.selfHosted().v1().distributionCredentials().get(p, id)  // Get credentials
client.selfHosted().v1().distributionCredentials().delete(p,i) // Delete credentials
```

## Development

### Requirements

- Java 11 or higher (Java 17 recommended for development)
- Gradle (wrapper included)
- Deepgram API key ([sign up](https://console.deepgram.com/signup))

### Running Tests

```bash
# Unit tests
./gradlew unitTest

# Integration tests (requires DEEPGRAM_API_KEY)
./gradlew integrationTest

# All tests
./gradlew test
```

### Using Make

```bash
make check             # lint + build + unit tests
make test-integration  # integration tests only
make test-all          # full test suite
make format            # auto-format code
```

## API Reference

See [reference.md](reference.md) for complete API documentation.

## Getting Help

- [Documentation](https://developers.deepgram.com)
- [Community Discord](https://discord.gg/deepgram)
- [Support](mailto:support@deepgram.com)
- [Report Issues](https://github.com/deepgram/deepgram-java-sdk/issues)

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## Community Code of Conduct

Please see our community [code of conduct](https://developers.deepgram.com/code-of-conduct) before contributing to this project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Built by [Deepgram](https://www.deepgram.com)
