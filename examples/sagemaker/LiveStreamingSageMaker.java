import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.types.ListenV1CloseStream;
import com.deepgram.resources.listen.v1.types.ListenV1CloseStreamType;
import com.deepgram.resources.listen.v1.types.ListenV1ResultsChannelAlternativesItem;
import com.deepgram.resources.listen.v1.websocket.V1ConnectOptions;
import com.deepgram.resources.listen.v1.websocket.V1WebSocketClient;
import com.deepgram.sagemaker.SageMakerConfig;
import com.deepgram.sagemaker.SageMakerTransportFactory;
import com.deepgram.types.ListenV1Model;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import okio.ByteString;

/**
 * Live transcription via SageMaker endpoint.
 *
 * <p>Streams a WAV file through a Deepgram model running on SageMaker using HTTP/2 bidirectional streaming. Audio is
 * paced to real-time to simulate a live microphone stream.
 *
 * <p>Requires the {@code deepgram-sagemaker-java} transport library.
 *
 * <p>Usage:
 *
 * <pre>
 *   export SAGEMAKER_ENDPOINT=my-deepgram-endpoint
 *   export AWS_REGION=us-east-2
 *   java LiveStreamingSageMaker [path/to/audio.wav]
 * </pre>
 *
 * <p>Environment variables:
 *
 * <ul>
 *   <li>{@code SAGEMAKER_ENDPOINT} — SageMaker endpoint name (default: "deepgram-nova-3")
 *   <li>{@code AWS_REGION} — AWS region (default: "us-west-2")
 * </ul>
 */
public class LiveStreamingSageMaker {

    public static void main(String[] args) throws Exception {
        String endpointName = System.getenv().getOrDefault("SAGEMAKER_ENDPOINT", "deepgram-nova-3");
        String region = System.getenv().getOrDefault("AWS_REGION", "us-west-2");

        // Audio file — from args or default
        Path audioFile = args.length > 0 ? Path.of(args[0]) : Path.of("spacewalk.wav");
        if (!Files.exists(audioFile)) {
            System.err.println("Audio file not found: " + audioFile);
            System.err.println("Download from: https://dpgr.am/spacewalk.wav");
            System.exit(1);
        }

        // Create the SageMaker transport factory
        SageMakerConfig config = SageMakerConfig.builder()
                .endpointName(endpointName)
                .region(region)
                .build();
        SageMakerTransportFactory factory = new SageMakerTransportFactory(config);

        // Build the SDK client — apiKey is unused, SageMaker uses AWS credentials
        DeepgramClient client = DeepgramClient.builder()
                .apiKey("unused")
                .transportFactory(factory)
                .build();

        System.out.println("Live transcription via SageMaker transport");
        System.out.println("Endpoint: " + endpointName);
        System.out.println("Region:   " + region);
        System.out.println();

        // From here, the code is identical to any standard Deepgram SDK usage
        V1WebSocketClient wsClient = client.listen().v1().v1WebSocket();
        CountDownLatch done = new CountDownLatch(1);

        wsClient.onResults(result -> {
            if (result.getChannel() != null
                    && result.getChannel().getAlternatives() != null
                    && !result.getChannel().getAlternatives().isEmpty()) {
                ListenV1ResultsChannelAlternativesItem alt =
                        result.getChannel().getAlternatives().get(0);
                String transcript = alt.getTranscript();
                if (transcript != null && !transcript.isEmpty()) {
                    boolean isFinal = result.getIsFinal().orElse(false);
                    System.out.printf("%s %s%n", isFinal ? "[final]  " : "[interim]", transcript);
                }
            }
        });

        wsClient.onMetadata(metadata -> {
            System.out.println("Metadata: request_id=" + metadata.getRequestId());
        });

        wsClient.onError(error -> {
            System.err.println("Error: " + error.getMessage());
            done.countDown();
        });

        wsClient.onDisconnected(reason -> {
            System.out.println("Closed (code: " + reason.getCode() + ")");
            done.countDown();
        });

        // Connect
        CompletableFuture<Void> connectFuture = wsClient.connect(
                V1ConnectOptions.builder().model(ListenV1Model.NOVA3).build());
        connectFuture.get(10, TimeUnit.SECONDS);
        System.out.println("Connected. Streaming audio...\n");

        // Parse WAV header for pacing
        int sampleRate;
        int blockAlign;
        try (RandomAccessFile raf = new RandomAccessFile(audioFile.toFile(), "r")) {
            raf.skipBytes(24);
            byte[] srBytes = new byte[4];
            raf.read(srBytes);
            sampleRate = ByteBuffer.wrap(srBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

            raf.skipBytes(4); // skip byte rate
            byte[] baBytes = new byte[2];
            raf.read(baBytes);
            blockAlign = ByteBuffer.wrap(baBytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
        }

        // Read and send audio paced to real-time (including WAV header)
        byte[] audio = Files.readAllBytes(audioFile);
        int chunkSize = 8192;
        double framesPerChunk = (double) chunkSize / blockAlign;
        long sleepMicros = (long) (framesPerChunk / sampleRate * 1_000_000);

        for (int i = 0; i < audio.length; i += chunkSize) {
            int end = Math.min(i + chunkSize, audio.length);
            byte[] chunk = new byte[end - i];
            System.arraycopy(audio, i, chunk, 0, chunk.length);
            wsClient.sendMedia(ByteString.of(chunk));
            TimeUnit.MICROSECONDS.sleep(sleepMicros);
        }

        // Signal end of audio
        wsClient.sendCloseStream(ListenV1CloseStream.builder()
                .type(ListenV1CloseStreamType.CLOSE_STREAM)
                .build());

        done.await(60, TimeUnit.SECONDS);
        wsClient.disconnect();
        factory.shutdown();
        System.out.println("Done.");
    }
}
