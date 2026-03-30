import com.deepgram.DeepgramClient;
import com.deepgram.types.ListModelsV1Response;
import com.deepgram.types.ListModelsV1ResponseSttModels;
import com.deepgram.types.ListModelsV1ResponseTtsModels;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import java.util.Collections;
import java.util.List;

/**
 * List available speech-to-text and text-to-speech models for a specific project.
 *
 * <p>Usage: java ProjectModels
 */
public class ProjectModels {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Project-Specific Models");
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

            // List models for this project
            ListModelsV1Response modelsResponse =
                    client.manage().v1().projects().models().list(projectId);

            // Display STT models
            List<ListModelsV1ResponseSttModels> sttModels =
                    modelsResponse.getStt().orElse(Collections.emptyList());

            System.out.println("=== Speech-to-Text Models ===");
            System.out.println("-".repeat(60));
            if (sttModels.isEmpty()) {
                System.out.println("  No STT models available");
            } else {
                for (ListModelsV1ResponseSttModels model : sttModels) {
                    String name = model.getName().orElse("unnamed");
                    List<String> languages = model.getLanguages().orElse(Collections.emptyList());

                    System.out.printf("  %s%n", name);
                    if (!languages.isEmpty()) {
                        System.out.printf("    Languages: %s%n", String.join(", ", languages));
                    }
                }
            }

            System.out.println();

            // Display TTS models
            List<ListModelsV1ResponseTtsModels> ttsModels =
                    modelsResponse.getTts().orElse(Collections.emptyList());

            System.out.println("=== Text-to-Speech Models ===");
            System.out.println("-".repeat(60));
            if (ttsModels.isEmpty()) {
                System.out.println("  No TTS models available");
            } else {
                for (ListModelsV1ResponseTtsModels model : ttsModels) {
                    String name = model.getName().orElse("unnamed");
                    System.out.printf("  %s%n", name);
                }
            }

            System.out.println();
            System.out.printf("Total: %d STT models, %d TTS models%n", sttModels.size(), ttsModels.size());

        } catch (Exception e) {
            System.err.println("Error listing models: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
