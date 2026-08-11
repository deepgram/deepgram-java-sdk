import com.deepgram.DeepgramClient;
import com.deepgram.resources.speak.v2.types.SpeakV2Close;
import com.deepgram.resources.speak.v2.types.SpeakV2Configure;
import com.deepgram.resources.speak.v2.types.SpeakV2Flush;
import com.deepgram.resources.speak.v2.types.SpeakV2Interrupt;
import com.deepgram.resources.speak.v2.types.SpeakV2InterruptPlaybackOffset;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Streaming text-to-speech using the Speak V2 WebSocket. Sends text chunks and receives audio data in real time, saving
 * to a file. Unlike V1, the V2 connection is opened with {@link V2ConnectOptions} (model is required; encoding and
 * sample rate are optional).
 *
 * <p>This example also demonstrates the Flux TTS barge-in controls:
 *
 * <ul>
 *   <li>{@code sendConfigure(...)} — adjust the speech-rate multiplier mid-stream; the server acknowledges with a
 *       {@code ConfigureSuccess} or a typed {@code ConfigureFailure} (e.g. {@code SPEED_OUT_OF_RANGE}).
 *   <li>{@code sendInterrupt(...)} — stop playback (barge-in). Pass a {@link SpeakV2InterruptPlaybackOffset} carrying
 *       the number of audio milliseconds the client has actually played so the server can report {@code text_spoken}
 *       and {@code text_remaining} in the {@code SpeechInterrupted} event. The offset is cumulative from the start of
 *       the session, and each interrupt must advance past the previous one. Omit the offset and
 *       {@code SpeechInterrupted} comes back without the spoken/remaining split.
 * </ul>
 *
 * <p>Usage: java StreamingTtsV2 [output-file]
 */
public class StreamingTtsV2 {
    // Single source of truth for the audio format, shared by the connect() call and the
    // playback-offset math below so the two can't drift.
    private static final SpeakV2SampleRate SAMPLE_RATE = SpeakV2SampleRate.SIXTEEN_THOUSAND;
    private static final int BYTES_PER_SAMPLE = 2; // LINEAR16 mono
    private static final long BYTES_PER_SECOND = Long.parseLong(SAMPLE_RATE.toString()) * BYTES_PER_SAMPLE;

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
        // Total audio bytes received so far — used to estimate the playback offset for barge-in.
        AtomicLong bytesReceived = new AtomicLong(0);

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
                    bytesReceived.addAndGet(bytes.length);
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

            // Barge-in: the server acknowledges sendInterrupt(...) with SpeechInterrupted. When the interrupt
            // carried a playback offset, text_spoken / text_remaining describe where playback was cut off.
            wsClient.onSpeechInterrupted(interrupted -> {
                System.out.printf("Speech interrupted at %d ms played%n", interrupted.getAudioPlayedMs());
                interrupted.getTextSpoken().ifPresent(spoken -> System.out.println("  text spoken:    " + spoken));
                interrupted
                        .getTextRemaining()
                        .ifPresent(remaining -> System.out.println("  text remaining: " + remaining));
            });

            // Mid-stream configure acknowledgements.
            wsClient.onConfigureSuccess(success -> {
                System.out.println("Configure applied: " + success.getApplied());
            });

            wsClient.onConfigureFailure(failure -> {
                System.out.printf("Configure rejected [%s]: %s%n", failure.getCode(), failure.getDescription());
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
                    .sampleRate(SAMPLE_RATE)
                    .build();
            CompletableFuture<Void> connectFuture = wsClient.connect(connectOptions);
            connectFuture.get(10, TimeUnit.SECONDS);

            // Adjust the speech rate mid-stream. Accepted values are 0.85–1.15 in 0.05 increments; anything
            // else comes back as a ConfigureFailure (SPEED_OUT_OF_RANGE / SPEED_INCREMENT_INVALID).
            System.out.println("Configuring speed = 1.05");
            wsClient.sendConfigure(SpeakV2Configure.builder().speed(1.05).build());

            // Send a longer utterance, split across chunks, that we can barge in on.
            String[] chunks = {
                "This is a longer sentence that we will interrupt partway through ",
                "to demonstrate barge-in, where the caller starts speaking before playback finishes."
            };
            for (String chunk : chunks) {
                System.out.println("Sending: \"" + chunk + "\"");
                wsClient.sendSpeak(SpeakV2Speak.builder().text(chunk).build());
            }
            wsClient.sendFlush(SpeakV2Flush.builder().build());

            // Let some audio arrive, then barge in. In a real app you'd trigger this when the user starts
            // speaking; here we interrupt after a fixed delay and report how much audio had played.
            Thread.sleep(1500);
            long playedMs = bytesReceived.get() * 1000 / BYTES_PER_SECOND;
            System.out.printf("%nBarging in at ~%d ms of played audio%n", playedMs);
            wsClient.sendInterrupt(SpeakV2Interrupt.builder()
                    .playbackOffset(SpeakV2InterruptPlaybackOffset.builder()
                            .value((int) playedMs)
                            .build())
                    .build());

            // Send a short follow-up so there is something to hear after the interrupt.
            wsClient.sendSpeak(SpeakV2Speak.builder().text("Sure, go ahead.").build());
            wsClient.sendFlush(SpeakV2Flush.builder().build());

            // Give time for audio to arrive
            Thread.sleep(3000);

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
