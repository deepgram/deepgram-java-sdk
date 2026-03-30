import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates using custom request options to override settings per-request, such as timeouts, custom headers, and
 * query parameters.
 *
 * <p>Usage: java RequestOptions
 */
public class RequestOptions {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Custom Request Options");
        System.out.println();

        try {
            // Build custom request options
            com.deepgram.core.RequestOptions requestOptions = com.deepgram.core.RequestOptions.builder()
                    .timeout(60, TimeUnit.SECONDS) // 60-second timeout
                    .addHeader("X-Custom-Header", "example-value") // Custom header
                    .build();

            System.out.println("Making request with custom options:");
            System.out.println("  Timeout: 60 seconds");
            System.out.println("  Custom Header: X-Custom-Header=example-value");
            System.out.println();

            // Build request
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url("https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav")
                    .smartFormat(true)
                    .build();

            // Make the request with custom options
            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request, requestOptions);

            // Display results
            result.visit(new MediaTranscribeResponse.Visitor<Void>() {
                @Override
                public Void visit(ListenV1Response response) {
                    ListenV1ResponseResults results = response.getResults();
                    if (results != null && !results.getChannels().isEmpty()) {
                        ListenV1ResponseResultsChannelsItem channel =
                                results.getChannels().get(0);
                        List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                                channel.getAlternatives().orElse(Collections.emptyList());
                        if (!alternatives.isEmpty()) {
                            alternatives.get(0).getTranscript().ifPresent(transcript -> {
                                System.out.println("Transcription:");
                                System.out.println("-".repeat(50));
                                System.out.println(transcript);
                                System.out.println("-".repeat(50));
                            });
                        }
                    }
                    return null;
                }

                @Override
                public Void visit(com.deepgram.types.ListenV1AcceptedResponse accepted) {
                    System.out.println("Request accepted: " + accepted.getRequestId());
                    return null;
                }
            });

            System.out.println();
            System.out.println("Request completed successfully with custom options.");

            // Example: Per-request API key override
            System.out.println();
            System.out.println("Note: You can also override the API key per-request:");
            System.out.println("  com.deepgram.core.RequestOptions.builder()");
            System.out.println("      .apiKey(\"different-api-key\")");
            System.out.println("      .build();");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
