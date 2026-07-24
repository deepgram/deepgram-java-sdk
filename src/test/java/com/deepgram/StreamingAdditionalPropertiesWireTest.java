package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.types.ListenV1Model;
import com.deepgram.types.ListenV2Model;
import com.deepgram.types.SpeakV1Model;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.HttpUrl;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Connect-handshake wire coverage for the {@code additionalProperties} escape hatch on every
 * streaming {@code connect(...)} builder (issue #83).
 *
 * <p>The generated clients build the upgrade URL only from the typed options and drop
 * {@code additionalProperties}, so unmodeled query params (e.g. {@code no_delay}) set via
 * {@code .additionalProperty(key, value)} never reached the wire. Guards that they now do, on
 * listen v1/v2 and speak v1/v2. Frozen via {@code src/test/} in .fernignore.
 */
class StreamingAdditionalPropertiesWireTest {
    private MockWebServer server;
    private DeepgramClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        String base = server.url("/").toString().replaceAll("/$", "");
        Environment env = Environment.custom()
                .base(base)
                .production(base)
                .agent(base)
                .agentRest(base)
                .build();
        client = DeepgramClient.builder().apiKey("test").environment(env).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    private HttpUrl connectAndCaptureUrl(Consumer<MockWebServer> connect, String expectedPath) throws Exception {
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.close(1000, null);
            }
        }));
        connect.accept(server);
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).as("handshake request was sent").isNotNull();
        HttpUrl url = HttpUrl.parse(server.url("/").scheme() + "://" + request.getHeader("Host") + request.getPath());
        assertThat(url).as("parsed handshake URL").isNotNull();
        assertThat(url.encodedPath()).isEqualTo(expectedPath);
        return url;
    }

    private void assertEscapeHatchEmitted(HttpUrl url) {
        // The unmodeled params reached the wire, with a non-string value serialized correctly...
        assertThat(url.queryParameter("no_delay")).isEqualTo("true");
        assertThat(url.queryParameter("custom_key")).isEqualTo("custom_value");
        // ...alongside (not instead of) the typed option.
        assertThat(url.queryParameterNames()).contains("model");
    }

    @Test
    @DisplayName("listen v1: additionalProperties (no_delay) are emitted as query params")
    void listenV1() throws Exception {
        HttpUrl url = connectAndCaptureUrl(
                s -> client.listen()
                        .v1()
                        .v1WebSocket()
                        .connect(com.deepgram.resources.listen.v1.websocket.V1ConnectOptions.builder()
                                .model(ListenV1Model.NOVA3)
                                .additionalProperty("no_delay", true)
                                .additionalProperty("custom_key", "custom_value")
                                .build()),
                "/v1/listen");
        assertEscapeHatchEmitted(url);
    }

    @Test
    @DisplayName("listen v2: additionalProperties (no_delay) are emitted as query params")
    void listenV2() throws Exception {
        HttpUrl url = connectAndCaptureUrl(
                s -> client.listen()
                        .v2()
                        .v2WebSocket()
                        .connect(com.deepgram.resources.listen.v2.websocket.V2ConnectOptions.builder()
                                .model(ListenV2Model.FLUX_GENERAL_EN)
                                .additionalProperty("no_delay", true)
                                .additionalProperty("custom_key", "custom_value")
                                .build()),
                "/v2/listen");
        assertEscapeHatchEmitted(url);
    }

    @Test
    @DisplayName("speak v1: additionalProperties are emitted as query params")
    void speakV1() throws Exception {
        HttpUrl url = connectAndCaptureUrl(
                s -> client.speak()
                        .v1()
                        .v1WebSocket()
                        .connect(com.deepgram.resources.speak.v1.websocket.V1ConnectOptions.builder()
                                .model(SpeakV1Model.AURA2HERA_EN)
                                .additionalProperty("no_delay", true)
                                .additionalProperty("custom_key", "custom_value")
                                .build()),
                "/v1/speak");
        assertEscapeHatchEmitted(url);
    }

    @Test
    @DisplayName("speak v2: additionalProperties are emitted as query params")
    void speakV2() throws Exception {
        HttpUrl url = connectAndCaptureUrl(
                s -> client.speak()
                        .v2()
                        .v2WebSocket()
                        .connect(com.deepgram.resources.speak.v2.websocket.V2ConnectOptions.builder()
                                .model("aura-2-thalia-en")
                                .additionalProperty("no_delay", true)
                                .additionalProperty("custom_key", "custom_value")
                                .build()),
                "/v2/speak");
        assertEscapeHatchEmitted(url);
    }
}
