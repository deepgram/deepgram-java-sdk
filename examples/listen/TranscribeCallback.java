import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeRequestCallbackMethod;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;

/**
 * Submit an audio URL for asynchronous transcription with a callback URL.
 * Deepgram will POST the results to your callback URL when processing completes.
 *
 * <p>Usage: java TranscribeCallback [callback-url] [audio-url]
 */
public class TranscribeCallback {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Callback URL -- Deepgram will POST results here when transcription is complete.
        // You can use a service like https://webhook.site to test this.
        String callbackUrl = "https://webhook.site/your-unique-url";
        if (args.length > 0) {
            callbackUrl = args[0];
        }

        String audioUrl = "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav";
        if (args.length > 1) {
            audioUrl = args[1];
        }

        System.out.println("Transcribe with callback (async)");
        System.out.println("Audio:    " + audioUrl);
        System.out.println("Callback: " + callbackUrl);
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            // Submit transcription request with callback
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url(audioUrl)
                    .callback(callbackUrl)
                    .callbackMethod(MediaTranscribeRequestCallbackMethod.POST)
                    .build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

            // With a callback, the response is typically a ListenV1AcceptedResponse (202 Accepted)
            result.visit(new MediaTranscribeResponse.Visitor<Void>() {
                @Override
                public Void visit(ListenV1Response response) {
                    System.out.println("Received immediate response (unexpected with callback).");
                    return null;
                }

                @Override
                public Void visit(ListenV1AcceptedResponse accepted) {
                    System.out.println("Submitted! request_id=" + accepted.getRequestId());
                    System.out.println("Results will be sent to your callback URL.");
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("Error submitting transcription: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
