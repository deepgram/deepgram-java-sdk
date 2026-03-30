import com.deepgram.DeepgramClient;
import java.util.Collections;
import java.util.List;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;

/**
 * Transcribe audio from a URL using Deepgram's speech-to-text REST API.
 *
 * <p>Usage: java TranscribeUrl [audio-url]
 */
public class TranscribeUrl {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Audio URL to transcribe
        String audioUrl = "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav";
        if (args.length > 0) {
            audioUrl = args[0];
        }

        System.out.println("Transcribing audio from URL...");
        System.out.println("URL: " + audioUrl);
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            // Transcribe from URL
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url(audioUrl)
                    .model(MediaTranscribeRequestModel.NOVA3)
                    .smartFormat(true)
                    .build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

            // The response is a union type: either ListenV1Response or ListenV1AcceptedResponse.
            // Use the visitor pattern to handle both cases.
            result.visit(new MediaTranscribeResponse.Visitor<Void>() {
                @Override
                public Void visit(ListenV1Response response) {
                    ListenV1ResponseResults results = response.getResults();
                    if (results != null && !results.getChannels().isEmpty()) {
                        ListenV1ResponseResultsChannelsItem channel = results.getChannels().get(0);
                        List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                                channel.getAlternatives().orElse(Collections.emptyList());
                        if (!alternatives.isEmpty()) {
                            ListenV1ResponseResultsChannelsItemAlternativesItem alt =
                                    alternatives.get(0);

                            // Display transcription
                            alt.getTranscript().ifPresent(transcript -> {
                                System.out.println("Transcription:");
                                System.out.println("-".repeat(50));
                                System.out.println(transcript);
                                System.out.println("-".repeat(50));
                            });

                            // Display confidence
                            alt.getConfidence().ifPresent(confidence ->
                                    System.out.printf("%nConfidence: %.2f%%%n", confidence * 100));
                        }
                    } else {
                        System.out.println("No transcription results found");
                    }
                    return null;
                }

                @Override
                public Void visit(types.ListenV1AcceptedResponse accepted) {
                    System.out.println("Request accepted: " + accepted.getRequestId());
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("Error transcribing audio: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
