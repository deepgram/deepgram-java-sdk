import com.deepgram.DeepgramClient;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import com.deepgram.resources.listen.v1.media.requests.MediaTranscribeRequestOctetStream;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestModel;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;

/**
 * Demonstrates transcribing audio from downloaded bytes (file upload).
 * Downloads an audio file into memory and sends the raw bytes to Deepgram.
 *
 * <p>Usage: java FileUploadTypes
 */
public class FileUploadTypes {
    private static final String SAMPLE_AUDIO_URL =
            "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav";

    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("File Upload Transcription");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            // Download the audio file into a byte array
            System.out.println("Downloading audio from: " + SAMPLE_AUDIO_URL);
            byte[] audioBytes = downloadAudio(SAMPLE_AUDIO_URL);
            System.out.printf("Downloaded %d bytes%n%n", audioBytes.length);

            // Method 1: Simple file upload with just bytes
            System.out.println("=== Method 1: Simple byte[] upload ===");
            MediaTranscribeResponse result1 = client.listen().v1().media().transcribeFile(audioBytes);
            printTranscript(result1);

            System.out.println();

            // Method 2: File upload with options using MediaTranscribeRequestOctetStream
            System.out.println("=== Method 2: File upload with options ===");
            MediaTranscribeRequestOctetStream request = MediaTranscribeRequestOctetStream.builder()
                    .body(audioBytes)
                    .model(MediaTranscribeRequestModel.NOVA3)
                    .smartFormat(true)
                    .build();

            MediaTranscribeResponse result2 = client.listen().v1().media().transcribeFile(request);
            printTranscript(result2);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static byte[] downloadAudio(String url) throws Exception {
        try (InputStream in = URI.create(url).toURL().openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }

    private static void printTranscript(MediaTranscribeResponse response) {
        response.visit(new MediaTranscribeResponse.Visitor<Void>() {
            @Override
            public Void visit(ListenV1Response resp) {
                ListenV1ResponseResults results = resp.getResults();
                if (results != null && !results.getChannels().isEmpty()) {
                    ListenV1ResponseResultsChannelsItem channel = results.getChannels().get(0);
                    List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                            channel.getAlternatives().orElse(Collections.emptyList());
                    if (!alternatives.isEmpty()) {
                        alternatives.get(0).getTranscript().ifPresent(transcript -> {
                            System.out.println("Transcript:");
                            System.out.println("-".repeat(50));
                            System.out.println(transcript);
                            System.out.println("-".repeat(50));
                        });
                    }
                }
                return null;
            }

            @Override
            public Void visit(ListenV1AcceptedResponse accepted) {
                System.out.println("Request accepted: " + accepted.getRequestId());
                return null;
            }
        });
    }
}
