package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deepgram.core.ClientOptions;
import com.deepgram.core.Environment;
import java.util.concurrent.CompletableFuture;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for the DeepgramClientBuilder class and related client configuration. */
class ClientBuilderTest {

    @Nested
    @DisplayName("API key configuration")
    class ApiKeyConfiguration {

        @Test
        @DisplayName("builds client with explicit API key")
        void testExplicitApiKey() {
            DeepgramClient client =
                    DeepgramClient.builder().apiKey("test-api-key-123").build();

            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("throws when API key is null and env var not set")
        void testNullApiKeyThrows() {
            // When API key is explicitly set to null, build() should throw
            assertThatThrownBy(() -> DeepgramClient.builder().apiKey(null).build())
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Environment configuration")
    class EnvironmentConfiguration {

        @Test
        @DisplayName("defaults to PRODUCTION environment")
        void testDefaultEnvironment() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("accepts an agent-shaped environment")
        void testAgentEnvironment() {
            Environment agent = Environment.custom()
                    .base("https://agent.deepgram.com")
                    .agent("wss://agent.deepgram.com")
                    .production("wss://api.deepgram.com")
                    .agentRest("https://agent.deepgram.com")
                    .build();

            DeepgramClient client = DeepgramClient.builder()
                    .apiKey("test-key")
                    .environment(agent)
                    .build();

            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("accepts custom environment")
        void testCustomEnvironment() {
            Environment custom = Environment.custom()
                    .base("https://custom.example.com")
                    .agent("wss://agent.example.com")
                    .production("wss://custom.example.com")
                    .build();

            DeepgramClient client = DeepgramClient.builder()
                    .apiKey("test-key")
                    .environment(custom)
                    .build();

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Timeout configuration")
    class TimeoutConfiguration {

        @Test
        @DisplayName("builds client with custom timeout")
        void testCustomTimeout() {
            DeepgramClient client =
                    DeepgramClient.builder().apiKey("test-key").timeout(120).build();

            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("builds client with default timeout when not specified")
        void testDefaultTimeout() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Max retries configuration")
    class MaxRetriesConfiguration {

        @Test
        @DisplayName("builds client with custom max retries")
        void testCustomMaxRetries() {
            DeepgramClient client =
                    DeepgramClient.builder().apiKey("test-key").maxRetries(5).build();

            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("builds client with zero retries")
        void testZeroRetries() {
            DeepgramClient client =
                    DeepgramClient.builder().apiKey("test-key").maxRetries(0).build();

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("HTTP client configuration")
    class HttpClientConfiguration {

        @Test
        @DisplayName("builds client with custom OkHttpClient")
        void testCustomHttpClient() {
            OkHttpClient customHttpClient = new OkHttpClient.Builder().build();

            DeepgramClient client = DeepgramClient.builder()
                    .apiKey("test-key")
                    .httpClient(customHttpClient)
                    .build();

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Client lifecycle")
    class ClientLifecycle {
        @Test
        @DisplayName("closing the default client releases SDK-owned HTTP resources")
        void closesDefaultClientResources() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            client.close();

            assertThat(client.clientOptions
                            .httpClient()
                            .dispatcher()
                            .executorService()
                            .isShutdown())
                    .isTrue();
        }

        @Test
        @DisplayName("closing the default async client releases SDK-owned HTTP resources")
        void closesDefaultAsyncClientResources() {
            AsyncDeepgramClient client =
                    AsyncDeepgramClient.builder().apiKey("test-key").build();

            client.close();

            assertThat(client.clientOptions
                            .httpClient()
                            .dispatcher()
                            .executorService()
                            .isShutdown())
                    .isTrue();
        }

        @Test
        @DisplayName("closing a client does not release caller-owned HTTP resources")
        void doesNotCloseCustomClientResources() {
            OkHttpClient customHttpClient = new OkHttpClient.Builder().build();
            DeepgramClient client = DeepgramClient.builder()
                    .apiKey("test-key")
                    .httpClient(customHttpClient)
                    .build();

            try {
                client.close();

                assertThat(customHttpClient.dispatcher().executorService().isShutdown())
                        .isFalse();
            } finally {
                customHttpClient.dispatcher().executorService().shutdown();
                customHttpClient.connectionPool().evictAll();
            }
        }

        @Test
        @DisplayName("closing an async client does not release caller-owned HTTP resources")
        void doesNotCloseCustomAsyncClientResources() {
            OkHttpClient customHttpClient = new OkHttpClient.Builder().build();
            AsyncDeepgramClient client = AsyncDeepgramClient.builder()
                    .apiKey("test-key")
                    .httpClient(customHttpClient)
                    .build();

            try {
                client.close();

                assertThat(customHttpClient.dispatcher().executorService().isShutdown())
                        .isFalse();
            } finally {
                customHttpClient.dispatcher().executorService().shutdown();
                customHttpClient.connectionPool().evictAll();
            }
        }

        @Test
        @DisplayName("disconnecting an unconnected WebSocket is safe")
        void disconnectsUnconnectedWebSockets() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            try {
                assertThatCode(() -> client.listen().v1().v1WebSocket().disconnect())
                        .doesNotThrowAnyException();
                assertThatCode(() -> client.listen().v2().v2WebSocket().disconnect())
                        .doesNotThrowAnyException();
                assertThatCode(() -> client.speak().v1().v1WebSocket().disconnect())
                        .doesNotThrowAnyException();
                assertThatCode(() -> client.speak().v2().v2WebSocket().disconnect())
                        .doesNotThrowAnyException();
                assertThatCode(() -> client.agent().v1().v1WebSocket().disconnect())
                        .doesNotThrowAnyException();
            } finally {
                client.close();
            }
        }

        @Test
        @DisplayName("disconnect prevents a WebSocket from connecting later")
        void disconnectPreventsLaterConnections() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            try {
                var listenV1 = client.listen().v1().v1WebSocket();
                listenV1.disconnect();
                assertConnectionRejected(
                        listenV1.connect(com.deepgram.resources.listen.v1.websocket.V1ConnectOptions.builder()
                                .model(com.deepgram.types.ListenV1Model.NOVA3)
                                .build()));

                var listenV2 = client.listen().v2().v2WebSocket();
                listenV2.disconnect();
                assertConnectionRejected(
                        listenV2.connect(com.deepgram.resources.listen.v2.websocket.V2ConnectOptions.builder()
                                .model(com.deepgram.types.ListenV2Model.FLUX_GENERAL_EN)
                                .build()));

                var speakV1 = client.speak().v1().v1WebSocket();
                speakV1.disconnect();
                assertConnectionRejected(speakV1.connect());

                var speakV2 = client.speak().v2().v2WebSocket();
                speakV2.disconnect();
                assertConnectionRejected(
                        speakV2.connect(com.deepgram.resources.speak.v2.websocket.V2ConnectOptions.builder()
                                .model("flux-alexis-en")
                                .build()));

                var agentV1 = client.agent().v1().v1WebSocket();
                agentV1.disconnect();
                assertConnectionRejected(agentV1.connect());
            } finally {
                client.close();
            }
        }

        @Test
        @DisplayName("disconnect completes an in-flight connection exceptionally")
        void disconnectCompletesInFlightConnection() {
            ClientOptions options = ClientOptions.builder()
                    .environment(Environment.PRODUCTION)
                    .webSocketFactory((request, listener) -> new UnopenedWebSocket())
                    .build();
            var socket = new com.deepgram.resources.listen.v2.websocket.V2WebSocketClient(options);

            try {
                CompletableFuture<Void> connection =
                        socket.connect(com.deepgram.resources.listen.v2.websocket.V2ConnectOptions.builder()
                                .model(com.deepgram.types.ListenV2Model.FLUX_GENERAL_EN)
                                .build());

                socket.disconnect();

                assertConnectionRejected(connection);
            } finally {
                options.httpClient().dispatcher().executorService().shutdown();
                options.httpClient().connectionPool().evictAll();
            }
        }
    }

    private static void assertConnectionRejected(CompletableFuture<Void> connection) {
        assertThatThrownBy(connection::join)
                .isInstanceOf(java.util.concurrent.CompletionException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    private static final class UnopenedWebSocket implements WebSocket {
        @Override
        public Request request() {
            return new Request.Builder().url("ws://localhost/").build();
        }

        @Override
        public long queueSize() {
            return 0;
        }

        @Override
        public boolean send(String text) {
            return false;
        }

        @Override
        public boolean send(ByteString bytes) {
            return false;
        }

        @Override
        public boolean close(int code, String reason) {
            return true;
        }

        @Override
        public void cancel() {}
    }

    @Nested
    @DisplayName("Custom headers configuration")
    class CustomHeadersConfiguration {

        @Test
        @DisplayName("builds client with custom headers")
        void testCustomHeaders() {
            DeepgramClient client = DeepgramClient.builder()
                    .apiKey("test-key")
                    .addHeader("X-Custom-Header", "custom-value")
                    .addHeader("X-Request-ID", "test-123")
                    .build();

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Method chaining")
    class MethodChaining {

        @Test
        @DisplayName("supports fluent builder pattern with all options")
        void testFluentBuilder() {
            DeepgramClient client = DeepgramClient.builder()
                    .apiKey("test-key")
                    .environment(Environment.PRODUCTION)
                    .timeout(30)
                    .maxRetries(3)
                    .addHeader("X-Custom", "value")
                    .build();

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Client sub-resources")
    class ClientSubResources {

        @Test
        @DisplayName("listen() returns a non-null ListenClient")
        void testListenClient() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.listen()).isNotNull();
        }

        @Test
        @DisplayName("speak() returns a non-null SpeakClient")
        void testSpeakClient() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.speak()).isNotNull();
        }

        @Test
        @DisplayName("read() returns a non-null ReadClient")
        void testReadClient() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.read()).isNotNull();
        }

        @Test
        @DisplayName("manage() returns a non-null ManageClient")
        void testManageClient() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.manage()).isNotNull();
        }

        @Test
        @DisplayName("agent() returns a non-null AgentClient")
        void testAgentClient() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.agent()).isNotNull();
        }

        @Test
        @DisplayName("auth() returns a non-null AuthClient")
        void testAuthClient() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.auth()).isNotNull();
        }

        @Test
        @DisplayName("selfHosted() returns a non-null SelfHostedClient")
        void testSelfHostedClient() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.selfHosted()).isNotNull();
        }

        @Test
        @DisplayName("sub-resources are memoized (same instance returned)")
        void testSubResourceMemoization() {
            DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

            assertThat(client.listen()).isSameAs(client.listen());
            assertThat(client.speak()).isSameAs(client.speak());
            assertThat(client.read()).isSameAs(client.read());
            assertThat(client.manage()).isSameAs(client.manage());
        }
    }
}
