import com.deepgram.DeepgramClient;
import com.deepgram.resources.manage.v1.projects.billing.breakdown.requests.BreakdownListRequest;
import com.deepgram.resources.manage.v1.projects.billing.breakdown.types.BreakdownListRequestGroupingItem;
import com.deepgram.types.BillingBreakdownV1Response;
import com.deepgram.types.BillingBreakdownV1ResponseResultsItem;
import com.deepgram.types.BillingBreakdownV1ResponseResultsItemGrouping;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * View detailed billing breakdown by line item and tags for a Deepgram project.
 *
 * <p>Usage: java BillingDetailed
 */
public class BillingDetailed {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Detailed Billing Breakdown");
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

            // Get billing breakdown grouped by line item
            System.out.println("=== Billing by Line Item ===");
            System.out.println("-".repeat(60));
            BreakdownListRequest lineItemRequest = BreakdownListRequest.builder()
                    .grouping(Arrays.asList(BreakdownListRequestGroupingItem.LINE_ITEM))
                    .build();

            BillingBreakdownV1Response lineItemBreakdown =
                    client.manage().v1().projects().billing().breakdown().list(projectId, lineItemRequest);

            printBillingResults(lineItemBreakdown);

            System.out.println();

            // Get billing breakdown grouped by tags
            System.out.println("=== Billing by Tags ===");
            System.out.println("-".repeat(60));
            BreakdownListRequest tagRequest = BreakdownListRequest.builder()
                    .grouping(Arrays.asList(BreakdownListRequestGroupingItem.TAGS))
                    .build();

            BillingBreakdownV1Response tagBreakdown =
                    client.manage().v1().projects().billing().breakdown().list(projectId, tagRequest);

            printBillingResults(tagBreakdown);

        } catch (Exception e) {
            System.err.println("Error retrieving billing breakdown: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printBillingResults(BillingBreakdownV1Response response) {
        List<BillingBreakdownV1ResponseResultsItem> results = response.getResults();

        if (results.isEmpty()) {
            System.out.println("  No billing data found");
            return;
        }

        float totalDollars = 0;
        for (BillingBreakdownV1ResponseResultsItem item : results) {
            BillingBreakdownV1ResponseResultsItemGrouping grouping = item.getGrouping();
            String lineItem = grouping.getLineItem().orElse("N/A");
            String deployment = grouping.getDeployment().orElse("N/A");
            String start = grouping.getStart().orElse("");
            String end = grouping.getEnd().orElse("");
            List<String> tags = grouping.getTags().orElse(Collections.emptyList());

            System.out.printf("  Line Item:  %s%n", lineItem);
            System.out.printf("  Deployment: %s%n", deployment);
            if (!tags.isEmpty()) {
                System.out.printf("  Tags:       %s%n", String.join(", ", tags));
            }
            if (!start.isEmpty() && !end.isEmpty()) {
                System.out.printf("  Period:     %s to %s%n", start, end);
            }
            System.out.printf("  Cost:       $%.4f%n", item.getDollars());
            System.out.println();
            totalDollars += item.getDollars();
        }

        System.out.printf("  Total: $%.4f%n", totalDollars);
    }
}
