import com.deepgram.DeepgramClient;
import java.util.Collections;
import java.util.List;
import com.deepgram.types.ListProjectDistributionCredentialsV1Response;
import com.deepgram.types.ListProjectDistributionCredentialsV1ResponseDistributionCredentialsItem;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;

/**
 * Manage self-hosted distribution credentials for on-premises Deepgram deployments.
 * Lists existing credentials for a project.
 *
 * <p>Usage: java Credentials
 */
public class Credentials {
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

        System.out.println("Self-Hosted Distribution Credentials");
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

            // List distribution credentials
            ListProjectDistributionCredentialsV1Response credentialsResponse =
                    client.selfHosted().v1().distributionCredentials().list(projectId);

            List<ListProjectDistributionCredentialsV1ResponseDistributionCredentialsItem> credentials =
                    credentialsResponse.getDistributionCredentials().orElse(Collections.emptyList());

            System.out.printf("Distribution Credentials (%d):%n", credentials.size());
            System.out.println("-".repeat(60));

            if (credentials.isEmpty()) {
                System.out.println("  No distribution credentials found.");
                System.out.println("  Distribution credentials are used for self-hosted deployments.");
            } else {
                for (var cred : credentials) {
                    System.out.println("  " + cred);
                }
            }

            System.out.println();

            // Note: To create new credentials, use:
            // client.selfHosted().v1().distributionCredentials().create(projectId);
            //
            // To get specific credentials:
            // client.selfHosted().v1().distributionCredentials().get(projectId, credentialId);
            //
            // To delete credentials:
            // client.selfHosted().v1().distributionCredentials().delete(projectId, credentialId);

            System.out.println("To create or manage credentials, uncomment the relevant code sections.");

        } catch (Exception e) {
            System.err.println("Error managing credentials: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
