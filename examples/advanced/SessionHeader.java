import com.deepgram.DeepgramClient;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;

/**
 * Demonstrates setting custom session and tracking headers via the client builder.
 * These headers are sent with every request for correlation and debugging.
 *
 * <p>Usage: java SessionHeader
 */
public class SessionHeader {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Generate a session ID for request correlation
        String sessionId = UUID.randomUUID().toString();
        String requestTag = "demo-session-header";

        System.out.println("Custom Session / Tracking Headers");
        System.out.println("Session ID: " + sessionId);
        System.out.println("Request Tag: " + requestTag);
        System.out.println();

        // Create client with custom headers using the builder
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .sessionId(sessionId)
                .addHeader("X-Request-Tag", requestTag)
                .addHeader("X-Client-Version", "java-sdk-example/1.0")
                .build();

        try {
            // Build request
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url("https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav")
                    .smartFormat(true)
                    .build();

            System.out.println("Making request with custom headers...");
            MediaTranscribeResponse result =
                    client.listen().v1().media().transcribeUrl(request);

            // Display results
            result.visit(new MediaTranscribeResponse.Visitor<Void>() {
                @Override
                public Void visit(ListenV1Response response) {
                    ListenV1ResponseResults results = response.getResults();
                    if (results != null && !results.getChannels().isEmpty()) {
                        ListenV1ResponseResultsChannelsItem channel = results.getChannels().get(0);
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
                public Void visit(ListenV1AcceptedResponse accepted) {
                    System.out.println("Request accepted: " + accepted.getRequestId());
                    return null;
                }
            });

            System.out.println();
            System.out.println("Request completed with session ID: " + sessionId);
            System.out.println("These headers appear in Deepgram's request logs for debugging.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
