import com.deepgram.DeepgramClient;
import com.deepgram.DeepgramClientBuilder;
import com.deepgram.core.Environment;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;
import com.deepgram.resources.listen.v2.types.ListenV2ForceEndTurn;
import com.deepgram.resources.listen.v2.types.ListenV2TurnInfoEvent;
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.listen.v2.websocket.V2WebSocketClient;
import com.deepgram.types.ListenV2Encoding;
import com.deepgram.types.ListenV2Model;
import com.deepgram.types.ListenV2SampleRate;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import okio.ByteString;

/**
 * Ending a Listen V2 turn on demand with {@code ForceEndTurn}, and reading back which cause ended the turn via
 * {@code TurnInfo.getTrigger()}.
 *
 * <p>Normally Flux decides when a turn is over. {@code ForceEndTurn} lets the application decide instead — useful when
 * something outside the audio tells you the speaker is done, such as a push-to-talk button being released. The turn
 * ends immediately regardless of end-of-turn confidence, and the connection stays open for the next turn.
 *
 * <p>{@code ForceEndTurn} is gated per deployment. Where it is not enabled the server replies
 * {@code UNPARSABLE_CLIENT_MESSAGE} and closes the connection; this example reports that and exits rather than failing.
 * Point it at an environment with the feature enabled via DEEPGRAM_BASE_URL.
 *
 * <p>Usage: java ForceEndTurn
 */
public class ForceEndTurn {

    private static final String AUDIO_URL = "https://dpgr.am/spacewalk.wav";

    // spacewalk.wav is 16-bit mono PCM at 44.1kHz behind a standard 44-byte RIFF header.
    private static final int SAMPLE_RATE = 44100;
    private static final int WAV_HEADER_BYTES = 44;
    private static final int CHUNK_BYTES = SAMPLE_RATE / 10 * 2; // 100ms of audio

    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Force-end-turn (Listen V2 WebSocket)");
        System.out.println();

        DeepgramClientBuilder builder = DeepgramClient.builder().apiKey(apiKey);

        // ForceEndTurn is not enabled on every deployment. Set DEEPGRAM_BASE_URL (wss://...) to
        // target one where it is.
        String baseUrl = System.getenv("DEEPGRAM_BASE_URL");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            String https = baseUrl.startsWith("wss://") ? "https://" + baseUrl.substring("wss://".length()) : baseUrl;
            builder.environment(Environment.custom()
                    .base(https)
                    .production(baseUrl)
                    .agent(baseUrl)
                    .agentRest(https)
                    .build());
            System.out.println("Targeting " + baseUrl);
        }

        DeepgramClient client = builder.build();
        V2WebSocketClient wsClient = client.listen().v2().v2WebSocket();

        CountDownLatch turnStarted = new CountDownLatch(1);
        CountDownLatch endOfTurn = new CountDownLatch(1);
        AtomicBoolean featureDisabled = new AtomicBoolean(false);

        try {
            byte[] audio = download(AUDIO_URL);

            wsClient.onConnected(connected -> System.out.println("Connected: request_id=" + connected.getRequestId()));

            wsClient.onTurnInfo(turn -> {
                if (turn.getEvent().equals(ListenV2TurnInfoEvent.START_OF_TURN)) {
                    turnStarted.countDown();
                    System.out.println("[StartOfTurn] turn=" + turn.getTurnIndex());
                } else if (turn.getEvent().equals(ListenV2TurnInfoEvent.END_OF_TURN)) {
                    // trigger is present on EndOfTurn and only there. "manual" means a ForceEndTurn
                    // ended this turn; "model" means Flux's own detection did; "timeout" means
                    // eot_timeout_ms elapsed. It is an open enum, so tolerate unfamiliar values.
                    System.out.printf(
                            "[EndOfTurn]   turn=%d trigger=%s end_of_turn_confidence=%.4f%n",
                            turn.getTurnIndex(),
                            turn.getTrigger().orElse("<not sent by this deployment>"),
                            turn.getEndOfTurnConfidence());
                    System.out.println("              transcript: \"" + turn.getTranscript() + "\"");
                    endOfTurn.countDown();
                }
            });

            wsClient.onErrorMessage(error -> {
                if ("UNPARSABLE_CLIENT_MESSAGE".equals(error.getCode())) {
                    featureDisabled.set(true);
                }
                System.err.println("Server error: " + error.getCode() + " - " + error.getDescription());
                endOfTurn.countDown();
            });

            wsClient.onError(error -> System.err.println("Error: " + error.getMessage()));

            wsClient.connect(V2ConnectOptions.builder()
                            .model(ListenV2Model.FLUX_GENERAL_EN)
                            .encoding(ListenV2Encoding.LINEAR16)
                            .sampleRate(ListenV2SampleRate.of(SAMPLE_RATE))
                            .build())
                    .get(15, TimeUnit.SECONDS);

            // Stream audio until a turn is actually underway. Forcing an end before StartOfTurn
            // would have no turn to end.
            System.out.println("Streaming audio...");
            int offset = WAV_HEADER_BYTES;
            for (int i = 0; i < 40 && offset < audio.length; i++) {
                int len = Math.min(CHUNK_BYTES, audio.length - offset);
                wsClient.sendMedia(ByteString.of(audio, offset, len));
                offset += len;
                Thread.sleep(100);
                if (turnStarted.getCount() == 0 && i >= 20) {
                    break;
                }
            }

            // Cut the turn off mid-sentence. Flux would have kept it open; we end it anyway.
            System.out.println("Sending ForceEndTurn mid-sentence...");
            wsClient.sendForceEndTurn(ListenV2ForceEndTurn.builder().build());

            endOfTurn.await(15, TimeUnit.SECONDS);

            if (featureDisabled.get()) {
                System.out.println();
                System.out.println("ForceEndTurn is not enabled on this deployment.");
                System.out.println("Set DEEPGRAM_BASE_URL to an environment where it is enabled.");
            } else {
                wsClient.sendCloseStream(ListenV2CloseStream.builder().build());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            wsClient.disconnect();
        }
    }

    private static byte[] download(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        try (InputStream in = conn.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            return out.toByteArray();
        } finally {
            conn.disconnect();
        }
    }
}
