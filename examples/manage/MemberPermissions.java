import com.deepgram.DeepgramClient;
import com.deepgram.types.ListProjectMemberScopesV1Response;
import com.deepgram.types.ListProjectMembersV1Response;
import com.deepgram.types.ListProjectMembersV1ResponseMembersItem;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import java.util.Collections;
import java.util.List;

/**
 * View and manage member scopes/permissions for a Deepgram project. Lists all members and their assigned scopes.
 *
 * <p>Usage: java MemberPermissions
 */
public class MemberPermissions {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Member Permissions (Scopes)");
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

            // List all members
            ListProjectMembersV1Response membersResponse =
                    client.manage().v1().projects().members().list(projectId);
            List<ListProjectMembersV1ResponseMembersItem> members =
                    membersResponse.getMembers().orElse(Collections.emptyList());

            System.out.printf("Members (%d):%n", members.size());
            System.out.println("-".repeat(60));

            if (members.isEmpty()) {
                System.out.println("  No members found");
                return;
            }

            // For each member, retrieve their scopes
            for (ListProjectMembersV1ResponseMembersItem member : members) {
                String memberId = member.getMemberId().orElse("unknown");
                String email = member.getEmail().orElse("unknown");

                System.out.printf("  Member: %s%n", email);
                System.out.printf("  ID:     %s%n", memberId);

                // Also fetch detailed scopes via the scopes endpoint
                try {
                    ListProjectMemberScopesV1Response scopesResponse =
                            client.manage().v1().projects().members().scopes().list(projectId, memberId);
                    List<String> detailedScopes = scopesResponse.getScopes().orElse(Collections.emptyList());

                    if (!detailedScopes.isEmpty()) {
                        System.out.println("  Scopes (detailed):");
                        for (String scope : detailedScopes) {
                            System.out.printf("    - %s%n", scope);
                        }
                    }
                } catch (Exception e) {
                    System.out.printf("  Could not fetch detailed scopes: %s%n", e.getMessage());
                }

                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
