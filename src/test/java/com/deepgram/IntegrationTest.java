package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deepgram.core.DeepgramHttpException;
import com.deepgram.core.Environment;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.requests.MediaTranscribeRequestOctetStream;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.resources.read.v1.text.requests.TextAnalyzeRequest;
import com.deepgram.resources.speak.v1.audio.requests.SpeakV1Request;
import com.deepgram.resources.speak.v2.types.SpeakV2Close;
import com.deepgram.resources.speak.v2.types.SpeakV2Flush;
import com.deepgram.resources.speak.v2.types.SpeakV2Speak;
import com.deepgram.resources.speak.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.speak.v2.websocket.V2WebSocketClient;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ReadV1Request;
import com.deepgram.types.ReadV1RequestText;
import com.deepgram.types.ReadV1Response;
import com.deepgram.types.SpeakV2Encoding;
import com.deepgram.types.SpeakV2SampleRate;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the Deepgram Java SDK. These tests require a valid DEEPGRAM_API_KEY environment variable and
 * make real API calls to Deepgram services.
 *
 * <p>Tests are organized into tiers: Tier 1 tests must pass before release; Tier 2 tests should pass but are less
 * critical.
 */
public class IntegrationTest {

    private static final String TEST_AUDIO_URL = "https://dpgr.am/spacewalk.wav";

    private DeepgramClient client;
    private String apiKey;

    @BeforeEach
    void setUp() {
        apiKey = System.getenv("DEEPGRAM_API_KEY");
        org.junit.jupiter.api.Assumptions.assumeTrue(
                apiKey != null && !apiKey.isEmpty(), "DEEPGRAM_API_KEY not set, skipping integration test");
        DeepgramClientBuilder builder = DeepgramClient.builder().apiKey(apiKey);
        // TEST ONLY: target a non-prod host (e.g. staging) by setting DEEPGRAM_BASE_URL
        // (wss://... or https://...). Defaults to production when unset.
        String baseUrl = System.getenv("DEEPGRAM_BASE_URL");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            String https = baseUrl.startsWith("wss://")
                    ? "https://" + baseUrl.substring("wss://".length())
                    : baseUrl.startsWith("ws://") ? "http://" + baseUrl.substring("ws://".length()) : baseUrl;
            builder.environment(Environment.custom()
                    .base(https)
                    .production(baseUrl)
                    .agent(baseUrl)
                    .agentRest(https)
                    .build());
        }
        client = builder.build();
    }

    // --- Tier 1: Must pass before release ---

    @Nested
    @DisplayName("Tier 1: Must-pass tests")
    class Tier1Tests {

        @Test
        @DisplayName("TranscribeURL - transcribe audio from a URL")
        void testIntegration_TranscribeURL() {
            ListenV1RequestUrl request =
                    ListenV1RequestUrl.builder().url(TEST_AUDIO_URL).build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

            assertThat(result).isNotNull();

            // Visit the union type to extract ListenV1Response
            ListenV1Response response = result.visit(new MediaTranscribeResponse.Visitor<ListenV1Response>() {
                @Override
                public ListenV1Response visit(ListenV1Response value) {
                    return value;
                }

                @Override
                public ListenV1Response visit(ListenV1AcceptedResponse value) {
                    return null;
                }
            });

            assertThat(response).as("expected ListenV1Response").isNotNull();

            ListenV1ResponseResults results = response.getResults();
            assertThat(results).isNotNull();
            assertThat(results.getChannels()).isNotEmpty();

            ListenV1ResponseResultsChannelsItem firstChannel =
                    results.getChannels().get(0);
            assertThat(firstChannel.getAlternatives()).isPresent();
            assertThat(firstChannel.getAlternatives().get()).isNotEmpty();

            Optional<String> transcript =
                    firstChannel.getAlternatives().get().get(0).getTranscript();
            assertThat(transcript).isPresent();
            assertThat(transcript.get()).isNotEmpty();
            System.out.println("Transcript: " + transcript.get());
        }

        @Test
        @DisplayName("TranscribeFile - transcribe audio from raw bytes")
        void testIntegration_TranscribeFile() throws Exception {
            // Download audio file
            URL url = new URL(TEST_AUDIO_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            byte[] audioData;
            try (InputStream inputStream = connection.getInputStream()) {
                audioData = inputStream.readAllBytes();
            }
            assertThat(audioData).isNotEmpty();

            MediaTranscribeRequestOctetStream request =
                    MediaTranscribeRequestOctetStream.builder().body(audioData).build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeFile(request);

            assertThat(result).isNotNull();

            ListenV1Response response = result.visit(new MediaTranscribeResponse.Visitor<ListenV1Response>() {
                @Override
                public ListenV1Response visit(ListenV1Response value) {
                    return value;
                }

                @Override
                public ListenV1Response visit(ListenV1AcceptedResponse value) {
                    return null;
                }
            });

            assertThat(response).as("expected ListenV1Response").isNotNull();

            ListenV1ResponseResults results = response.getResults();
            assertThat(results).isNotNull();
            assertThat(results.getChannels()).isNotEmpty();

            ListenV1ResponseResultsChannelsItem firstChannel =
                    results.getChannels().get(0);
            assertThat(firstChannel.getAlternatives()).isPresent();
            assertThat(firstChannel.getAlternatives().get()).isNotEmpty();

            Optional<String> transcript =
                    firstChannel.getAlternatives().get().get(0).getTranscript();
            assertThat(transcript).isPresent();
            assertThat(transcript.get()).isNotEmpty();
            System.out.println("File transcript: " + transcript.get());
        }

        @Test
        @DisplayName("SpeakREST - generate speech from text")
        void testIntegration_SpeakREST() throws Exception {
            SpeakV1Request request = SpeakV1Request.builder()
                    .text("Hello, this is a test of the Deepgram text to speech API.")
                    .build();

            InputStream audioStream = client.speak().v1().audio().generate(request);

            assertThat(audioStream).isNotNull();
            byte[] audioData = audioStream.readAllBytes();
            assertThat(audioData.length).as("expected audio bytes").isGreaterThan(0);
            System.out.println("Speak REST returned " + audioData.length + " bytes of audio");
        }

        @Test
        @DisplayName("InvalidAPIKey_REST - verify error handling for invalid key")
        void testIntegration_InvalidAPIKey_REST() {
            DeepgramClient invalidClient =
                    DeepgramClient.builder().apiKey("invalid-key-12345").build();

            ListenV1RequestUrl request =
                    ListenV1RequestUrl.builder().url(TEST_AUDIO_URL).build();

            assertThatThrownBy(() -> invalidClient.listen().v1().media().transcribeUrl(request))
                    .isInstanceOf(DeepgramHttpException.class)
                    .satisfies(thrown -> {
                        DeepgramHttpException apiException = (DeepgramHttpException) thrown;
                        System.out.println("Got API error: status=" + apiException.statusCode());
                        assertThat(apiException.statusCode())
                                .as("expected 401 or 403 for invalid API key")
                                .isIn(401, 403);
                    });
        }
    }

    // --- Tier 2: Should pass, less critical ---

    @Nested
    @DisplayName("Tier 2: Should-pass tests")
    class Tier2Tests {

        @Test
        @DisplayName("ReadAnalyze - analyze text content")
        void testIntegration_ReadAnalyze() {
            TextAnalyzeRequest request = TextAnalyzeRequest.builder()
                    .body(ReadV1Request.of(ReadV1RequestText.builder()
                            .text("The Java SDK is working great. I love using Deepgram for speech to text.")
                            .build()))
                    .sentiment(true)
                    .topics(true)
                    .language("en")
                    .build();

            ReadV1Response result = client.read().v1().text().analyze(request);

            assertThat(result).isNotNull();
            assertThat(result.getResults()).isNotNull();
            System.out.println("Read analysis completed successfully");
        }

        @Test
        @DisplayName("ManageProjects - list projects for the API key")
        void testIntegration_ManageProjects() {
            ListProjectsV1Response result = client.manage().v1().projects().list();

            assertThat(result).isNotNull();
            assertThat(result.getProjects()).isPresent();

            List<ListProjectsV1ResponseProjectsItem> projects =
                    result.getProjects().get();
            assertThat(projects).as("expected at least one project").isNotEmpty();
            System.out.println("Found " + projects.size() + " projects");
        }

        @Test
        @DisplayName("SpeakV2WebSocket - stream TTS audio over the Speak v2 WebSocket")
        void testIntegration_SpeakV2WebSocket() throws Exception {
            // The Speak v2 WebSocket endpoint is not yet generally available on the production URL
            // this key targets, so this test is opt-in: set DEEPGRAM_SPEAK_V2_WS=1 in an environment
            // with access (e.g. staging). Otherwise it is skipped rather than failing the build.
            String enabled = System.getenv("DEEPGRAM_SPEAK_V2_WS");
            org.junit.jupiter.api.Assumptions.assumeTrue(
                    enabled != null && !enabled.isEmpty(),
                    "DEEPGRAM_SPEAK_V2_WS not set, skipping Speak v2 WebSocket integration test");

            V2WebSocketClient wsClient = client.speak().v2().v2WebSocket();

            CountDownLatch flushedLatch = new CountDownLatch(1);
            CountDownLatch audioLatch = new CountDownLatch(1);
            AtomicLong totalAudioBytes = new AtomicLong(0);
            AtomicReference<String> serverError = new AtomicReference<>();

            wsClient.onSpeakV2Audio(audio -> {
                totalAudioBytes.addAndGet(audio.size());
                audioLatch.countDown();
            });
            wsClient.onFlushed(flushed -> flushedLatch.countDown());
            wsClient.onErrorMessage(error -> serverError.set(String.valueOf(error)));
            wsClient.onError(error -> serverError.set(error.getMessage()));

            try {
                V2ConnectOptions options = V2ConnectOptions.builder()
                        .model("flux-alexis-en")
                        .encoding(SpeakV2Encoding.LINEAR16)
                        .sampleRate(SpeakV2SampleRate.SIXTEEN_THOUSAND)
                        // speed is sent as a number on the connect URL. The generator retyped it to a
                        // closed string enum in the 2026-08-19 regen and we patched it back to Double;
                        // setting it here exercises that patch against the live server, which the
                        // mock-server wire test in SpeakV2ConnectWireTest cannot do. An out-of-range or
                        // off-increment value comes back as SPEED_OUT_OF_RANGE / SPEED_INCREMENT_INVALID,
                        // so a regression to the string form would surface as a server error below.
                        .speed(1.05)
                        .build();
                wsClient.connect(options).get(15, TimeUnit.SECONDS);

                wsClient.sendSpeak(SpeakV2Speak.builder()
                        .text("Hello from the Deepgram Java SDK integration test.")
                        .build());
                wsClient.sendFlush(SpeakV2Flush.builder().build());

                boolean flushed = flushedLatch.await(20, TimeUnit.SECONDS);
                // Flux streams audio frames *after* the Flushed control message, so wait for at
                // least one audio frame before closing — otherwise Close races ahead of the audio
                // and truncates the stream (observed against staging: Flushed arrives, then audio).
                boolean gotAudio = audioLatch.await(20, TimeUnit.SECONDS);

                wsClient.sendClose(SpeakV2Close.builder().build());

                assertThat(serverError.get())
                        .as("no server/transport error during streaming")
                        .isNull();
                assertThat(flushed).as("received a Flushed message").isTrue();
                assertThat(gotAudio).as("received at least one audio frame").isTrue();
                assertThat(totalAudioBytes.get())
                        .as("expected streamed audio bytes")
                        .isGreaterThan(0);
                System.out.println("Speak v2 WS returned " + totalAudioBytes.get() + " bytes of audio");
            } finally {
                wsClient.disconnect();
            }
        }
    }
}
