package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.ObjectMappers;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextListen;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentListen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the union default-variant patch on the two agent Settings listen-provider unions (see
 * .fernignore): {@code AgentV1SettingsAgentListenProvider} and
 * {@code AgentV1SettingsAgentContextListenProvider}.
 *
 * <p>{@code version} is optional on both {@code DeepgramListenProviderV1} and
 * {@code DeepgramListenProviderV2}, so a provider object without it is a valid payload. Fern points
 * the union's {@code @JsonTypeInfo} {@code defaultImpl} at {@code _UnknownValue}, whose
 * {@code @JsonCreator} has an empty body, so such a payload deserialized to an unknown variant
 * carrying {@code null}: {@code getV2()} came back empty and re-serializing emitted {@code null},
 * silently dropping the caller's provider. Both are patched to {@code defaultImpl = V2Value}, which
 * matches the server's default and keeps them consistent with the UpdateListen union
 * ({@link AgentV1UpdateListenShimTest}).
 *
 * <p>These two shipped in 0.7.1 with the defect, so this is a pre-existing bug fix rather than a
 * regression guard — but it is the same trade-off, so it is pinned the same way.
 */
public class AgentSettingsProviderDefaultTest {

    private static final String VERSIONLESS = "{\"provider\":{\"type\":\"deepgram\",\"model\":\"nova-3\"}}";

    @Test
    @DisplayName("settings agent.listen: a provider with no \"version\" key parses as V2, not an unknown variant")
    void agentListenVersionlessProviderIsV2() throws Exception {
        AgentV1SettingsAgentListen listen =
                ObjectMappers.JSON_MAPPER.readValue(VERSIONLESS, AgentV1SettingsAgentListen.class);

        assertThat(listen.getProvider()).isPresent();
        assertThat(listen.getProvider().get().isV2()).isTrue();
        assertThat(listen.getProvider().get()._isUnknown()).isFalse();
        assertThat(listen.getProvider().get().getV2()).isPresent();
        assertThat(listen.getProvider().get().getV2().get().getModel()).isEqualTo("nova-3");

        // and it must survive re-serialization rather than becoming {"provider":null}
        String again = ObjectMappers.JSON_MAPPER.writeValueAsString(listen);
        assertThat(again).doesNotContain("\"provider\":null");
        assertThat(again).contains("\"model\":\"nova-3\"");
    }

    @Test
    @DisplayName("settings agent.context.listen: a provider with no \"version\" key parses as V2")
    void agentContextListenVersionlessProviderIsV2() throws Exception {
        AgentV1SettingsAgentContextListen listen =
                ObjectMappers.JSON_MAPPER.readValue(VERSIONLESS, AgentV1SettingsAgentContextListen.class);

        assertThat(listen.getProvider()).isPresent();
        assertThat(listen.getProvider().get().isV2()).isTrue();
        assertThat(listen.getProvider().get()._isUnknown()).isFalse();
        assertThat(listen.getProvider().get().getV2().get().getModel()).isEqualTo("nova-3");

        String again = ObjectMappers.JSON_MAPPER.writeValueAsString(listen);
        assertThat(again).doesNotContain("\"provider\":null");
        assertThat(again).contains("\"model\":\"nova-3\"");
    }

    @Test
    @DisplayName("an explicit \"version\":\"v1\" still selects the V1 variant on both unions")
    void explicitV1StillWins() throws Exception {
        String v1 = "{\"provider\":{\"version\":\"v1\",\"type\":\"deepgram\","
                + "\"model\":\"nova-2\",\"language\":\"en\"}}";

        AgentV1SettingsAgentListen listen = ObjectMappers.JSON_MAPPER.readValue(v1, AgentV1SettingsAgentListen.class);
        assertThat(listen.getProvider().get().isV1()).isTrue();
        // note: model is Optional on V1 (required on V2), hence contains() rather than isEqualTo()
        assertThat(listen.getProvider().get().getV1().get().getModel()).contains("nova-2");

        AgentV1SettingsAgentContextListen context =
                ObjectMappers.JSON_MAPPER.readValue(v1, AgentV1SettingsAgentContextListen.class);
        assertThat(context.getProvider().get().isV1()).isTrue();
    }
}
