import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates per-request configuration overrides using RequestOptions. Different requests can have different
 * timeouts, headers, and even API keys.
 *
 * <p>Usage: java ScopedConfig
 */
public class ScopedConfig {
    private static final String SAMPLE_AUDIO_URL =
            "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav";

    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client with default settings
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Scoped Configuration (Per-Request Overrides)");
        System.out.println();

        try {
            // Request 1: Default configuration
            System.out.println("=== Request 1: Default config ===");
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url(SAMPLE_AUDIO_URL)
                    .smartFormat(true)
                    .build();

            MediaTranscribeResponse result1 = client.listen().v1().media().transcribeUrl(request);
            printTranscript("Default", result1);

            System.out.println();

            // Request 2: Extended timeout for long audio
            System.out.println("=== Request 2: Extended timeout (120s) ===");
            com.deepgram.core.RequestOptions longTimeoutOptions = com.deepgram.core.RequestOptions.builder()
                    .timeout(120, TimeUnit.SECONDS)
                    .addHeader("X-Request-Context", "long-audio-processing")
                    .build();

            MediaTranscribeResponse result2 = client.listen().v1().media().transcribeUrl(request, longTimeoutOptions);
            printTranscript("Extended timeout", result2);

            System.out.println();

            // Request 3: Custom tag header for tracking
            System.out.println("=== Request 3: Tagged request ===");
            com.deepgram.core.RequestOptions taggedOptions = com.deepgram.core.RequestOptions.builder()
                    .timeout(30, TimeUnit.SECONDS)
                    .addHeader("X-Batch-Id", "batch-2024-001")
                    .addHeader("X-Priority", "high")
                    .build();

            MediaTranscribeResponse result3 = client.listen().v1().media().transcribeUrl(request, taggedOptions);
            printTranscript("Tagged", result3);

            System.out.println();
            System.out.println("All requests completed with different scoped configurations.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printTranscript(String label, MediaTranscribeResponse response) {
        response.visit(new MediaTranscribeResponse.Visitor<Void>() {
            @Override
            public Void visit(ListenV1Response resp) {
                ListenV1ResponseResults results = resp.getResults();
                if (results != null && !results.getChannels().isEmpty()) {
                    ListenV1ResponseResultsChannelsItem channel =
                            results.getChannels().get(0);
                    List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                            channel.getAlternatives().orElse(Collections.emptyList());
                    if (!alternatives.isEmpty()) {
                        alternatives.get(0).getTranscript().ifPresent(transcript -> {
                            System.out.printf(
                                    "[%s] Transcript: %s%n",
                                    label, transcript.length() > 80 ? transcript.substring(0, 80) + "..." : transcript);
                        });
                    }
                }
                return null;
            }

            @Override
            public Void visit(ListenV1AcceptedResponse accepted) {
                System.out.printf("[%s] Request accepted: %s%n", label, accepted.getRequestId());
                return null;
            }
        });
    }
}
