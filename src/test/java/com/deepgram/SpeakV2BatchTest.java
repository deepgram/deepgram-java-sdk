package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.resources.speak.v2.audio.requests.SpeakV2Request;
import com.deepgram.resources.speak.v2.audio.types.AudioGenerateRequestEncoding;
import java.io.InputStream;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hand-written coverage for the Flux TTS batch (REST) endpoint: {@code speak().v2().audio().generate()}
 * (POST /v2/speak).
 *
 * <p>The Fern generator did not emit a wire test for this endpoint, so this fills the gap. It asserts
 * the outgoing request shape (POST /v2/speak, {@code model}/{@code encoding} in the query, {@code text}
 * in the JSON body), that the binary audio response is returned as an {@link InputStream}, and that
 * integer {@code sample_rate}/{@code bit_rate} serialize WITHOUT a decimal — stem parses these as a
 * nonzero u32 and rejects {@code "24000.0"}, so this guards the spec's {@code type: integer} typing
 * against a future regen. Frozen via {@code src/test/} in .fernignore.
 */
class SpeakV2BatchTest {
    private static final byte[] AUDIO = new byte[] {(byte) 0xFF, (byte) 0xFB, (byte) 0x90, 0x00, 0x11, 0x22, 0x33, 0x44};

    private MockWebServer server;
    private DeepgramClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        String base = server.url("/").toString().replaceAll("/$", "");
        Environment env = Environment.custom().base(base).production(base).agent(base).agentRest(base).build();
        client = DeepgramClient.builder().apiKey("test").environment(env).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    private void enqueueAudio() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "audio/mpeg")
                .setBody(new Buffer().write(AUDIO)));
    }

    @Test
    @DisplayName("issues POST /v2/speak with model in query + text in body, and returns the audio bytes")
    void batchRequestShape() throws Exception {
        enqueueAudio();

        InputStream response = client.speak()
                .v2()
                .audio()
                .generate(SpeakV2Request.builder()
                        .model("flux-alexis-en")
                        .text("Hello from the batch endpoint.")
                        .encoding(AudioGenerateRequestEncoding.MP3)
                        .build());
        byte[] audio = response.readAllBytes();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        HttpUrl url = request.getRequestUrl();
        assertThat(url.encodedPath()).isEqualTo("/v2/speak");
        assertThat(url.queryParameter("model")).isEqualTo("flux-alexis-en");
        assertThat(url.queryParameter("encoding")).isEqualTo("mp3");
        assertThat(request.getBody().readUtf8()).contains("\"text\":\"Hello from the batch endpoint.\"");
        assertThat(audio).isEqualTo(AUDIO);
    }

    @Test
    @DisplayName("serializes integer sample_rate/bit_rate without a decimal (guards the type: integer fix)")
    void integerSampleRateAndBitRate() throws Exception {
        enqueueAudio();

        client.speak()
                .v2()
                .audio()
                .generate(SpeakV2Request.builder()
                        .model("flux-alexis-en")
                        .text("hi")
                        .encoding(AudioGenerateRequestEncoding.LINEAR16)
                        .sampleRate(24000)
                        .bitRate(48000)
                        .build())
                .readAllBytes();

        HttpUrl url = server.takeRequest().getRequestUrl();
        assertThat(url.queryParameter("sample_rate")).isEqualTo("24000");
        assertThat(url.queryParameter("bit_rate")).isEqualTo("48000");
        // Must NOT be "24000.0" — stem rejects a non-integer with "expected a nonzero u32".
        assertThat(url.queryParameter("sample_rate")).doesNotContain(".");
        assertThat(url.queryParameter("bit_rate")).doesNotContain(".");
    }
}
