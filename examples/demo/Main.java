import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListProjectsV1Response;
import com.deepgram.types.ListProjectsV1ResponseProjectsItem;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;
import java.util.Collections;
import java.util.List;

/**
 * Comprehensive demo of the Deepgram Java SDK. Demonstrates multiple API features in a single program: authentication,
 * project listing, and speech-to-text transcription.
 *
 * <p>Usage: java Main
 */
public class Main {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("=".repeat(60));
        System.out.println("  Deepgram Java SDK Demo");
        System.out.println("=".repeat(60));
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        // Step 1: Verify authentication by listing projects
        handleListProjects(client);

        // Step 2: Transcribe audio from URL
        handleTranscription(client);

        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("  Demo complete!");
        System.out.println("=".repeat(60));
    }

    private static void handleListProjects(DeepgramClient client) {
        System.out.println("Step 1: List Projects");
        System.out.println("-".repeat(40));

        try {
            ListProjectsV1Response response = client.manage().v1().projects().list();
            List<ListProjectsV1ResponseProjectsItem> projects =
                    response.getProjects().orElse(Collections.emptyList());

            System.out.printf("  Found %d project(s)%n", projects.size());
            for (ListProjectsV1ResponseProjectsItem project : projects) {
                String name = project.getName().orElse("unnamed");
                String id = project.getProjectId().orElse("unknown");
                System.out.printf("  - %s (%s)%n", name, id);
            }
        } catch (Exception e) {
            System.err.println("  Error listing projects: " + e.getMessage());
        }

        System.out.println();
    }

    private static void handleTranscription(DeepgramClient client) {
        System.out.println("Step 2: Transcribe Audio");
        System.out.println("-".repeat(40));

        String audioUrl = "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav";
        System.out.println("  Audio URL: " + audioUrl);

        try {
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url(audioUrl)
                    .model(MediaTranscribeRequestModel.NOVA3)
                    .smartFormat(true)
                    .build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

            result.visit(new MediaTranscribeResponse.Visitor<Void>() {
                @Override
                public Void visit(ListenV1Response response) {
                    ListenV1ResponseResults results = response.getResults();
                    if (results != null && !results.getChannels().isEmpty()) {
                        ListenV1ResponseResultsChannelsItem channel =
                                results.getChannels().get(0);
                        List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                                channel.getAlternatives().orElse(Collections.emptyList());
                        if (!alternatives.isEmpty()) {
                            ListenV1ResponseResultsChannelsItemAlternativesItem alt = alternatives.get(0);

                            alt.getTranscript().ifPresent(transcript -> {
                                System.out.println();
                                System.out.println("  Transcript:");
                                System.out.println("  " + "-".repeat(38));
                                System.out.println("  " + transcript);
                                System.out.println("  " + "-".repeat(38));
                            });

                            alt.getConfidence()
                                    .ifPresent(confidence ->
                                            System.out.printf("  Confidence: %.2f%%%n", confidence * 100));
                        }
                    }
                    return null;
                }

                @Override
                public Void visit(com.deepgram.types.ListenV1AcceptedResponse accepted) {
                    System.out.println("  Request accepted: " + accepted.getRequestId());
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("  Error transcribing audio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
