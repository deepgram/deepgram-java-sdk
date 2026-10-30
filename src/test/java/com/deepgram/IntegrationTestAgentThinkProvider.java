package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudio;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.DeepgramThinkProvider;
import com.deepgram.types.DeepgramThinkProviderModel;
import com.deepgram.types.DeepgramThinkProviderReasoningMode;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Live integration coverage for the Deepgram-hosted Think provider. */
class IntegrationTestAgentThinkProvider {
    private DeepgramClient client;

    @BeforeEach
    void setUp() {
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        org.junit.jupiter.api.Assumptions.assumeTrue(
                apiKey != null && !apiKey.isEmpty(), "DEEPGRAM_API_KEY not set, skipping integration test");
        String enabled = System.getenv("DEEPGRAM_AGENT_THINK_PROVIDER");
        org.junit.jupiter.api.Assumptions.assumeTrue(
                enabled != null && !enabled.isEmpty(), "DEEPGRAM_AGENT_THINK_PROVIDER not set, skipping integration test");
        client = DeepgramClient.builder().apiKey(apiKey).build();
    }

    @Test
    @DisplayName("Deepgram Think accepts Think Fast and PhoneLLM settings")
    void appliesSettingsForBothDeepgramThinkModels() throws Exception {
        assertSettingsApplied(
                DeepgramThinkProviderModel.THINK_FAST, DeepgramThinkProviderReasoningMode.LOW, true);
        assertSettingsApplied(
                DeepgramThinkProviderModel.PHONELLM_MINI_ALPHA_2_FP8,
                DeepgramThinkProviderReasoningMode.NONE,
                false);
    }

    private void assertSettingsApplied(
            DeepgramThinkProviderModel model,
            DeepgramThinkProviderReasoningMode reasoningMode,
            boolean voicePromptOptOut)
            throws Exception {
        V1WebSocketClient webSocket = client.agent().v1().v1WebSocket();
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        webSocket.onWelcome(ignored -> webSocket
                .sendSettings(settings(model, reasoningMode, voicePromptOptOut))
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        failure.compareAndSet(null, throwable);
                        completed.countDown();
                    }
                }));
        webSocket.onSettingsApplied(ignored -> completed.countDown());
        webSocket.onErrorMessage(error -> {
            failure.compareAndSet(null, new IllegalStateException(error.toString()));
            completed.countDown();
        });
        webSocket.onError(error -> {
            failure.compareAndSet(null, error);
            completed.countDown();
        });
        webSocket.onDisconnected(reason -> {
            failure.compareAndSet(
                    null, new IllegalStateException("Agent disconnected before applying Think settings: " + reason));
            completed.countDown();
        });

        try {
            webSocket.connect().get(10, TimeUnit.SECONDS);

            assertThat(completed.await(20, TimeUnit.SECONDS))
                    .withFailMessage("Agent did not apply settings for model %s", model)
                    .isTrue();
            assertThat(failure.get()).isNull();
        } finally {
            webSocket.disconnect();
        }
    }

    private AgentV1Settings settings(
            DeepgramThinkProviderModel model,
            DeepgramThinkProviderReasoningMode reasoningMode,
            boolean voicePromptOptOut) {
        DeepgramThinkProvider provider = DeepgramThinkProvider.builder()
                .model(model)
                .version("v1")
                .temperature(0.2)
                .reasoningMode(reasoningMode)
                .voicePromptOptOut(voicePromptOptOut)
                .build();
        return AgentV1Settings.builder()
                .audio(AgentV1SettingsAudio.builder().build())
                .agent(AgentV1SettingsAgent.of(AgentV1SettingsAgentContext.builder()
                        .think(AgentV1SettingsAgentContextThink.of(ThinkSettingsV1.builder()
                                .provider(ThinkSettingsV1Provider.deepgram(provider))
                                .prompt("Keep responses brief.")
                                .build()))
                        .build()))
                .build();
    }
}
