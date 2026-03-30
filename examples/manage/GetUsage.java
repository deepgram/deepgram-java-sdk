import com.deepgram.DeepgramClient;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import com.deepgram.types.UsageV1Response;
import java.util.Collections;
import java.util.List;

/**
 * Retrieve usage information for a Deepgram project.
 *
 * <p>Usage: java GetUsage
 */
public class GetUsage {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Project Usage");
        System.out.println();

        try {
            // First, get the project ID
            ListProjectsV1Response projectsResponse =
                    client.manage().v1().projects().list();
            List<ListProjectsV1ResponseProjectsItem> projects =
                    projectsResponse.getProjects().orElse(Collections.emptyList());

            if (projects.isEmpty()) {
                System.out.println("No projects found");
                System.exit(1);
            }

            String projectId = projects.get(0).getProjectId().orElse("");
            String projectName = projects.get(0).getName().orElse("unnamed");
            System.out.printf("Using project: %s (%s)%n%n", projectName, projectId);

            // Get usage
            UsageV1Response usageResponse =
                    client.manage().v1().projects().usage().get(projectId);

            System.out.println("Usage Summary:");
            System.out.println("-".repeat(60));

            usageResponse.getStart().ifPresent(start -> System.out.println("  Start:      " + start));
            usageResponse.getEnd().ifPresent(end -> System.out.println("  End:        " + end));
            usageResponse.getResolution().ifPresent(resolution -> System.out.println("  Resolution: " + resolution));

            System.out.println();
            System.out.println("Full response: " + usageResponse);

        } catch (Exception e) {
            System.err.println("Error retrieving usage: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
