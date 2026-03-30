import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;
import java.util.Collections;
import java.util.List;

/**
 * Reusable handler utilities for Deepgram API responses. Demonstrates how to structure helper methods for common tasks
 * like transcription with formatted output.
 *
 * <p>Usage: java Handlers
 */
public class Handlers {

    /** Transcribe audio from a URL and print the result. */
    public static void transcribeAndPrint(DeepgramClient client, String audioUrl) {
        System.out.printf("Transcribing: %s%n", audioUrl);

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
                    printTranscription(response);
                    return null;
                }

                @Override
                public Void visit(ListenV1AcceptedResponse accepted) {
                    System.out.println("Request accepted: " + accepted.getRequestId());
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("Transcription error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Extract and print the transcription from a ListenV1Response. */
    public static void printTranscription(ListenV1Response response) {
        ListenV1ResponseResults results = response.getResults();
        if (results == null || results.getChannels().isEmpty()) {
            System.out.println("No transcription results");
            return;
        }

        ListenV1ResponseResultsChannelsItem channel = results.getChannels().get(0);
        List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                channel.getAlternatives().orElse(Collections.emptyList());

        if (alternatives.isEmpty()) {
            System.out.println("No alternatives found");
            return;
        }

        ListenV1ResponseResultsChannelsItemAlternativesItem alt = alternatives.get(0);

        alt.getTranscript().ifPresent(transcript -> {
            System.out.println("Transcript: " + transcript);
        });

        alt.getConfidence().ifPresent(confidence -> {
            System.out.printf("Confidence: %.2f%%%n", confidence * 100);
        });
    }

    /** Demo entry point showing how to use the handler utilities. */
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        System.out.println("Handlers Demo");
        System.out.println("=".repeat(50));
        System.out.println();

        // Transcribe multiple audio files using the handler
        String[] audioUrls = {
            "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav", "https://dpgr.am/spacewalk.wav"
        };

        for (String url : audioUrls) {
            transcribeAndPrint(client, url);
            System.out.println();
        }

        System.out.println("Done!");
    }
}
