package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.ObjectMappers;
import com.deepgram.resources.agent.v1.types.AgentV1UpdateListenListen;
import com.deepgram.resources.agent.v1.types.AgentV1UpdateListenListenProvider;
import com.deepgram.types.DeepgramListenProviderV1;
import com.deepgram.types.DeepgramListenProviderV2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the hand-written back-compat shim on {@code AgentV1UpdateListenListen} (see .fernignore).
 *
 * <p>fern-java-sdk 4.16.0 widened {@code provider} from {@link DeepgramListenProviderV2} to an
 * {@link AgentV1UpdateListenListenProvider} (V1|V2) union so UpdateListen can change model/language
 * (docs #1066). That return-type change is source-breaking for 0.7.x. The shim keeps the old typed
 * API ({@code getProvider()}/{@code provider(V2)}) while exposing the union additively
 * ({@code getProviderValue()}/{@code provider(V1)}). These tests lock in both the API surface and
 * the outbound JSON shape (UpdateListen is a client-sent message, so serialization is what matters).
 */
public class AgentV1UpdateListenShimTest {

    @Test
    @DisplayName("back-compat: provider(V2) builds and getProvider() still returns the V2")
    void backCompatV2Api() {
        DeepgramListenProviderV2 v2 =
                DeepgramListenProviderV2.builder().model("nova-3").build();
        AgentV1UpdateListenListen msg =
                AgentV1UpdateListenListen.builder().provider(v2).build();

        // 0.7.x typed API preserved
        assertThat(msg.getProvider()).isEqualTo(v2);
        // union exposed additively
        assertThat(msg.getProviderValue().isV2()).isTrue();
        assertThat(msg.getProviderValue().getV2()).contains(v2);
    }

    @Test
    @DisplayName("new: provider(V1) carries the union; getProvider() is null (no V2 present)")
    void newV1Api() {
        DeepgramListenProviderV1 v1 = DeepgramListenProviderV1.builder()
                .model("nova-2")
                .language("en")
                .build();
        AgentV1UpdateListenListen msg =
                AgentV1UpdateListenListen.builder().provider(v1).build();

        assertThat(msg.getProvider()).isNull();
        assertThat(msg.getProviderValue().isV1()).isTrue();
        assertThat(msg.getProviderValue().getV1()).contains(v1);
    }

    @Test
    @DisplayName("union setter: provider(union) is accepted directly")
    void unionSetter() {
        DeepgramListenProviderV2 v2 =
                DeepgramListenProviderV2.builder().model("nova-3").build();
        AgentV1UpdateListenListenProvider union = AgentV1UpdateListenListenProvider.v2(v2);
        AgentV1UpdateListenListen msg =
                AgentV1UpdateListenListen.builder().provider(union).build();

        assertThat(msg.getProviderValue()).isEqualTo(union);
        assertThat(msg.getProvider()).isEqualTo(v2);
    }

    @Test
    @DisplayName("outbound JSON (V2): provider serializes as the bare V2 object, exactly once")
    void serializesV2() throws Exception {
        DeepgramListenProviderV2 v2 =
                DeepgramListenProviderV2.builder().model("nova-3").build();
        String json = ObjectMappers.JSON_MAPPER.writeValueAsString(
                AgentV1UpdateListenListen.builder().provider(v2).build());

        // exactly one "provider" key (getProvider() must be @JsonIgnore so it doesn't double-emit)
        assertThat(json.split("\"provider\"", -1).length - 1).isEqualTo(1);
        // provider is the V2 object itself (type=deepgram, model), not wrapped in a variant key
        assertThat(json).contains("\"provider\":{").contains("\"model\":\"nova-3\"").contains("\"type\":\"deepgram\"");
        // not wrapped in a union-variant key (e.g. {"provider":{"v2":{...}}}); the "v2" that
        // appears legitimately is the version value ("version":"v2"), never a key ("v2":)
        assertThat(json).doesNotContain("\"v2\":").doesNotContain("\"v1\":");
    }

    @Test
    @DisplayName("outbound JSON (V1): provider serializes as the bare V1 object with model/language")
    void serializesV1() throws Exception {
        DeepgramListenProviderV1 v1 = DeepgramListenProviderV1.builder()
                .model("nova-2")
                .language("en")
                .build();
        String json = ObjectMappers.JSON_MAPPER.writeValueAsString(
                AgentV1UpdateListenListen.builder().provider(v1).build());

        assertThat(json.split("\"provider\"", -1).length - 1).isEqualTo(1);
        assertThat(json).contains("\"model\":\"nova-2\"").contains("\"language\":\"en\"");
        assertThat(json).doesNotContain("\"v2\":").doesNotContain("\"v1\":");
    }

    @Test
    @DisplayName("round-trip: serialize -> deserialize preserves V2 (union discriminates on version)")
    void roundTripV2() throws Exception {
        DeepgramListenProviderV2 v2 =
                DeepgramListenProviderV2.builder().model("nova-3").build();
        AgentV1UpdateListenListen msg =
                AgentV1UpdateListenListen.builder().provider(v2).build();

        String json = ObjectMappers.JSON_MAPPER.writeValueAsString(msg);
        AgentV1UpdateListenListen back = ObjectMappers.JSON_MAPPER.readValue(json, AgentV1UpdateListenListen.class);

        assertThat(back.getProviderValue().isV2()).isTrue();
        assertThat(back.getProvider()).isEqualTo(v2);
        assertThat(back).isEqualTo(msg);
    }

    @Test
    @DisplayName("round-trip: serialize -> deserialize preserves V1")
    void roundTripV1() throws Exception {
        DeepgramListenProviderV1 v1 = DeepgramListenProviderV1.builder()
                .model("nova-2")
                .language("en")
                .build();
        AgentV1UpdateListenListen msg =
                AgentV1UpdateListenListen.builder().provider(v1).build();

        String json = ObjectMappers.JSON_MAPPER.writeValueAsString(msg);
        AgentV1UpdateListenListen back = ObjectMappers.JSON_MAPPER.readValue(json, AgentV1UpdateListenListen.class);

        assertThat(back.getProviderValue().isV1()).isTrue();
        assertThat(back).isEqualTo(msg);
    }

    @Test
    @DisplayName("0.7.x compat: a provider with no \"version\" key parses as V2, not an unknown variant")
    void legacyVersionlessProviderParsesAsV2() throws Exception {
        // Exactly what 0.7.x emitted and parsed as a V2: "version" is optional on the provider, so
        // a version-less object is a valid payload. Fern defaulted a missing discriminator to
        // _UnknownValue, whose value was never populated -- getProvider() came back null and
        // re-serializing emitted {"provider":null}, silently dropping the caller's provider.
        // Patched via defaultImpl = V2Value (see AgentV1UpdateListenListenProvider).
        String legacy = "{\"provider\":{\"type\":\"deepgram\",\"model\":\"nova-3\"}}";

        AgentV1UpdateListenListen back = ObjectMappers.JSON_MAPPER.readValue(legacy, AgentV1UpdateListenListen.class);

        assertThat(back.getProviderValue().isV2()).isTrue();
        assertThat(back.getProviderValue()._isUnknown()).isFalse();
        assertThat(back.getProvider()).isNotNull();
        assertThat(back.getProvider().getModel()).isEqualTo("nova-3");

        // and it must survive re-serialization rather than becoming {"provider":null}
        String again = ObjectMappers.JSON_MAPPER.writeValueAsString(back);
        assertThat(again).doesNotContain("\"provider\":null");
        assertThat(again).contains("\"model\":\"nova-3\"");
    }

    @Test
    @DisplayName("forward-compat: an unrecognized \"version\" coerces to V2 rather than losing the provider")
    void unknownVersionCoercesToV2() throws Exception {
        // Jackson applies defaultImpl for an absent OR unrecognized discriminator, so a future
        // "v3" lands on V2 and re-serializes as "v2". Deliberate: this message is client-sent, and
        // preserving the provider beats dropping it (the old behaviour returned null and threw
        // from _getUnknown()). Locking this in so the trade-off is not silently reverted.
        String future = "{\"provider\":{\"version\":\"v3\",\"type\":\"deepgram\",\"model\":\"nova-9\"}}";

        AgentV1UpdateListenListen back = ObjectMappers.JSON_MAPPER.readValue(future, AgentV1UpdateListenListen.class);

        assertThat(back.getProvider()).isNotNull();
        assertThat(back.getProvider().getModel()).isEqualTo("nova-9");
        assertThat(ObjectMappers.JSON_MAPPER.writeValueAsString(back)).doesNotContain("\"provider\":null");
    }
}
