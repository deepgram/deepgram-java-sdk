import com.deepgram.DeepgramClient;
import java.util.Collections;
import java.util.List;
import com.deepgram.types.CreateKeyV1Response;
import com.deepgram.types.ListProjectKeysV1Response;
import com.deepgram.types.ListProjectKeysV1ResponseApiKeysItem;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;

/**
 * Manage API keys for a project: list existing keys and create a new one.
 *
 * <p>Usage: java ManageKeys
 */
public class ManageKeys {
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

        System.out.println("Manage API Keys");
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

            // List existing keys
            ListProjectKeysV1Response keysResponse =
                    client.manage().v1().projects().keys().list(projectId);

            List<ListProjectKeysV1ResponseApiKeysItem> keys =
                    keysResponse.getApiKeys().orElse(Collections.emptyList());

            System.out.printf("Existing API Keys (%d):%n", keys.size());
            System.out.println("-".repeat(60));

            for (ListProjectKeysV1ResponseApiKeysItem keyItem : keys) {
                keyItem.getApiKey().ifPresent(key -> {
                    System.out.printf("  Key ID:  %s%n", key);
                });
                keyItem.getMember().ifPresent(member -> {
                    System.out.printf("  Member:  %s%n", member);
                });
                System.out.println();
            }

            // Note: Creating a key requires careful consideration.
            // Uncomment the following to create a new key:
            //
            // CreateKeysRequest createRequest = CreateKeysRequest.builder()
            //         .body(Map.of("comment", "SDK Example Key",
            //                      "scopes", List.of("member")))
            //         .build();
            //
            // CreateKeyV1Response createResponse =
            //         client.manage().v1().projects().keys().create(projectId, createRequest);
            // System.out.println("Created key: " + createResponse);

            System.out.println("To create or delete keys, uncomment the relevant code sections.");

        } catch (Exception e) {
            System.err.println("Error managing keys: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
