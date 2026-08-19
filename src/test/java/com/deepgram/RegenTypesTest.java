package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.ObjectMappers;
import com.deepgram.resources.agent.v1.types.AgentV1AgentAudioDone;
import com.deepgram.resources.agent.v1.types.AgentV1KeepAlive;
import com.deepgram.resources.agent.v1.types.AgentV1ListenUpdated;
import com.deepgram.resources.agent.v1.types.AgentV1PromptUpdated;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsApplied;
import com.deepgram.resources.agent.v1.types.AgentV1SpeakUpdated;
import com.deepgram.resources.agent.v1.types.AgentV1ThinkUpdated;
import com.deepgram.resources.agent.v1.types.AgentV1UpdateListenListenProvider;
import com.deepgram.resources.agent.v1.types.AgentV1UserStartedSpeaking;
import com.deepgram.resources.listen.v1.types.ListenV1ResultsMetadata;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;
import com.deepgram.resources.listen.v2.types.ListenV2ForceEndTurn;
import com.deepgram.resources.listen.v2.types.ListenV2TurnInfo;
import com.deepgram.resources.listen.v2.types.ListenV2TurnInfoEvent;
import com.deepgram.resources.listen.v2.types.ListenV2TurnInfoWordsItem;
import com.deepgram.resources.speak.v2.types.SpeakV2Close;
import com.deepgram.resources.speak.v2.types.SpeakV2Flush;
import com.deepgram.types.DeepgramListenProviderV2;
import com.deepgram.types.DeepgramModel;
import com.deepgram.types.Google;
import com.deepgram.types.GoogleModel;
import com.deepgram.types.GoogleVersion;
import com.deepgram.types.ListenV1ResponseMetadata;
import com.deepgram.types.ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem;
import com.deepgram.types.ListenV2Redact;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Coverage for type-shape changes introduced by the 2026-06-15 regeneration:
 *
 * <ul>
 *   <li>{@link ListenV2TurnInfoWordsItem} {@code start}/{@code end} became {@code Optional<Double>} (were required
 *       {@code double}) — the client must tolerate words that omit timestamps.
 *   <li>{@link ListenV2CloseStream} {@code type} is now a fixed {@code "CloseStream"} constant, and its manually
 *       patched {@code equals}/{@code hashCode} pair must honour the {@link Object} contract.
 *   <li>{@link DeepgramListenProviderV2} {@code language_hint} was renamed to {@code language_hints} (a list).
 *   <li>Fields-less message types generate {@code equals()} but no {@code hashCode()}; we patch a consistent
 *       {@code hashCode()} onto each (frozen in {@code .fernignore}). This test guards those patches against a
 *       future regen silently dropping them again — see {@code FieldsLessMessageContract}.
 * </ul>
 */
public class RegenTypesTest {

    private static final ObjectMapper MAPPER = ObjectMappers.JSON_MAPPER;

    @Nested
    @DisplayName("ListenV2TurnInfoWordsItem optional start/end")
    class TurnInfoWordsItem {

        @Test
        @DisplayName("round-trips when start and end are present")
        void roundTripsWithTimestamps() throws Exception {
            ListenV2TurnInfoWordsItem word = ListenV2TurnInfoWordsItem.builder()
                    .word("hello")
                    .confidence(0.97f)
                    .start(1.5)
                    .end(2.25)
                    .build();

            String json = MAPPER.writeValueAsString(word);
            ListenV2TurnInfoWordsItem parsed = MAPPER.readValue(json, ListenV2TurnInfoWordsItem.class);

            assertThat(parsed.getStart()).contains(1.5);
            assertThat(parsed.getEnd()).contains(2.25);
            assertThat(parsed).isEqualTo(word);
        }

        @Test
        @DisplayName("deserializes with start and end absent (no longer required)")
        void toleratesMissingTimestamps() throws Exception {
            String json = "{\"word\":\"hello\",\"confidence\":0.97}";

            ListenV2TurnInfoWordsItem parsed = MAPPER.readValue(json, ListenV2TurnInfoWordsItem.class);

            assertThat(parsed.getWord()).isEqualTo("hello");
            assertThat(parsed.getStart()).isEmpty();
            assertThat(parsed.getEnd()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ListenV2CloseStream")
    class CloseStream {

        @Test
        @DisplayName("type is the fixed CloseStream constant")
        void typeIsFixedConstant() throws Exception {
            ListenV2CloseStream message = ListenV2CloseStream.builder().build();

            assertThat(message.getType()).isEqualTo("CloseStream");
            assertThat(MAPPER.writeValueAsString(message)).contains("\"type\":\"CloseStream\"");
        }

        @Test
        @DisplayName("equals and hashCode honour the Object contract")
        void equalsHashCodeContract() {
            ListenV2CloseStream a = ListenV2CloseStream.builder().build();
            ListenV2CloseStream b = ListenV2CloseStream.builder().build();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    @Nested
    @DisplayName("Fields-less message types: manual hashCode() patch honours the Object contract")
    class FieldsLessMessageContract {

        @Test
        @DisplayName("equal instances share a hash code across all patched fields-less types")
        void equalsHashCodeContract() {
            // Each type generates equals() (all instances equal) but no hashCode(); we patch a
            // consistent hashCode(). Two freshly-built instances must be equal AND share a hash.
            assertContract(SpeakV2Close.builder().build(), SpeakV2Close.builder().build());
            assertContract(SpeakV2Flush.builder().build(), SpeakV2Flush.builder().build());
            assertContract(AgentV1ListenUpdated.builder().build(), AgentV1ListenUpdated.builder().build());
            assertContract(AgentV1SpeakUpdated.builder().build(), AgentV1SpeakUpdated.builder().build());
            assertContract(AgentV1AgentAudioDone.builder().build(), AgentV1AgentAudioDone.builder().build());
            assertContract(AgentV1SettingsApplied.builder().build(), AgentV1SettingsApplied.builder().build());
            assertContract(
                    AgentV1UserStartedSpeaking.builder().build(),
                    AgentV1UserStartedSpeaking.builder().build());
            assertContract(AgentV1KeepAlive.builder().build(), AgentV1KeepAlive.builder().build());
            assertContract(AgentV1ThinkUpdated.builder().build(), AgentV1ThinkUpdated.builder().build());
            assertContract(AgentV1PromptUpdated.builder().build(), AgentV1PromptUpdated.builder().build());
            assertContract(ListenV2ForceEndTurn.builder().build(), ListenV2ForceEndTurn.builder().build());
        }

        private void assertContract(Object a, Object b) {
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    @Nested
    @DisplayName("DeepgramListenProviderV2 languageHints")
    class ListenProviderV2 {

        @Test
        @DisplayName("language_hints round-trips as a list")
        void languageHintsRoundTrip() throws Exception {
            DeepgramListenProviderV2 provider = DeepgramListenProviderV2.builder()
                    .model("flux-general-multi")
                    .languageHints(Arrays.asList("en", "es"))
                    .build();

            String json = MAPPER.writeValueAsString(provider);
            assertThat(json).contains("\"language_hints\"");

            DeepgramListenProviderV2 parsed = MAPPER.readValue(json, DeepgramListenProviderV2.class);
            assertThat(parsed.getLanguageHints()).contains(Arrays.asList("en", "es"));
        }
    }

    @Nested
    @DisplayName("DeepgramModel.FLUX_RENEE_EN: manually restored constant")
    class FluxReneeConstant {

        @Test
        @DisplayName("resolves, carries the right wire value, and round-trips")
        void reneeConstantSurvives() throws Exception {
            // Generator 4.18.0 dropped this constant, but the voice is live on /v2/speak. The patch
            // restores all five touchpoints; this guards a future regen silently dropping it again.
            assertThat(DeepgramModel.FLUX_RENEE_EN.toString()).isEqualTo("flux-renee-en");
            assertThat(DeepgramModel.FLUX_RENEE_EN.getEnumValue()).isEqualTo(DeepgramModel.Value.FLUX_RENEE_EN);
            assertThat(DeepgramModel.valueOf("flux-renee-en")).isEqualTo(DeepgramModel.FLUX_RENEE_EN);

            String json = MAPPER.writeValueAsString(DeepgramModel.FLUX_RENEE_EN);
            assertThat(json).isEqualTo("\"flux-renee-en\"");
            assertThat(MAPPER.readValue(json, DeepgramModel.class)).isEqualTo(DeepgramModel.FLUX_RENEE_EN);
        }
    }

    /**
     * Coverage for public-surface changes introduced by the 2026-08-11 regeneration. These are
     * breaking (documented in {@code docs/Migrating-v0.7-to-v0.8.md}); the tests pin the new typed
     * API shapes so a future regen can't silently reshape them again without a failing guard.
     */
    @Nested
    @DisplayName("2026-08-11 regen type shapes")
    class Regen20260811 {

        @Test
        @DisplayName("AgentV1UpdateListenListen provider is a V1/V2 union: v2 variant round-trips")
        void updateListenProviderUnionV2() throws Exception {
            // provider was retyped from a bare DeepgramListenProviderV2 to this discriminated union.
            // Guard the v2 factory + accessors (the migration path) and that the payload survives
            // serialization (the nested provider model must appear on the wire).
            DeepgramListenProviderV2 v2 =
                    DeepgramListenProviderV2.builder().model("flux-general-en").build();
            AgentV1UpdateListenListenProvider provider = AgentV1UpdateListenListenProvider.v2(v2);

            assertThat(provider.isV2()).isTrue();
            assertThat(provider.isV1()).isFalse();
            assertThat(provider.getV2()).contains(v2);
            assertThat(MAPPER.writeValueAsString(provider)).contains("flux-general-en");
        }

        @Test
        @DisplayName("Google.version is a GoogleVersion enum serializing to its wire value")
        void googleVersionEnum() throws Exception {
            Google google = Google.builder()
                    .model(GoogleModel.GEMINI25FLASH)
                    .version(GoogleVersion.V1BETA)
                    .build();

            assertThat(google.getVersion()).contains(GoogleVersion.V1BETA);
            assertThat(MAPPER.writeValueAsString(google)).contains("\"version\":\"v1beta\"");
        }

        @Test
        @DisplayName("ListenV2Redact enum serializes to its raw wire value")
        void listenV2RedactWireValue() {
            assertThat(ListenV2Redact.NUMBERS.toString()).isEqualTo("numbers");
            assertThat(ListenV2Redact.AGGRESSIVE_NUMBERS.toString()).isEqualTo("aggressive_numbers");
        }
    }

    /**
     * Coverage for the read-side fields added by the 2026-08-19 regeneration. These are additive, so
     * nothing breaks if they are absent — which is exactly the risk: a regen could drop or rename one
     * and no existing test would notice. Each is asserted from a realistic server payload (parsing the
     * JSON rather than round-tripping a builder) so the tests exercise the direction that actually
     * matters, server to SDK, including the absent case.
     */
    @Nested
    @DisplayName("2026-08-19 regen read-side fields")
    class Regen20260819 {

        @Test
        @DisplayName("ListenV2TurnInfo.trigger is surfaced when the server sends it")
        void turnInfoTriggerPresent() throws Exception {
            // trigger identifies what ended a turn. Typed as an open String rather than an enum, so a
            // new server-side value cannot break a deployed client.
            String json = "{\"type\":\"TurnInfo\",\"request_id\":\"req-1\",\"sequence_id\":3,"
                    + "\"event\":\"EndOfTurn\",\"turn_index\":0,\"audio_window_start\":0.0,"
                    + "\"audio_window_end\":1.5,\"transcript\":\"hello\",\"end_of_turn_confidence\":0.91,"
                    + "\"trigger\":\"manual\"}";

            ListenV2TurnInfo turn = MAPPER.readValue(json, ListenV2TurnInfo.class);

            assertThat(turn.getTrigger()).contains("manual");
            assertThat(turn.getEvent()).isEqualTo(ListenV2TurnInfoEvent.END_OF_TURN);
        }

        @Test
        @DisplayName("ListenV2TurnInfo.trigger is empty when the server omits it")
        void turnInfoTriggerAbsent() throws Exception {
            // The live API does not emit trigger yet, so the absent case is the one deployed clients
            // actually hit today. It must parse cleanly rather than failing or defaulting to a value.
            String json = "{\"type\":\"TurnInfo\",\"request_id\":\"req-1\",\"sequence_id\":1,"
                    + "\"event\":\"Update\",\"turn_index\":0,\"audio_window_start\":0.0,"
                    + "\"audio_window_end\":0.5,\"transcript\":\"\",\"end_of_turn_confidence\":0.1}";

            ListenV2TurnInfo turn = MAPPER.readValue(json, ListenV2TurnInfo.class);

            assertThat(turn.getTrigger()).isEmpty();
        }

        @Test
        @DisplayName("ListenV1 results metadata exposes diarize_info (model_uuid + arch)")
        void resultsMetadataDiarizeInfo() throws Exception {
            String json = "{\"request_id\":\"req-2\",\"model_uuid\":\"m-uuid\","
                    + "\"model_info\":{\"name\":\"nova-3\",\"version\":\"1\",\"arch\":\"nova-3\"},"
                    + "\"diarize_info\":{\"model_uuid\":\"d-uuid\",\"arch\":\"v1\"}}";

            ListenV1ResultsMetadata meta = MAPPER.readValue(json, ListenV1ResultsMetadata.class);

            assertThat(meta.getDiarizeInfo()).isPresent();
            assertThat(meta.getDiarizeInfo().get().getModelUuid()).isEqualTo("d-uuid");
            assertThat(meta.getDiarizeInfo().get().getArch()).isEqualTo("v1");
        }

        @Test
        @DisplayName("ListenV1 results metadata diarize_info is empty when diarization is off")
        void resultsMetadataDiarizeInfoAbsent() throws Exception {
            String json = "{\"request_id\":\"req-2\",\"model_uuid\":\"m-uuid\","
                    + "\"model_info\":{\"name\":\"nova-3\",\"version\":\"1\",\"arch\":\"nova-3\"}}";

            ListenV1ResultsMetadata meta = MAPPER.readValue(json, ListenV1ResultsMetadata.class);

            assertThat(meta.getDiarizeInfo()).isEmpty();
        }

        @Test
        @DisplayName("ListenV1 response metadata exposes its own diarize_info variant")
        void responseMetadataDiarizeInfo() throws Exception {
            // The pre-recorded response carries a parallel DiarizeInfo type under com.deepgram.types;
            // both were added this regen, so both need a guard.
            String json = "{\"request_id\":\"req-3\",\"created\":\"2026-08-19T00:00:00Z\","
                    + "\"duration\":1.0,\"channels\":1,\"sha256\":\"abc\",\"transaction_key\":\"tk\","
                    + "\"model_info\":{},\"diarize_info\":{\"model_uuid\":\"d-uuid\",\"arch\":\"v1\"}}";

            ListenV1ResponseMetadata meta = MAPPER.readValue(json, ListenV1ResponseMetadata.class);

            assertThat(meta.getDiarizeInfo()).isPresent();
            assertThat(meta.getDiarizeInfo().get().getModelUuid()).isEqualTo("d-uuid");
            assertThat(meta.getDiarizeInfo().get().getArch()).isEqualTo("v1");
        }

        @Test
        @DisplayName("pre-recorded words item exposes speaker_confidence alongside speaker")
        void wordsItemSpeakerConfidence() throws Exception {
            String json = "{\"word\":\"hello\",\"start\":0.1,\"end\":0.4,\"confidence\":0.99,"
                    + "\"speaker\":0,\"speaker_confidence\":0.87}";

            ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem word =
                    MAPPER.readValue(json, ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem.class);

            assertThat(word.getSpeaker()).contains(0);
            assertThat(word.getSpeakerConfidence()).isPresent();
            assertThat(word.getSpeakerConfidence().get()).isEqualTo(0.87f);
        }

        @Test
        @DisplayName("words item speaker_confidence is empty when diarization is off")
        void wordsItemSpeakerConfidenceAbsent() throws Exception {
            // speaker_confidence is pre-recorded only -- it is never returned for streaming, so the
            // empty case must stay clean rather than throwing on a streaming-shaped payload.
            String json = "{\"word\":\"hello\",\"start\":0.1,\"end\":0.4,\"confidence\":0.99}";

            ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem word =
                    MAPPER.readValue(json, ListenV1ResponseResultsChannelsItemAlternativesItemWordsItem.class);

            assertThat(word.getSpeakerConfidence()).isEmpty();
            assertThat(word.getSpeaker()).isEmpty();
        }
    }
}
