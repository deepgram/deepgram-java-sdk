import com.deepgram.DeepgramClient;
import com.deepgram.resources.read.v1.text.requests.TextAnalyzeRequest;
import com.deepgram.resources.read.v1.text.types.TextAnalyzeRequestCustomIntentMode;
import com.deepgram.resources.read.v1.text.types.TextAnalyzeRequestCustomTopicMode;
import com.deepgram.types.ReadV1Request;
import com.deepgram.types.ReadV1RequestText;
import com.deepgram.types.ReadV1Response;
import com.deepgram.types.ReadV1ResponseResults;
import java.util.Arrays;

/**
 * Advanced text intelligence analysis with custom topics and intents. Demonstrates how to guide Deepgram's analysis
 * using domain-specific categories.
 *
 * <p>Usage: java AdvancedAnalysis
 */
public class AdvancedAnalysis {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Advanced Text Analysis (Custom Topics & Intents)");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        try {
            String sampleText = "I've been a loyal customer for five years, but lately the service "
                    + "has been terrible. My last three orders arrived damaged, and when I called "
                    + "support, I was put on hold for over an hour. I'm considering switching to "
                    + "your competitor unless something changes. I'd like a full refund for my "
                    + "last order and a discount on future purchases.";

            System.out.println("Input text:");
            System.out.println("-".repeat(60));
            System.out.println(sampleText);
            System.out.println("-".repeat(60));
            System.out.println();

            // Create the request body
            ReadV1RequestText textBody =
                    ReadV1RequestText.builder().text(sampleText).build();

            // Build request with custom topics and intents
            TextAnalyzeRequest request = TextAnalyzeRequest.builder()
                    .body(ReadV1Request.of(textBody))
                    .language("en")
                    .sentiment(true)
                    .topics(true)
                    .intents(true)
                    .customTopic(Arrays.asList(
                            "Customer Retention",
                            "Product Quality",
                            "Customer Support",
                            "Pricing and Refunds",
                            "Competitor Comparison"))
                    .customTopicMode(TextAnalyzeRequestCustomTopicMode.EXTENDED)
                    .customIntent(Arrays.asList(
                            "Request Refund", "Complaint", "Threat to Leave", "Request Discount", "Escalation Request"))
                    .customIntentMode(TextAnalyzeRequestCustomIntentMode.EXTENDED)
                    .build();

            ReadV1Response response = client.read().v1().text().analyze(request);

            // Display results
            ReadV1ResponseResults results = response.getResults();

            System.out.println("=== Analysis Results ===");
            System.out.println();

            // Sentiments
            results.getSentiments().ifPresent(sentiments -> {
                System.out.println("Sentiments:");
                System.out.println("-".repeat(50));
                System.out.println(sentiments);
                System.out.println();
            });

            // Topics (including custom)
            results.getTopics().ifPresent(topics -> {
                System.out.println("Topics (with custom categories):");
                System.out.println("-".repeat(50));
                System.out.println(topics);
                System.out.println();
            });

            // Intents (including custom)
            results.getIntents().ifPresent(intents -> {
                System.out.println("Intents (with custom categories):");
                System.out.println("-".repeat(50));
                System.out.println(intents);
                System.out.println();
            });

            // Summary
            results.getSummary().ifPresent(summary -> {
                System.out.println("Summary:");
                System.out.println("-".repeat(50));
                System.out.println(summary);
            });

        } catch (Exception e) {
            System.err.println("Error analyzing text: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
