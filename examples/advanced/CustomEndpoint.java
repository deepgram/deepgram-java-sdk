import com.deepgram.DeepgramClient;
import com.deepgram.core.Environment;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates configuring a regional or EU endpoint for data residency. Uses Environment.custom() to point all API
 * traffic to a specific region.
 *
 * <p>Usage: java CustomEndpoint
 */
public class CustomEndpoint {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Read custom endpoint from environment, or use EU endpoint as example
        String customBaseUrl = System.getenv("DEEPGRAM_BASE_URL");
        if (customBaseUrl == null || customBaseUrl.isEmpty()) {
            customBaseUrl = "https://api.deepgram.com";
        }

        System.out.println("Custom / Regional Endpoint Configuration");
        System.out.println("Base URL: " + customBaseUrl);
        System.out.println();

        // Create a custom environment for a specific region
        // For EU data residency, you would use a URL like:
        //   https://api.eu.deepgram.com
        Environment regionalEnv = Environment.custom()
                .base(customBaseUrl)
                .agent(customBaseUrl.replace("https://", "wss://").replace("http://", "wss://"))
                .production(customBaseUrl.replace("https://", "wss://").replace("http://", "wss://"))
                .build();

        // Create client with the regional environment
        DeepgramClient client =
                DeepgramClient.builder().apiKey(apiKey).environment(regionalEnv).build();

        try {
            // Verify the connection
            ListProjectsV1Response response = client.manage().v1().projects().list();

            List<ListProjectsV1ResponseProjectsItem> projects =
                    response.getProjects().orElse(Collections.emptyList());

            System.out.println("Connection to custom endpoint successful!");
            System.out.printf("Found %d project(s):%n", projects.size());

            for (ListProjectsV1ResponseProjectsItem project : projects) {
                String id = project.getProjectId().orElse("unknown");
                String name = project.getName().orElse("unnamed");
                System.out.printf("  - %s (%s)%n", name, id);
            }

            System.out.println();
            System.out.println("Note: For EU data residency, set DEEPGRAM_BASE_URL to your");
            System.out.println("regional endpoint (e.g., https://api.eu.deepgram.com).");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
