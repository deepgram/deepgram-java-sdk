import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import core.DeepgramApiApiException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import resources.listen.v1.media.requests.ListenV1RequestUrl;
import resources.listen.v1.media.requests.MediaTranscribeRequestOctetStream;
import resources.listen.v1.media.types.MediaTranscribeResponse;
import resources.read.v1.text.requests.TextAnalyzeRequest;
import resources.speak.v1.audio.requests.SpeakV1Request;
import types.ListProjectsV1Response;
import types.ListProjectsV1ResponseProjectsItem;
import types.ListenV1Response;
import types.ListenV1ResponseResults;
import types.ListenV1ResponseResultsChannelsItem;
import types.ReadV1Request;
import types.ReadV1RequestText;
import types.ReadV1Response;

/**
 * Integration tests for the Deepgram Java SDK. These tests require a valid DEEPGRAM_API_KEY
 * environment variable and make real API calls to Deepgram services.
 *
 * <p>Tests are organized into tiers: Tier 1 tests must pass before release; Tier 2 tests should
 * pass but are less critical.
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
    client = DeepgramClient.builder().apiKey(apiKey).build();
  }

  // --- Tier 1: Must pass before release ---

  @Nested
  @DisplayName("Tier 1: Must-pass tests")
  class Tier1Tests {

    @Test
    @DisplayName("TranscribeURL - transcribe audio from a URL")
    void testIntegration_TranscribeURL() {
      ListenV1RequestUrl request = ListenV1RequestUrl.builder().url(TEST_AUDIO_URL).build();

      MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

      assertThat(result).isNotNull();

      // Visit the union type to extract ListenV1Response
      ListenV1Response response =
          result.visit(
              new MediaTranscribeResponse.Visitor<ListenV1Response>() {
                @Override
                public ListenV1Response visit(ListenV1Response value) {
                  return value;
                }

                @Override
                public ListenV1Response visit(types.ListenV1AcceptedResponse value) {
                  return null;
                }
              });

      assertThat(response).as("expected ListenV1Response").isNotNull();

      ListenV1ResponseResults results = response.getResults();
      assertThat(results).isNotNull();
      assertThat(results.getChannels()).isNotEmpty();

      ListenV1ResponseResultsChannelsItem firstChannel = results.getChannels().get(0);
      assertThat(firstChannel.getAlternatives()).isPresent();
      assertThat(firstChannel.getAlternatives().get()).isNotEmpty();

      Optional<String> transcript = firstChannel.getAlternatives().get().get(0).getTranscript();
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

      ListenV1Response response =
          result.visit(
              new MediaTranscribeResponse.Visitor<ListenV1Response>() {
                @Override
                public ListenV1Response visit(ListenV1Response value) {
                  return value;
                }

                @Override
                public ListenV1Response visit(types.ListenV1AcceptedResponse value) {
                  return null;
                }
              });

      assertThat(response).as("expected ListenV1Response").isNotNull();

      ListenV1ResponseResults results = response.getResults();
      assertThat(results).isNotNull();
      assertThat(results.getChannels()).isNotEmpty();

      ListenV1ResponseResultsChannelsItem firstChannel = results.getChannels().get(0);
      assertThat(firstChannel.getAlternatives()).isPresent();
      assertThat(firstChannel.getAlternatives().get()).isNotEmpty();

      Optional<String> transcript = firstChannel.getAlternatives().get().get(0).getTranscript();
      assertThat(transcript).isPresent();
      assertThat(transcript.get()).isNotEmpty();
      System.out.println("File transcript: " + transcript.get());
    }

    @Test
    @DisplayName("SpeakREST - generate speech from text")
    void testIntegration_SpeakREST() throws Exception {
      SpeakV1Request request =
          SpeakV1Request.builder()
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
      DeepgramClient invalidClient = DeepgramClient.builder().apiKey("invalid-key-12345").build();

      ListenV1RequestUrl request = ListenV1RequestUrl.builder().url(TEST_AUDIO_URL).build();

      assertThatThrownBy(() -> invalidClient.listen().v1().media().transcribeUrl(request))
          .isInstanceOf(DeepgramApiApiException.class)
          .satisfies(
              thrown -> {
                DeepgramApiApiException apiException = (DeepgramApiApiException) thrown;
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
      TextAnalyzeRequest request =
          TextAnalyzeRequest.builder()
              .body(
                  ReadV1Request.of(
                      ReadV1RequestText.builder()
                          .text(
                              "The Java SDK is working great. I love using Deepgram for speech to text.")
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

      List<ListProjectsV1ResponseProjectsItem> projects = result.getProjects().get();
      assertThat(projects).as("expected at least one project").isNotEmpty();
      System.out.println("Found " + projects.size() + " projects");
    }
  }
}
