import java.util.Collections;
import java.util.List;
import types.ListProjectMembersV1Response;
import types.ListProjectMembersV1ResponseMembersItem;
import types.ListProjectsV1Response;
import types.ListProjectsV1ResponseProjectsItem;

/**
 * List and manage project members.
 *
 * <p>Usage: java ManageMembers
 */
public class ManageMembers {
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

        System.out.println("Manage Project Members");
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

            // List members
            ListProjectMembersV1Response membersResponse =
                    client.manage().v1().projects().members().list(projectId);

            List<ListProjectMembersV1ResponseMembersItem> members =
                    membersResponse.getMembers().orElse(Collections.emptyList());

            System.out.printf("Project Members (%d):%n", members.size());
            System.out.println("-".repeat(60));

            for (ListProjectMembersV1ResponseMembersItem member : members) {
                String memberId = member.getMemberId().orElse("unknown");
                String email = member.getEmail().orElse("unknown");
                System.out.printf("  Member ID: %s%n", memberId);
                System.out.printf("  Email:     %s%n", email);
                System.out.println();
            }

            // Note: To remove a member, use:
            // client.manage().v1().projects().members().delete(projectId, memberId);

        } catch (Exception e) {
            System.err.println("Error managing members: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
