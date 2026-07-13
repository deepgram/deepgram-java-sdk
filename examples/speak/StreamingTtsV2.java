import com.deepgram.DeepgramClient;
import com.deepgram.resources.speak.v2.types.SpeakV2Close;
import com.deepgram.resources.speak.v2.types.SpeakV2Flush;
import com.deepgram.resources.speak.v2.types.SpeakV2Speak;
import com.deepgram.resources.speak.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.speak.v2.websocket.V2WebSocketClient;
import com.deepgram.types.SpeakV2Encoding;
import com.deepgram.types.SpeakV2SampleRate;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Streaming text-to-speech using the Speak V2 WebSocket. Sends text chunks and receives audio data in real time, saving
 * to a file. Unlike V1, the V2 connection is opened with {@link V2ConnectOptions} (model is required; encoding and
 * sample rate are optional).
 *
 * <p>Usage: java StreamingTtsV2 [output-file]
 */
public class StreamingTtsV2 {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        String outputFile = "output_streaming_v2.wav";
        if (args.length > 0) {
            outputFile = args[0];
        }

        System.out.println("Streaming Text-to-Speech (Speak V2 WebSocket)");
        System.out.println("Output: " + outputFile);
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        // Get the Speak V2 WebSocket client
        V2WebSocketClient wsClient = client.speak().v2().v2WebSocket();

        CountDownLatch closeLatch = new CountDownLatch(1);
        AtomicInteger audioChunks = new AtomicInteger(0);

        try (OutputStream audioOutput = new FileOutputStream(outputFile)) {
            final String outputPath = outputFile;

            // Register event handlers before connecting
            wsClient.onConnected(() -> {
                System.out.println("Connected to Deepgram TTS WebSocket (V2)");
            });

            wsClient.onSpeakV2Audio(audioData -> {
                try {
                    // Audio data arrives as ByteString
                    byte[] bytes = audioData.toByteArray();
                    audioOutput.write(bytes);
                    int count = audioChunks.incrementAndGet();
                    System.out.printf("Received audio chunk #%d (%d bytes)%n", count, bytes.length);
                } catch (Exception e) {
                    System.err.println("Error writing audio: " + e.getMessage());
                }
            });

            wsClient.onSpeechStarted(started -> {
                System.out.println("Speech started: " + started);
            });

            wsClient.onFlushed(flushed -> {
                System.out.println("Audio flushed - all queued text has been converted");
            });

            wsClient.onWarning(warning -> {
                System.out.println("Warning: " + warning);
            });

            wsClient.onErrorMessage(error -> {
                System.err.println("Server error: " + error);
            });

            wsClient.onError(error -> {
                System.err.println("Error: " + error.getMessage());
            });

            wsClient.onDisconnected(reason -> {
                // audioOutput is closed by try-with-resources when the block exits.
                System.out.println(
                        "\nConnection closed (code: " + reason.getCode() + ", reason: " + reason.getReason() + ")");
                closeLatch.countDown();
            });

            // Connect to the WebSocket. Model is required; encoding and sample rate are optional.
            V2ConnectOptions connectOptions = V2ConnectOptions.builder()
                    .model("flux-alexis-en")
                    .encoding(SpeakV2Encoding.LINEAR16)
                    .sampleRate(SpeakV2SampleRate.SIXTEEN_THOUSAND)
                    .build();
            CompletableFuture<Void> connectFuture = wsClient.connect(connectOptions);
            connectFuture.get(10, TimeUnit.SECONDS);

            // Send text chunks for TTS conversion
            String[] sentences = {
                "Hello, this is a streaming text-to-speech demo.",
                "Each sentence is sent as a separate message.",
                "The audio is generated and streamed back in real time."
            };

            for (String sentence : sentences) {
                System.out.println("Sending: \"" + sentence + "\"");
                wsClient.sendSpeak(SpeakV2Speak.builder().text(sentence).build());
            }

            // Flush to ensure all text is processed
            wsClient.sendFlush(SpeakV2Flush.builder().build());

            // Give time for audio to arrive
            Thread.sleep(5000);

            // Close the connection
            System.out.println("\nClosing connection...");
            wsClient.sendClose(SpeakV2Close.builder().build());

            closeLatch.await(10, TimeUnit.SECONDS);

            System.out.printf("%nTotal audio chunks received: %d%n", audioChunks.get());
            System.out.printf("Audio saved to %s%n", outputPath);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            wsClient.disconnect();
        }
    }
}
