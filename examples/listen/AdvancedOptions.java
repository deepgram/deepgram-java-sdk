import java.util.Collections;
import java.util.List;
import resources.listen.v1.media.requests.ListenV1RequestUrl;
import resources.listen.v1.media.types.MediaTranscribeRequestModel;
import resources.listen.v1.media.types.MediaTranscribeResponse;
import types.ListenV1Response;
import types.ListenV1ResponseResults;
import types.ListenV1ResponseResultsChannelsItem;
import types.ListenV1ResponseResultsChannelsItemAlternativesItem;
import types.ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem;

/**
 * Transcribe audio with advanced options: smart formatting, punctuation,
 * diarization, model selection, and language specification.
 *
 * <p>Usage: java AdvancedOptions
 */
public class AdvancedOptions {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Create client
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        System.out.println("Transcribing with advanced options...");
        System.out.println();

        try {
            // Build request with advanced options
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url("https://dpgr.am/spacewalk.wav")
                    .model(MediaTranscribeRequestModel.NOVA3)
                    .smartFormat(true)
                    .punctuate(true)
                    .diarize(true)
                    .language("en-US")
                    .build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

            // Display results using visitor
            result.visit(new MediaTranscribeResponse.Visitor<Void>() {
                @Override
                public Void visit(ListenV1Response response) {
                    ListenV1ResponseResults results = response.getResults();
                    if (results == null || results.getChannels().isEmpty()) {
                        System.out.println("No transcription results found");
                        return null;
                    }

                    ListenV1ResponseResultsChannelsItem channel = results.getChannels().get(0);
                    List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                            channel.getAlternatives().orElse(Collections.emptyList());
                    if (alternatives.isEmpty()) {
                        System.out.println("No alternatives found");
                        return null;
                    }

                    ListenV1ResponseResultsChannelsItemAlternativesItem alt =
                            alternatives.get(0);

                    // Display transcript
                    alt.getTranscript().ifPresent(transcript -> {
                        System.out.println("Transcription:");
                        System.out.println("-".repeat(50));
                        System.out.println(transcript);
                        System.out.println("-".repeat(50));
                    });

                    // Display confidence
                    alt.getConfidence().ifPresent(confidence ->
                            System.out.printf("%nConfidence: %.2f%%%n", confidence * 100));

                    // Display word-level details
                    List<ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem> words =
                            alt.getWords().orElse(Collections.emptyList());
                    if (!words.isEmpty()) {
                        System.out.println("\nWord Details (first 10):");
                        System.out.println("-".repeat(50));

                        int limit = Math.min(words.size(), 10);
                        for (int i = 0; i < limit; i++) {
                            ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem word =
                                    words.get(i);
                            StringBuilder sb = new StringBuilder("  ");
                            word.getWord().ifPresent(w ->
                                    sb.append(String.format("%-15s", w)));
                            if (word.getStart().isPresent() && word.getEnd().isPresent()) {
                                sb.append(String.format(" [%.2fs - %.2fs]",
                                        word.getStart().get(), word.getEnd().get()));
                            }
                            word.getConfidence().ifPresent(c ->
                                    sb.append(String.format(" (%.0f%%)", c * 100)));
                            System.out.println(sb);
                        }
                        if (words.size() > 10) {
                            System.out.printf("  ... and %d more words%n", words.size() - 10);
                        }
                    }
                    return null;
                }

                @Override
                public Void visit(types.ListenV1AcceptedResponse accepted) {
                    System.out.println("Request accepted: " + accepted.getRequestId());
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("Error transcribing audio: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
