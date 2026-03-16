import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import resources.speak.v1.types.SpeakV1Close;
import resources.speak.v1.types.SpeakV1CloseType;
import resources.speak.v1.types.SpeakV1Flush;
import resources.speak.v1.types.SpeakV1FlushType;
import resources.speak.v1.types.SpeakV1Text;
import resources.speak.v1.websocket.V1WebSocketClient;

/**
 * Streaming text-to-speech using the Speak V1 WebSocket.
 * Sends text chunks and receives audio data in real time, saving to a file.
 *
 * <p>Usage: java StreamingTts [output-file]
 */
public class StreamingTts {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        String outputFile = "output_streaming.wav";
        if (args.length > 0) {
            outputFile = args[0];
        }

        System.out.println("Streaming Text-to-Speech (WebSocket)");
        System.out.println("Output: " + outputFile);
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        // Get the Speak WebSocket client
        V1WebSocketClient wsClient = client.speak().v1().v1WebSocket();

        CountDownLatch closeLatch = new CountDownLatch(1);
        AtomicInteger audioChunks = new AtomicInteger(0);

        try {
            OutputStream audioOutput = new FileOutputStream(outputFile);
            final String outputPath = outputFile;

            // Register event handlers before connecting
            wsClient.onConnected(() -> {
                System.out.println("Connected to Deepgram TTS WebSocket");
            });

            wsClient.onSpeakV1Audio(audioData -> {
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

            wsClient.onMetadata(metadata -> {
                System.out.println("Metadata: " + metadata);
            });

            wsClient.onFlushed(flushed -> {
                System.out.println("Audio flushed - all queued text has been converted");
            });

            wsClient.onWarning(warning -> {
                System.out.println("Warning: " + warning);
            });

            wsClient.onError(error -> {
                System.err.println("Error: " + error.getMessage());
            });

            wsClient.onDisconnected(reason -> {
                try {
                    audioOutput.close();
                } catch (Exception e) {
                    // ignore
                }
                System.out.println("\nConnection closed (code: " + reason.getCode()
                        + ", reason: " + reason.getReason() + ")");
                closeLatch.countDown();
            });

            // Connect to the WebSocket
            CompletableFuture<Void> connectFuture = wsClient.connect();
            connectFuture.get(10, TimeUnit.SECONDS);

            // Send text chunks for TTS conversion
            String[] sentences = {
                "Hello, this is a streaming text-to-speech demo.",
                "Each sentence is sent as a separate message.",
                "The audio is generated and streamed back in real time."
            };

            for (String sentence : sentences) {
                System.out.println("Sending: \"" + sentence + "\"");
                wsClient.sendText(
                        SpeakV1Text.builder()
                                .text(sentence)
                                .build());
            }

            // Flush to ensure all text is processed
            wsClient.sendFlush(
                    SpeakV1Flush.builder()
                            .type(SpeakV1FlushType.FLUSH)
                            .build());

            // Give time for audio to arrive
            Thread.sleep(5000);

            // Close the connection
            System.out.println("\nClosing connection...");
            wsClient.sendClose(
                    SpeakV1Close.builder()
                            .type(SpeakV1CloseType.CLOSE)
                            .build());

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
