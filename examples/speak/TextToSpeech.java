import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import resources.speak.v1.audio.requests.SpeakV1Request;

/**
 * Convert text to speech using Deepgram's TTS REST API and save the audio to a file.
 *
 * <p>Usage: java TextToSpeech [output-file]
 */
public class TextToSpeech {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        String outputFile = "output.mp3";
        if (args.length > 0) {
            outputFile = args[0];
        }

        String text = "Hello! This is a text-to-speech example using the Deepgram Java SDK. "
                + "Deepgram's TTS API produces natural-sounding speech from text.";

        System.out.println("Text-to-Speech (REST API)");
        System.out.println("Text: " + text);
        System.out.println("Output: " + outputFile);
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            // Build the TTS request
            SpeakV1Request request = SpeakV1Request.builder()
                    .text(text)
                    .build();

            // Generate audio
            InputStream audioStream = client.speak().v1().audio().generate(request);

            // Save audio to file
            Path outputPath = Paths.get(outputFile);
            long bytes = Files.copy(audioStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
            audioStream.close();

            System.out.printf("Audio saved to %s (%d bytes)%n", outputPath, bytes);

        } catch (Exception e) {
            System.err.println("Error generating speech: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
