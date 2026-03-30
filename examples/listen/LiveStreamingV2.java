import com.deepgram.DeepgramClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStreamType;
import com.deepgram.resources.listen.v2.types.ListenV2TurnInfoEvent;
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.listen.v2.websocket.V2WebSocketClient;

/**
 * Real-time live transcription using the Listen V2 WebSocket.
 * V2 provides turn-based transcription with enhanced features.
 *
 * <p>Usage: java LiveStreamingV2
 */
public class LiveStreamingV2 {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Live transcription (Listen V2 WebSocket)");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        // Get the V2 WebSocket client
        V2WebSocketClient wsClient = client.listen().v2().v2WebSocket();

        CountDownLatch closeLatch = new CountDownLatch(1);

        try {
            // Register event handlers before connecting
            wsClient.onConnected(() -> {
                System.out.println("Connected to Deepgram V2");
            });

            wsClient.onConnected(connected -> {
                System.out.println("V2 Connected: request_id=" + connected.getRequestId());
            });

            wsClient.onTurnInfo(turnInfo -> {
                String transcript = turnInfo.getTranscript();
                ListenV2TurnInfoEvent event = turnInfo.getEvent();
                double turnIndex = turnInfo.getTurnIndex();

                System.out.printf("[%s] turn=%.0f transcript=\"%s\"%n",
                        event, turnIndex, transcript);
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
                    V2ConnectOptions.builder().model("flux-general-en").build());
            connectFuture.get(10, TimeUnit.SECONDS);

            System.out.println("V2 WebSocket connected. In a real application, you would stream");
            System.out.println("audio data using wsClient.sendMedia(audioData).");
            System.out.println();
            System.out.println("Sending close stream message...");

            // In a real application, you would stream audio data here:
            // wsClient.sendMedia(audioChunk);

            // Close the stream
            wsClient.sendCloseStream(
                    ListenV2CloseStream.builder()
                            .type(ListenV2CloseStreamType.CLOSE_STREAM)
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
