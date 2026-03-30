import com.deepgram.DeepgramClient;
import com.deepgram.resources.read.v1.text.requests.TextAnalyzeRequest;
import com.deepgram.types.ReadV1Request;
import com.deepgram.types.ReadV1RequestText;
import com.deepgram.types.ReadV1Response;
import com.deepgram.types.ReadV1ResponseResults;

/**
 * Analyze text content using Deepgram's text intelligence API. Demonstrates sentiment analysis, topic detection,
 * summarization, and intent detection.
 *
 * <p>Usage: java AnalyzeText
 */
public class AnalyzeText {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Analyze Text (Read API)");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        try {
            // Create the request body with inline text
            ReadV1RequestText textBody = ReadV1RequestText.builder()
                    .text(
                            "Life moves pretty fast. If you don't stop and look around once in a while, you could miss it.")
                    .build();

            // Build the analyze request with options
            TextAnalyzeRequest request = TextAnalyzeRequest.builder()
                    .body(ReadV1Request.of(textBody))
                    .sentiment(true)
                    .topics(true)
                    .intents(true)
                    .language("en")
                    .build();

            ReadV1Response response = client.read().v1().text().analyze(request);

            // Display results
            ReadV1ResponseResults results = response.getResults();

            // Summary
            results.getSummary().ifPresent(summary -> {
                System.out.println("Summary:");
                System.out.println("-".repeat(50));
                System.out.println(summary);
                System.out.println();
            });

            // Topics
            results.getTopics().ifPresent(topics -> {
                System.out.println("Topics:");
                System.out.println("-".repeat(50));
                System.out.println(topics);
                System.out.println();
            });

            // Intents
            results.getIntents().ifPresent(intents -> {
                System.out.println("Intents:");
                System.out.println("-".repeat(50));
                System.out.println(intents);
                System.out.println();
            });

            // Sentiments
            results.getSentiments().ifPresent(sentiments -> {
                System.out.println("Sentiments:");
                System.out.println("-".repeat(50));
                System.out.println(sentiments);
            });

        } catch (Exception e) {
            System.err.println("Error analyzing text: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
