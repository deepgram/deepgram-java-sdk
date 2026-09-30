import com.deepgram.DeepgramClient;
import com.deepgram.resources.speak.v2.audio.requests.SpeakV2Request;
import com.deepgram.resources.speak.v2.audio.types.AudioGenerateRequestEncoding;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Generates Flux TTS batch audio with pause and pronunciation controls.
 *
 * <p>Usage: {@code java FluxTtsControls [output-directory]}
 *
 * <p>Pause and pronunciation controls cannot be combined in the same Flux TTS request. This example sends them
 * separately.
 */
public class FluxTtsControls {
    private static final String MODEL = "flux-alexis-en";

    public static void main(String[] args) {
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        Path outputDirectory = args.length == 1 ? Path.of(args[0]) : Path.of(".");

        try (DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build()) {
            Files.createDirectories(outputDirectory);

            generate(
                    client,
                    "Your confirmation number is four seven two. \\{pause:500ms\\} Is there anything else I can help with?",
                    outputDirectory.resolve("flux-pause.mp3"));
            generate(
                    client,
                    "Take \\{\"word\": \"dupilumab\", \"pronounce\": \"duːˈpɪljuːmæb\"\\} twice daily.",
                    outputDirectory.resolve("flux-pronunciation.mp3"));
        } catch (Exception e) {
            System.err.println("Error generating Flux TTS Controls audio: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generate(DeepgramClient client, String text, Path outputPath) throws Exception {
        SpeakV2Request request = SpeakV2Request.builder()
                .model(MODEL)
                .text(text)
                .encoding(AudioGenerateRequestEncoding.MP3)
                .build();

        try (InputStream audio = client.speak().v2().audio().generate(request)) {
            long bytes = Files.copy(audio, outputPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.printf("Audio saved to %s (%d bytes)%n", outputPath, bytes);
        }
    }
}
