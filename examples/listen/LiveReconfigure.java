import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.types.ListenV1CloseStream;
import com.deepgram.resources.listen.v1.types.ListenV1CloseStreamType;
import com.deepgram.resources.listen.v1.types.ListenV1Configure;
import com.deepgram.resources.listen.v1.types.ListenV1Error;
import com.deepgram.resources.listen.v1.types.ListenV1ResultsChannelAlternativesItem;
import com.deepgram.resources.listen.v1.websocket.V1ConnectOptions;
import com.deepgram.resources.listen.v1.websocket.V1WebSocketClient;
import com.deepgram.types.ListenV1Model;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Reconfigures an active Nova-3 Listen V1 stream without reconnecting.
 *
 * <p>Usage: {@code DEEPGRAM_API_KEY=... java LiveReconfigure}
 */
public class LiveReconfigure {
    public static void main(String[] args) {
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();
        V1WebSocketClient wsClient = client.listen().v1().v1WebSocket();
        CountDownLatch closeLatch = new CountDownLatch(1);

        try {
            wsClient.onConnected(() -> System.out.println("Connected to Deepgram"));
            wsClient.onResults(result -> {
                if (result.getChannel() != null
                        && result.getChannel().getAlternatives() != null
                        && !result.getChannel().getAlternatives().isEmpty()) {
                    ListenV1ResultsChannelAlternativesItem alternative =
                            result.getChannel().getAlternatives().get(0);
                    if (alternative.getTranscript() != null
                            && !alternative.getTranscript().isEmpty()) {
                        System.out.println(alternative.getTranscript());
                    }
                }
            });
            wsClient.onErrorMessage(LiveReconfigure::printListenError);
            wsClient.onError(error -> System.err.println("WebSocket error occurred: " + error.getMessage()));
            wsClient.onDisconnected(reason -> {
                System.out.println("Connection closed.");
                closeLatch.countDown();
            });

            wsClient.connect(V1ConnectOptions.builder()
                            .model(ListenV1Model.NOVA3)
                            .build())
                    .get(10, TimeUnit.SECONDS);

            wsClient.sendConfigure(ListenV1Configure.builder()
                            .keyterms(List.of("Deepgram", "Nova-3"))
                            .features(Map.of("numerals", true))
                            .build())
                    .get(10, TimeUnit.SECONDS);
            System.out.println("Sent live reconfiguration for keyterms and numerals.");

            // Stream audio after this point, for example: wsClient.sendMedia(audioChunk);
            wsClient.sendCloseStream(ListenV1CloseStream.builder()
                            .type(ListenV1CloseStreamType.CLOSE_STREAM)
                            .build())
                    .get(10, TimeUnit.SECONDS);
            closeLatch.await(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Unable to run the live reconfiguration example: " + e.getMessage());
        } finally {
            wsClient.close();
            client.close();
        }
    }

    private static void printListenError(ListenV1Error error) {
        System.err.println("Listen error (" + error.getVariant() + "): " + error.getDescription());
        error.getCode().ifPresent(code -> System.err.println("Error code: " + code));
    }
}
