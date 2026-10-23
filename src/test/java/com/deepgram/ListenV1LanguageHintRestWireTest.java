package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.requests.MediaTranscribeRequestOctetStream;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListenV1LanguageHintRestWireTest {
    private MockWebServer server;
    private DeepgramClient client;
    private AsyncDeepgramClient asyncClient;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        String base = server.url("/").toString().replaceAll("/$", "");
        Environment environment = Environment.custom()
                .base(base)
                .production(base)
                .agent(base)
                .agentRest(base)
                .build();
        client = DeepgramClient.builder().apiKey("test").environment(environment).build();
        asyncClient = AsyncDeepgramClient.builder().apiKey("test").environment(environment).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void sendsRepeatedHintsForEveryUrlAndFileRequestPath() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{}"));
        try {
            client.listen().v1().media().transcribeUrl(ListenV1RequestUrl.builder()
                    .url("https://example.com/audio.wav")
                    .model(MediaTranscribeRequestModel.NOVA3)
                    .languageHint(List.of("en", "es"))
                    .build());
        } catch (RuntimeException ignored) {
            // The response is intentionally an error; this test only captures the outbound URL.
        }
        assertLanguageHints(server.takeRequest(5, TimeUnit.SECONDS));

        server.enqueue(new MockResponse().setResponseCode(500).setBody("{}"));
        try {
            client.listen().v1().media().transcribeFile(MediaTranscribeRequestOctetStream.builder()
                    .body(new byte[] {1, 2, 3})
                    .model(MediaTranscribeRequestModel.NOVA3)
                    .languageHint(List.of("en", "es"))
                    .build());
        } catch (RuntimeException ignored) {
            // The response is intentionally an error; this test only captures the outbound URL.
        }
        assertLanguageHints(server.takeRequest(5, TimeUnit.SECONDS));

        server.enqueue(new MockResponse().setResponseCode(500).setBody("{}"));
        try {
            asyncClient.listen().v1().media().transcribeUrl(ListenV1RequestUrl.builder()
                            .url("https://example.com/audio.wav")
                            .model(MediaTranscribeRequestModel.NOVA3)
                            .languageHint(List.of("en", "es"))
                            .build())
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // The response is intentionally an error; this test only captures the outbound URL.
        }
        assertLanguageHints(server.takeRequest(5, TimeUnit.SECONDS));

        server.enqueue(new MockResponse().setResponseCode(500).setBody("{}"));
        try {
            asyncClient.listen().v1().media().transcribeFile(MediaTranscribeRequestOctetStream.builder()
                            .body(new byte[] {1, 2, 3})
                            .model(MediaTranscribeRequestModel.NOVA3)
                            .languageHint(List.of("en", "es"))
                            .build())
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // The response is intentionally an error; this test only captures the outbound URL.
        }
        assertLanguageHints(server.takeRequest(5, TimeUnit.SECONDS));
    }

    private void assertLanguageHints(RecordedRequest request) {
        assertThat(request).isNotNull();
        assertThat(request.getRequestUrl().queryParameterValues("language_hint")).containsExactly("en", "es");
    }
}
