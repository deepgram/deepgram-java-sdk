import com.deepgram.DeepgramClient;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import java.util.Collections;
import java.util.List;

/**
 * List all projects associated with the authenticated API key.
 *
 * <p>Usage: java ListProjects
 */
public class ListProjects {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Listing projects...");
        System.out.println();

        try {
            ListProjectsV1Response response = client.manage().v1().projects().list();

            List<ListProjectsV1ResponseProjectsItem> projects =
                    response.getProjects().orElse(Collections.emptyList());

            System.out.printf("Found %d project(s):%n", projects.size());
            System.out.println("-".repeat(60));

            for (ListProjectsV1ResponseProjectsItem project : projects) {
                String id = project.getProjectId().orElse("unknown");
                String name = project.getName().orElse("unnamed");
                System.out.printf("  Project: %s%n", name);
                System.out.printf("  ID:      %s%n", id);
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Error listing projects: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
