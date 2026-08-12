package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.types.ListenV2Keyterm;
import com.deepgram.types.ListenV2LanguageHint;
import com.deepgram.types.ListenV2Model;
import com.deepgram.types.ListenV2Numerals;
import com.deepgram.types.ListenV2Redact;
import com.deepgram.types.ListenV2Tag;
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
 * Hand-written connect-handshake wire coverage for Flux STT connect query params ({@code numerals}
 * plus the array-valued {@code keyterm}, {@code tag}, and {@code language_hint}) on
 * {@code listen().v2().v2WebSocket().connect(...)} (GET /v2/listen upgrade).
 *
 * <p>The Fern generator did not emit a wire test for the /v2/listen handshake, so this fills the
 * gap for the 2026-07-20 regen's new {@code numerals} option: it pins that {@code numerals=true}
 * lands on the connect URL when set, and is omitted entirely when absent. {@code numerals} is a
 * {@link ListenV2Numerals} whose {@code @JsonValue toString()} is the raw wire string, so this also
 * guards that {@code String.valueOf(...)} serialization stays {@code "true"}/{@code "false"} rather
 * than an enum name. Frozen via {@code src/test/} in .fernignore.
 */
class ListenV2ConnectWireTest {
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

    /** Drives a real connect() against MockWebServer and returns the parsed handshake URL. */
    private HttpUrl connectAndCaptureUrl(V2ConnectOptions options) throws Exception {
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.close(1000, null);
            }
        }));
        client.listen().v2().v2WebSocket().connect(options);
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).as("handshake request was sent").isNotNull();
        HttpUrl url = HttpUrl.parse(server.url("/").scheme() + "://" + request.getHeader("Host") + request.getPath());
        assertThat(url).as("parsed handshake URL").isNotNull();
        assertThat(url.encodedPath()).isEqualTo("/v2/listen");
        return url;
    }

    @Test
    @DisplayName("numerals=true is sent on the connect URL when set")
    void numeralsPresentWhenSet() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model(ListenV2Model.FLUX_GENERAL_EN)
                .numerals(ListenV2Numerals.TRUE)
                .build());

        assertThat(url.queryParameter("numerals")).isEqualTo("true");
    }

    @Test
    @DisplayName("numerals is omitted from the connect URL when not set")
    void numeralsOmittedWhenAbsent() throws Exception {
        HttpUrl url = connectAndCaptureUrl(
                V2ConnectOptions.builder().model(ListenV2Model.FLUX_GENERAL_EN).build());

        assertThat(url.queryParameterNames()).doesNotContain("numerals");
    }

    @Test
    @DisplayName("multiple keyterms are sent as repeated params, not a stringified list")
    void keytermListSentAsRepeatedParams() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model(ListenV2Model.FLUX_GENERAL_EN)
                .keyterm(ListenV2Keyterm.of(List.of("a", "b")))
                .build());

        assertThat(url.queryParameterValues("keyterm")).containsExactly("a", "b");
    }

    @Test
    @DisplayName("a single string keyterm is sent as one param")
    void keytermStringSentAsOneParam() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model(ListenV2Model.FLUX_GENERAL_EN)
                .keyterm(ListenV2Keyterm.of("a"))
                .build());

        assertThat(url.queryParameterValues("keyterm")).containsExactly("a");
    }

    @Test
    @DisplayName("multiple tags are sent as repeated params")
    void tagListSentAsRepeatedParams() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model(ListenV2Model.FLUX_GENERAL_EN)
                .tag(ListenV2Tag.of(List.of("a", "b")))
                .build());

        assertThat(url.queryParameterValues("tag")).containsExactly("a", "b");
    }

    @Test
    @DisplayName("multiple language hints are sent as repeated params")
    void languageHintListSentAsRepeatedParams() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model(ListenV2Model.FLUX_GENERAL_EN)
                .languageHint(ListenV2LanguageHint.of(List.of("en", "es")))
                .build());

        assertThat(url.queryParameterValues("language_hint")).containsExactly("en", "es");
    }

    @Test
    @DisplayName("a single string language hint is sent as one param")
    void languageHintStringSentAsOneParam() throws Exception {
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model(ListenV2Model.FLUX_GENERAL_EN)
                .languageHint(ListenV2LanguageHint.of("en"))
                .build());

        assertThat(url.queryParameterValues("language_hint")).containsExactly("en");
    }

    @Test
    @DisplayName("redact is sent on the connect URL as its wire value when set")
    void redactPresentWhenSet() throws Exception {
        // redact is a new (2026-08-11 regen) single-value Flux STT connect param backed by the
        // ListenV2Redact enum, whose @JsonValue toString() is the raw wire string. Pin that it
        // serializes as "numbers" (not the enum constant name) and lands on the connect URL.
        HttpUrl url = connectAndCaptureUrl(V2ConnectOptions.builder()
                .model(ListenV2Model.FLUX_GENERAL_EN)
                .redact(ListenV2Redact.NUMBERS)
                .build());

        assertThat(url.queryParameter("redact")).isEqualTo("numbers");
    }

    @Test
    @DisplayName("redact is omitted from the connect URL when not set")
    void redactOmittedWhenAbsent() throws Exception {
        HttpUrl url = connectAndCaptureUrl(
                V2ConnectOptions.builder().model(ListenV2Model.FLUX_GENERAL_EN).build());

        assertThat(url.queryParameterNames()).doesNotContain("redact");
    }
}
