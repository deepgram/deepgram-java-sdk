import java.util.Collections;
import java.util.List;
import types.ListModelsV1Response;
import types.ListModelsV1ResponseSttModels;
import types.ListModelsV1ResponseTtsModels;

/**
 * List all available Deepgram models (speech-to-text and text-to-speech).
 *
 * <p>Usage: java ListModels
 */
public class ListModels {
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

        System.out.println("Listing available Deepgram models...");
        System.out.println();

        try {
            ListModelsV1Response response = client.manage().v1().models().list();

            // Display STT models
            List<ListModelsV1ResponseSttModels> sttModels =
                    response.getStt().orElse(Collections.emptyList());
            System.out.println("Speech-to-Text Models (" + sttModels.size() + "):");
            System.out.println("-".repeat(70));
            for (ListModelsV1ResponseSttModels model : sttModels) {
                String name = model.getName().orElse("unknown");
                String canonical = model.getCanonicalName().orElse("");
                String version = model.getVersion().orElse("");
                String uuid = model.getUuid().orElse("");
                System.out.printf("  %-20s %-20s v%-10s %s%n", name, canonical, version, uuid);
            }

            System.out.println();

            // Display TTS models
            List<ListModelsV1ResponseTtsModels> ttsModels =
                    response.getTts().orElse(Collections.emptyList());
            System.out.println("Text-to-Speech Models (" + ttsModels.size() + "):");
            System.out.println("-".repeat(70));
            for (ListModelsV1ResponseTtsModels model : ttsModels) {
                System.out.println("  " + model);
            }

        } catch (Exception e) {
            System.err.println("Error listing models: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
