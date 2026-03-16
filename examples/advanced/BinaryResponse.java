import java.util.List;
import java.util.Map;
import core.DeepgramApiHttpResponse;
import resources.listen.v1.media.requests.ListenV1RequestUrl;
import resources.listen.v1.media.types.MediaTranscribeResponse;

/**
 * Demonstrates accessing raw HTTP response headers alongside the parsed body.
 * Uses the withRawResponse() method to get both headers and body from the API.
 *
 * <p>Usage: java BinaryResponse
 */
public class BinaryResponse {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Raw HTTP Response (Headers + Body)");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            // Build request
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url("https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav")
                    .smartFormat(true)
                    .build();

            // Use withRawResponse() to access HTTP metadata
            DeepgramApiHttpResponse<MediaTranscribeResponse> rawResponse =
                    client.listen().v1().media().withRawResponse().transcribeUrl(request);

            // Access response headers
            Map<String, List<String>> headers = rawResponse.headers();

            System.out.println("=== Response Headers ===");
            System.out.println("-".repeat(60));
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String value : entry.getValue()) {
                    System.out.printf("  %s: %s%n", entry.getKey(), value);
                }
            }
            System.out.println();

            // Highlight useful headers
            System.out.println("=== Key Headers ===");
            System.out.println("-".repeat(60));
            printHeader(headers, "x-request-id");
            printHeader(headers, "content-type");
            printHeader(headers, "x-dg-model-name");
            printHeader(headers, "x-dg-model-uuid");
            printHeader(headers, "x-dg-sha");
            System.out.println();

            // Access the parsed body as usual
            MediaTranscribeResponse body = rawResponse.body();
            System.out.println("=== Response Body ===");
            System.out.println("-".repeat(60));
            System.out.println("Response type: " + body.getClass().getSimpleName());
            System.out.println("Response received successfully.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHeader(Map<String, List<String>> headers, String name) {
        List<String> values = headers.get(name);
        if (values != null && !values.isEmpty()) {
            System.out.printf("  %s: %s%n", name, String.join(", ", values));
        } else {
            System.out.printf("  %s: (not present)%n", name);
        }
    }
}
