import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import resources.listen.v1.media.requests.MediaTranscribeRequestOctetStream;
import resources.listen.v1.media.types.MediaTranscribeResponse;
import types.ListenV1Response;
import types.ListenV1ResponseResults;
import types.ListenV1ResponseResultsChannelsItem;
import types.ListenV1ResponseResultsChannelsItemAlternativesItem;

/**
 * Transcribe a local audio file using Deepgram's speech-to-text REST API.
 *
 * <p>Usage: java TranscribeFile <path-to-audio-file>
 */
public class TranscribeFile {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Require audio file argument
        if (args.length < 1) {
            System.out.println("Usage: java TranscribeFile <path-to-audio-file>");
            System.out.println();
            System.out.println("To download a Deepgram example audio file, run:");
            System.out.println("  curl -O https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav");
            System.exit(1);
        }

        Path audioPath = Paths.get(args[0]);
        if (!Files.exists(audioPath)) {
            System.err.println("File not found: " + audioPath);
            System.exit(1);
        }

        try {
            // Read audio file
            byte[] audioData = Files.readAllBytes(audioPath);
            System.out.printf("Transcribing audio file: %s%n", audioPath);
            System.out.printf("File size: %d bytes%n%n", audioData.length);

            // Create client
            DeepgramClient client = DeepgramClient.builder()
                    .apiKey(apiKey)
                    .build();

            // Transcribe from file data
            MediaTranscribeRequestOctetStream request = MediaTranscribeRequestOctetStream.builder()
                    .body(audioData)
                    .build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeFile(request);

            // Display results using visitor
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

                            alt.getTranscript().ifPresent(transcript -> {
                                System.out.println("Transcription:");
                                System.out.println("-".repeat(50));
                                System.out.println(transcript);
                                System.out.println("-".repeat(50));
                            });

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

        } catch (IOException e) {
            System.err.println("Error reading audio file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error transcribing audio: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
