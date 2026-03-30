import com.deepgram.DeepgramClient;
import java.util.Collections;
import java.util.List;
import com.deepgram.resources.manage.v1.projects.members.invites.requests.CreateProjectInviteV1Request;
import com.deepgram.types.ListProjectInvitesV1Response;
import com.deepgram.types.ListProjectInvitesV1ResponseInvitesItem;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;

/**
 * Manage project invitations: list existing invites and send new ones.
 *
 * <p>Usage: java ManageInvites
 */
public class ManageInvites {
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

        System.out.println("Manage Project Invites");
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

            // List existing invites
            ListProjectInvitesV1Response invitesResponse =
                    client.manage().v1().projects().members().invites().list(projectId);

            List<ListProjectInvitesV1ResponseInvitesItem> invites =
                    invitesResponse.getInvites().orElse(Collections.emptyList());

            System.out.printf("Pending Invites (%d):%n", invites.size());
            System.out.println("-".repeat(60));

            if (invites.isEmpty()) {
                System.out.println("  No pending invites");
            } else {
                for (ListProjectInvitesV1ResponseInvitesItem invite : invites) {
                    System.out.printf("  %s%n", invite);
                }
            }

            System.out.println();

            // Note: To send an invite, uncomment the following:
            //
            // CreateProjectInviteV1Request inviteRequest = CreateProjectInviteV1Request.builder()
            //         .email("user@example.com")
            //         .scope("member")
            //         .build();
            //
            // client.manage().v1().projects().members().invites()
            //         .create(projectId, inviteRequest);
            // System.out.println("Invite sent to user@example.com");

            // Note: To delete an invite, use:
            // client.manage().v1().projects().members().invites()
            //         .delete(projectId, "user@example.com");

            System.out.println("To send or delete invites, uncomment the relevant code sections.");

        } catch (Exception e) {
            System.err.println("Error managing invites: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
