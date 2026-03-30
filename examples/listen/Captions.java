import com.deepgram.DeepgramClient;
import com.deepgram.resources.listen.v1.media.requests.ListenV1RequestUrl;
import com.deepgram.resources.listen.v1.media.types.MediaTranscribeResponse;
import com.deepgram.types.ListenV1AcceptedResponse;
import com.deepgram.types.ListenV1Response;
import com.deepgram.types.ListenV1ResponseResults;
import com.deepgram.types.ListenV1ResponseResultsChannelsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItemParagraphsParagraphsItem;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItemParagraphsParagraphsItemSentencesItem;
import com.deepgram.types.ListenV1ResponseResultsUtterancesItem;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates transcription with paragraphs and utterances enabled, producing caption-friendly output with speaker
 * labels and timing.
 *
 * <p>Usage: java Captions
 */
public class Captions {
    private static final String SAMPLE_AUDIO_URL =
            "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav";

    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Transcription with Captions (Paragraphs & Utterances)");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        try {
            // Build request with paragraphs and utterances enabled
            ListenV1RequestUrl request = ListenV1RequestUrl.builder()
                    .url(SAMPLE_AUDIO_URL)
                    .smartFormat(true)
                    .paragraphs(true)
                    .utterances(true)
                    .diarize(true)
                    .build();

            MediaTranscribeResponse result = client.listen().v1().media().transcribeUrl(request);

            // Process the response
            result.visit(new MediaTranscribeResponse.Visitor<Void>() {
                @Override
                public Void visit(ListenV1Response response) {
                    ListenV1ResponseResults results = response.getResults();
                    if (results == null) {
                        System.out.println("No results returned");
                        return null;
                    }

                    // Display paragraphs
                    if (!results.getChannels().isEmpty()) {
                        ListenV1ResponseResultsChannelsItem channel =
                                results.getChannels().get(0);
                        List<ListenV1ResponseResultsChannelsItemAlternativesItem> alternatives =
                                channel.getAlternatives().orElse(Collections.emptyList());

                        if (!alternatives.isEmpty()) {
                            ListenV1ResponseResultsChannelsItemAlternativesItem alt = alternatives.get(0);

                            // Show paragraphs
                            alt.getParagraphs().ifPresent(paragraphs -> {
                                System.out.println("=== Paragraphs ===");
                                System.out.println("-".repeat(60));
                                List<ListenV1ResponseResultsChannelsItemAlternativesItemParagraphsParagraphsItem>
                                        paras = paragraphs.getParagraphs().orElse(Collections.emptyList());
                                for (int i = 0; i < paras.size(); i++) {
                                    ListenV1ResponseResultsChannelsItemAlternativesItemParagraphsParagraphsItem para =
                                            paras.get(i);
                                    float speaker = para.getSpeaker().orElse(0f);
                                    System.out.printf("Speaker %.0f:%n", speaker);

                                    List<
                                                    ListenV1ResponseResultsChannelsItemAlternativesItemParagraphsParagraphsItemSentencesItem>
                                            sentences = para.getSentences().orElse(Collections.emptyList());
                                    for (ListenV1ResponseResultsChannelsItemAlternativesItemParagraphsParagraphsItemSentencesItem
                                            sentence : sentences) {
                                        sentence.getText().ifPresent(text -> System.out.println("  " + text));
                                    }
                                    System.out.println();
                                }
                            });
                        }
                    }

                    // Display utterances
                    List<ListenV1ResponseResultsUtterancesItem> utterances =
                            results.getUtterances().orElse(Collections.emptyList());
                    if (!utterances.isEmpty()) {
                        System.out.println("=== Utterances ===");
                        System.out.println("-".repeat(60));
                        for (ListenV1ResponseResultsUtterancesItem utterance : utterances) {
                            float speaker = utterance.getSpeaker().orElse(0f);
                            float start = utterance.getStart().orElse(0f);
                            float end = utterance.getEnd().orElse(0f);
                            String transcript = utterance.getTranscript().orElse("");
                            System.out.printf("[%.2f - %.2f] Speaker %.0f: %s%n", start, end, speaker, transcript);
                        }
                    }

                    return null;
                }

                @Override
                public Void visit(ListenV1AcceptedResponse accepted) {
                    System.out.println("Request accepted: " + accepted.getRequestId());
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
