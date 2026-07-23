package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.resources.listen.v1.websocket.V1ConnectOptions;
import com.deepgram.types.ListenV1Keyterm;
import com.deepgram.types.ListenV1Model;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
 * Hand-written connect-handshake wire coverage for the {@code keyterm} query param on
 * {@code listen().v1().v1WebSocket().connect(...)} (GET /v1/listen upgrade).
 *
 * <p>Guards that a multi-value keyterm serializes as repeated params ({@code keyterm=a&keyterm=b})
 * rather than a single stringified list ({@code keyterm=[a, b]}), which the server would treat as
 * one nonsense term. Frozen via {@code src/test/} in .fernignore.
 */
class ListenV1ConnectWireTest {
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

    private HttpUrl connectAndCaptureUrl(V1ConnectOptions options) throws Exception {
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.close(1000, null);
            }
        }));
        client.listen().v1().v1WebSocket().connect(options);
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).as("handshake request was sent").isNotNull();
        HttpUrl url = HttpUrl.parse(server.url("/").scheme() + "://" + request.getHeader("Host") + request.getPath());
        assertThat(url).as("parsed handshake URL").isNotNull();
        assertThat(url.encodedPath()).isEqualTo("/v1/listen");
        return url;
    }

    @Test
    @DisplayName("multiple keyterms are sent as repeated params, not a stringified list")
    void keytermListSentAsRepeatedParams() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V1ConnectOptions.builder()
                .model(ListenV1Model.NOVA3)
                .keyterm(ListenV1Keyterm.of(List.of("a", "b")))
                .build());

        assertThat(url.queryParameterValues("keyterm")).containsExactly("a", "b");
    }
}
