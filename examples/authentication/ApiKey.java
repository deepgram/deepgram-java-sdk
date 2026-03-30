import com.deepgram.DeepgramClient;
import java.util.Collections;
import java.util.List;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;

/**
 * Demonstrates authenticating with an API key and making a simple API call.
 * The API key is read from the DEEPGRAM_API_KEY environment variable.
 *
 * <p>Usage: java ApiKey
 */
public class ApiKey {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Authentication with API Key");
        System.out.println();

        // Create client using API key
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            // Verify authentication by listing projects
            ListProjectsV1Response response = client.manage().v1().projects().list();

            List<ListProjectsV1ResponseProjectsItem> projects =
                    response.getProjects().orElse(Collections.emptyList());

            System.out.println("Authentication successful!");
            System.out.printf("Found %d project(s):%n", projects.size());

            for (ListProjectsV1ResponseProjectsItem project : projects) {
                String id = project.getProjectId().orElse("unknown");
                String name = project.getName().orElse("unnamed");
                System.out.printf("  - %s (%s)%n", name, id);
            }

        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
