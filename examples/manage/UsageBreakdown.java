import com.deepgram.DeepgramClient;
import com.deepgram.resources.manage.v1.projects.usage.breakdown.requests.BreakdownGetRequest;
import com.deepgram.resources.manage.v1.projects.usage.breakdown.types.BreakdownGetRequestGrouping;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import com.deepgram.types.UsageBreakdownV1Response;
import com.deepgram.types.UsageBreakdownV1ResponseResultsItem;
import com.deepgram.types.UsageBreakdownV1ResponseResultsItemGrouping;
import java.util.Collections;
import java.util.List;

/**
 * View usage breakdown grouped by model and tag for a Deepgram project.
 *
 * <p>Usage: java UsageBreakdown
 */
public class UsageBreakdown {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Usage Breakdown");
        System.out.println();

        try {
            // Get the first project
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

            // Get usage breakdown grouped by model
            System.out.println("=== Usage by Model ===");
            System.out.println("-".repeat(60));
            BreakdownGetRequest modelRequest = BreakdownGetRequest.builder()
                    .grouping(BreakdownGetRequestGrouping.MODELS)
                    .build();

            UsageBreakdownV1Response modelBreakdown =
                    client.manage().v1().projects().usage().breakdown().get(projectId, modelRequest);

            printUsageResults(modelBreakdown);

            System.out.println();

            // Get usage breakdown grouped by tags
            System.out.println("=== Usage by Tags ===");
            System.out.println("-".repeat(60));
            BreakdownGetRequest tagRequest = BreakdownGetRequest.builder()
                    .grouping(BreakdownGetRequestGrouping.TAGS)
                    .build();

            UsageBreakdownV1Response tagBreakdown =
                    client.manage().v1().projects().usage().breakdown().get(projectId, tagRequest);

            printUsageResults(tagBreakdown);

        } catch (Exception e) {
            System.err.println("Error retrieving usage breakdown: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsageResults(UsageBreakdownV1Response response) {
        List<UsageBreakdownV1ResponseResultsItem> results = response.getResults();

        if (results.isEmpty()) {
            System.out.println("  No usage data found");
            return;
        }

        for (UsageBreakdownV1ResponseResultsItem item : results) {
            UsageBreakdownV1ResponseResultsItemGrouping grouping = item.getGrouping();
            String model = grouping.getModels().orElse("N/A");
            String tags = grouping.getTags().orElse("N/A");

            System.out.printf("  Model: %-20s Tags: %-15s%n", model, tags);
            System.out.printf("    Hours:       %.4f%n", item.getHours());
            System.out.printf("    Requests:    %.0f%n", item.getRequests());
            System.out.printf("    Tokens In:   %.0f%n", item.getTokensIn());
            System.out.printf("    Tokens Out:  %.0f%n", item.getTokensOut());
            System.out.printf("    TTS Chars:   %.0f%n", item.getTtsCharacters());
            System.out.println();
        }
    }
}
