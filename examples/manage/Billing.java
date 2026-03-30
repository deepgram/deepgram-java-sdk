import com.deepgram.DeepgramClient;
import java.util.Collections;
import java.util.List;
import com.deepgram.types.ListProjectBalancesV1Response;
import com.deepgram.types.ListProjectBalancesV1ResponseBalancesItem;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;

/**
 * View billing balances for a Deepgram project.
 *
 * <p>Usage: java Billing
 */
public class Billing {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        System.out.println("Billing Information");
        System.out.println();

        try {
            // First, get the project ID
            ListProjectsV1Response projectsResponse = client.manage().v1().projects().list();
            List<ListProjectsV1ResponseProjectsItem> projects =
                    projectsResponse.getProjects().orElse(Collections.emptyList());

            if (projects.isEmpty()) {
                System.out.println("No projects found");
                System.exit(1);
            }

            String projectId = projects.get(0).getProjectId().orElse("");
            String projectName = projects.get(0).getName().orElse("unnamed");
            System.out.printf("Using project: %s (%s)%n%n", projectName, projectId);

            // List balances
            ListProjectBalancesV1Response balancesResponse =
                    client.manage().v1().projects().billing().balances().list(projectId);

            List<ListProjectBalancesV1ResponseBalancesItem> balances =
                    balancesResponse.getBalances().orElse(Collections.emptyList());

            System.out.printf("Balances (%d):%n", balances.size());
            System.out.println("-".repeat(60));

            if (balances.isEmpty()) {
                System.out.println("  No balances found");
            } else {
                for (ListProjectBalancesV1ResponseBalancesItem balance : balances) {
                    String balanceId = balance.getBalanceId().orElse("unknown");
                    double amount = balance.getAmount().orElse(0.0);
                    String units = balance.getUnits().orElse("");
                    String purchaseOrderId = balance.getPurchaseOrderId().orElse("N/A");

                    System.out.printf("  Balance ID: %s%n", balanceId);
                    System.out.printf("  Amount:     %.2f %s%n", amount, units);
                    System.out.printf("  PO ID:      %s%n", purchaseOrderId);
                    System.out.println();
                }
            }

        } catch (Exception e) {
            System.err.println("Error retrieving billing info: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
