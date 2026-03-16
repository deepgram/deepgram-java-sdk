import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import resources.listen.v1.types.ListenV1CloseStream;
import resources.listen.v1.types.ListenV1CloseStreamType;
import resources.listen.v1.types.ListenV1ResultsChannelAlternativesItem;
import resources.listen.v1.websocket.V1ConnectOptions;
import resources.listen.v1.websocket.V1WebSocketClient;
import types.ListenV1Model;

/**
 * Real-time live transcription using the Listen V1 WebSocket.
 * Connects to Deepgram, registers event handlers, and demonstrates the
 * WebSocket-based streaming transcription API.
 *
 * <p>Usage: java LiveStreaming
 */
public class LiveStreaming {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Live transcription (Listen V1 WebSocket)");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        // Get the WebSocket client from the SDK
        V1WebSocketClient wsClient = client.listen().v1().v1WebSocket();

        CountDownLatch closeLatch = new CountDownLatch(1);

        try {
            // Register event handlers before connecting
            wsClient.onConnected(() -> {
                System.out.println("Connected to Deepgram");
            });

            wsClient.onResults(result -> {
                if (result.getChannel() != null
                        && result.getChannel().getAlternatives() != null
                        && !result.getChannel().getAlternatives().isEmpty()) {
                    ListenV1ResultsChannelAlternativesItem alt =
                            result.getChannel().getAlternatives().get(0);
                    String transcript = alt.getTranscript();
                    if (transcript != null && !transcript.isEmpty()) {
                        boolean isFinal = result.getIsFinal().orElse(false);
                        String label = isFinal ? "[final]  " : "[interim]";
                        System.out.printf("%s %s%n", label, transcript);
                    }
                }
            });

            wsClient.onMetadata(metadata -> {
                System.out.println("Metadata received: request_id=" + metadata.getRequestId());
            });

            wsClient.onUtteranceEnd(utteranceEnd -> {
                System.out.println("--- utterance end ---");
            });

            wsClient.onSpeechStarted(speechStarted -> {
                System.out.println("Speech started");
            });

            wsClient.onError(error -> {
                System.err.println("Error: " + error.getMessage());
            });

            wsClient.onDisconnected(reason -> {
                System.out.println("\nConnection closed (code: " + reason.getCode()
                        + ", reason: " + reason.getReason() + ")");
                closeLatch.countDown();
            });

            // Connect to the WebSocket
            CompletableFuture<Void> connectFuture = wsClient.connect(
                    V1ConnectOptions.builder().model(ListenV1Model.NOVA3).build());
            connectFuture.get(10, TimeUnit.SECONDS);

            System.out.println("WebSocket connected. In a real application, you would stream");
            System.out.println("audio data using wsClient.sendMedia(audioData).");
            System.out.println();
            System.out.println("Sending close stream message...");

            // In a real application, you would stream audio data here:
            // wsClient.sendMedia(audioChunk);

            // Close the stream
            wsClient.sendCloseStream(
                    ListenV1CloseStream.builder()
                            .type(ListenV1CloseStreamType.CLOSE_STREAM)
                            .build());

            // Wait for disconnection
            closeLatch.await(15, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            wsClient.disconnect();
        }
    }
}
