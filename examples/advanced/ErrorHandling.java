import com.deepgram.DeepgramClient;
import com.deepgram.core.DeepgramHttpException;
import com.deepgram.errors.BadRequestError;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;

/**
 * Demonstrates error handling patterns with the Deepgram Java SDK. Shows how to catch and handle different error types
 * including API errors, bad requests, and network issues.
 *
 * <p>Usage: java ErrorHandling
 */
public class ErrorHandling {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Error Handling Examples");
        System.out.println();

        // Example 1: Handle a bad request (invalid URL)
        System.out.println("1. Bad Request Error (invalid audio URL):");
        System.out.println("-".repeat(50));
        try {
            ListenV1RequestUrl request =
                    ListenV1RequestUrl.builder().url("not-a-valid-url").build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);
            System.out.println("Unexpected success: " + result);

        } catch (BadRequestError e) {
            System.out.println("  Caught BadRequestError (HTTP 400)");
            System.out.println("  Status: " + e.statusCode());
            System.out.println("  Body:   " + e.body());
            System.out.println("  Message: " + e.getMessage());
        } catch (DeepgramHttpException e) {
            System.out.println("  Caught API error");
            System.out.println("  Status: " + e.statusCode());
            System.out.println("  Body:   " + e.body());
        } catch (Exception e) {
            System.out.println("  Caught exception: " + e.getClass().getSimpleName());
            System.out.println("  Message: " + e.getMessage());
        }

        System.out.println();

        // Example 2: Handle authentication error (invalid API key)
        System.out.println("2. Authentication Error (invalid API key):");
        System.out.println("-".repeat(50));
        try {
            DeepgramClient badClient =
                    DeepgramClient.builder().apiKey("invalid-api-key").build();

            badClient.manage().v1().projects().list();
            System.out.println("Unexpected success");

        } catch (DeepgramHttpException e) {
            System.out.println("  Caught API error");
            System.out.println("  Status: " + e.statusCode());
            System.out.println("  Body:   " + e.body());
        } catch (Exception e) {
            System.out.println("  Caught exception: " + e.getClass().getSimpleName());
            System.out.println("  Message: " + e.getMessage());
        }

        System.out.println();

        // Example 3: Successful request with proper error handling
        System.out.println("3. Successful Request:");
        System.out.println("-".repeat(50));
        try {
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url("https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav")
                    .build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);
            System.out.println("  Request succeeded");
            System.out.println("  Response: " + result);

        } catch (DeepgramHttpException e) {
            System.err.println("  API error: " + e.statusCode() + " - " + e.body());
        } catch (Exception e) {
            System.err.println("  Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
