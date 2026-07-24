package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.resources.speak.v2.websocket.V2ConnectOptions;
import com.deepgram.types.SpeakV2Tag;
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
 * Hand-written connect-handshake wire coverage for the array-valued {@code tag} query param on
 * {@code speak().v2().v2WebSocket().connect(...)} (GET /v2/speak upgrade).
 *
 * <p>Guards that a multi-value {@code tag} ({@code String | List<String>} union) serializes as
 * repeated params ({@code tag=a&tag=b}) rather than a single stringified list ({@code tag=[a, b]}).
 * Mirrors the listen v1/v2 connect wire tests. Frozen via {@code src/test/} in .fernignore.
 */
class SpeakV2ConnectWireTest {
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

    private HttpUrl connectAndCaptureUrl(V2ConnectOptions options) throws Exception {
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.close(1000, null);
            }
        }));
        client.speak().v2().v2WebSocket().connect(options);
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).as("handshake request was sent").isNotNull();
        HttpUrl url = HttpUrl.parse(server.url("/").scheme() + "://" + request.getHeader("Host") + request.getPath());
        assertThat(url).as("parsed handshake URL").isNotNull();
        assertThat(url.encodedPath()).isEqualTo("/v2/speak");
        return url;
    }

    @Test
    @DisplayName("multiple tags are sent as repeated params, not a stringified list")
    void tagListSentAsRepeatedParams() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model("aura-2-thalia-en")
                .tag(SpeakV2Tag.of(List.of("a", "b")))
                .build());

        assertThat(url.queryParameterValues("tag")).containsExactly("a", "b");
    }

    @Test
    @DisplayName("a single string tag is sent as one param")
    void tagStringSentAsOneParam() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model("aura-2-thalia-en")
                .tag(SpeakV2Tag.of("a"))
                .build());

        assertThat(url.queryParameterValues("tag")).containsExactly("a");
    }
}
