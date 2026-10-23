package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.core.ObjectMappers;
import com.deepgram.resources.agent.v1.types.AgentV1ConversationText;
import com.deepgram.resources.agent.v1.types.AgentV1ConversationTextConversationalBehavior;
import com.deepgram.resources.agent.v1.types.AgentV1FrustrationDetected;
import com.deepgram.resources.agent.v1.types.AgentV1InjectDynamicAgentMessage;
import com.deepgram.resources.agent.v1.types.AgentV1InjectDynamicAgentMessageBehavior;
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudio;
import com.deepgram.resources.agent.v1.types.AgentV1ThinkLiveUpdated;
import com.deepgram.resources.agent.v1.types.AgentV1UpdateThinkLive;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.DeepgramThinkLiveProviderV1;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.ThinkLiveSettingsV1;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1FunctionsItem;
import com.deepgram.types.ThinkSettingsV1Provider;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.Map;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentV1ThinkLiveWireTest {
    private MockWebServer server;
    private DeepgramClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        String base = server.url("/").toString().replaceAll("/$", "");
        Environment env = Environment.custom()
                .base(base)
                .production(base)
                .agent(base)
                .agentRest(base)
                .build();
        client = DeepgramClient.builder().apiKey("test").environment(env).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void sendsThinkLiveUpdateAndDynamicAgentInjection() throws Exception {
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                received.add(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        ThinkLiveSettingsV1 settings = ThinkLiveSettingsV1.builder()
                .provider(DeepgramThinkLiveProviderV1.builder()
                        .agentBackchannelSensitivity(0.35)
                        .presenceCheckMaxCount(2)
                        .frustrationMaxCount(3)
                        .build())
                .build();
        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        try {
            ws.connect().get(5, TimeUnit.SECONDS);
            ws.sendUpdateThinkLive(AgentV1UpdateThinkLive.builder().thinkLive(settings).build())
                    .get(5, TimeUnit.SECONDS);
            ws.sendInjectDynamicAgentMessage(AgentV1InjectDynamicAgentMessage.builder()
                            .behavior(AgentV1InjectDynamicAgentMessageBehavior.QUEUE)
                            .build())
                    .get(5, TimeUnit.SECONDS);

            com.fasterxml.jackson.databind.JsonNode update =
                    ObjectMappers.JSON_MAPPER.readTree(received.poll(5, TimeUnit.SECONDS));
            assertThat(update.path("type").asText()).isEqualTo("UpdateThink_Live");
            assertThat(update.path("think_live").path("provider").path("type").asText())
                    .isEqualTo("deepgram");
            assertThat(update.path("think_live").path("provider").path("version").asText())
                    .isEqualTo("v1");

            com.fasterxml.jackson.databind.JsonNode injection =
                    ObjectMappers.JSON_MAPPER.readTree(received.poll(5, TimeUnit.SECONDS));
            assertThat(injection.path("type").asText()).isEqualTo("InjectAgentMessage");
            assertThat(injection.path("is_dynamic").asBoolean()).isTrue();
            assertThat(injection.path("behavior").asText()).isEqualTo("queue");
            assertThat(injection.has("message")).isFalse();
        } finally {
            ws.disconnect();
        }
    }

    @Test
    void sendsInitialThinkLiveSettingsAndFunctionFillers() throws Exception {
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.send("{\"type\":\"Welcome\",\"request_id\":\"req-1\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                received.add(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        ThinkSettingsV1FunctionsItem function = ThinkSettingsV1FunctionsItem.builder()
                .name("lookup_status")
                .description("Look up account status")
                .parameters(Map.of("type", "object", "properties", Map.of()))
                .initialAgentFiller(true)
                .agentFillerMaxCount(2)
                .agentFillerIntervalMs(1_500)
                .build();
        ThinkLiveSettingsV1 thinkLive = ThinkLiveSettingsV1.builder()
                .provider(DeepgramThinkLiveProviderV1.builder()
                        .agentBackchannelSensitivity(0.35)
                        .presenceCheckMaxCount(2)
                        .presenceCheckIntervalMs(10_000)
                        .frustrationMaxCount(3)
                        .restrictedTerms(List.of("forbidden phrase"))
                        .build())
                .build();
        AgentV1Settings settings = AgentV1Settings.builder()
                .audio(AgentV1SettingsAudio.builder().build())
                .agent(AgentV1SettingsAgent.of(AgentV1SettingsAgentContext.builder()
                        .think(AgentV1SettingsAgentContextThink.of(ThinkSettingsV1.builder()
                                .provider(ThinkSettingsV1Provider.openAi(OpenAiThinkProvider.builder()
                                        .model(OpenAiThinkProviderModel.GPT4O_MINI)
                                        .build()))
                                .prompt("You are a concise test assistant.")
                                .functions(List.of(function))
                                .build()))
                        .thinkLive(thinkLive)
                        .build()))
                .build();

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        ws.onWelcome(welcome -> ws.sendSettings(settings));
        try {
            ws.connect().get(5, TimeUnit.SECONDS);

            com.fasterxml.jackson.databind.JsonNode frame =
                    ObjectMappers.JSON_MAPPER.readTree(received.poll(5, TimeUnit.SECONDS));
            assertThat(frame.path("type").asText()).isEqualTo("Settings");
            com.fasterxml.jackson.databind.JsonNode provider = frame.path("agent").path("think_live").path("provider");
            assertThat(provider.path("type").asText()).isEqualTo("deepgram");
            assertThat(provider.path("version").asText()).isEqualTo("v1");
            assertThat(provider.path("agent_backchannel_sensitivity").asDouble()).isEqualTo(0.35);
            assertThat(provider.path("presence_check_max_count").asInt()).isEqualTo(2);
            assertThat(provider.path("presence_check_interval_ms").asInt()).isEqualTo(10_000);
            assertThat(provider.path("frustration_max_count").asInt()).isEqualTo(3);
            assertThat(provider.path("restricted_terms").get(0).asText()).isEqualTo("forbidden phrase");

            com.fasterxml.jackson.databind.JsonNode fillers = frame.path("agent").path("think").path("functions").get(0);
            assertThat(fillers.path("initial_agent_filler").asBoolean()).isTrue();
            assertThat(fillers.path("agent_filler_max_count").asInt()).isEqualTo(2);
            assertThat(fillers.path("agent_filler_interval_ms").asInt()).isEqualTo(1_500);
        } finally {
            ws.disconnect();
        }
    }

    @Test
    void dispatchesThinkLiveConversationEvents() throws Exception {
        CountDownLatch received = new CountDownLatch(3);
        AtomicReference<AgentV1ThinkLiveUpdated> updated = new AtomicReference<>();
        AtomicReference<AgentV1FrustrationDetected> frustration = new AtomicReference<>();
        AtomicReference<AgentV1ConversationText> conversationText = new AtomicReference<>();
        AtomicInteger genericErrorCount = new AtomicInteger();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.send("{\"type\":\"ThinkLiveUpdated\"}");
                webSocket.send("{\"type\":\"FrustrationDetected\",\"count\":2,\"escalated\":true}");
                webSocket.send("{\"type\":\"ConversationText\",\"role\":\"assistant\","
                        + "\"content\":\"Let me check that.\",\"conversational_behavior\":\"filler\"}");
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        ws.onThinkLiveUpdated(event -> {
            updated.set(event);
            received.countDown();
        });
        ws.onFrustrationDetected(event -> {
            frustration.set(event);
            received.countDown();
        });
        ws.onConversationText(event -> {
            conversationText.set(event);
            received.countDown();
        });
        ws.onError(event -> genericErrorCount.incrementAndGet());
        try {
            ws.connect().get(5, TimeUnit.SECONDS);

            assertThat(received.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(updated.get()).isNotNull();
            assertThat(frustration.get().getCount()).isEqualTo(2);
            assertThat(frustration.get().getEscalated()).isTrue();
            assertThat(conversationText.get().getContent()).isEqualTo("Let me check that.");
            assertThat(conversationText.get().getConversationalBehavior())
                    .contains(AgentV1ConversationTextConversationalBehavior.FILLER);
            assertThat(genericErrorCount).hasValue(0);
        } finally {
            ws.disconnect();
        }
    }

    @Test
    void ignoresUnknownFramesAfterDeliveringThemRaw() throws Exception {
        String unknownFrame = "{\"type\":\"FutureAgentControl\",\"feature\":true}";
        CountDownLatch rawFrameReceived = new CountDownLatch(1);
        AtomicReference<String> rawFrame = new AtomicReference<>();
        AtomicInteger genericErrorCount = new AtomicInteger();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.send(unknownFrame);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        ws.onMessage(message -> {
            rawFrame.set(message);
            rawFrameReceived.countDown();
        });
        ws.onError(event -> genericErrorCount.incrementAndGet());
        try {
            ws.connect().get(5, TimeUnit.SECONDS);

            assertThat(rawFrameReceived.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(rawFrame.get()).isEqualTo(unknownFrame);
            assertThat(genericErrorCount).hasValue(0);
        } finally {
            ws.disconnect();
        }
    }

    @Test
    void reportsUnknownFramesToLegacyErrorHandlersWithoutRawHandler() throws Exception {
        String unknownFrame = "{\"type\":\"FutureAgentControl\",\"feature\":true}";
        CountDownLatch errorReceived = new CountDownLatch(1);
        AtomicReference<Exception> error = new AtomicReference<>();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.send(unknownFrame);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        ws.onError(event -> {
            error.set(event);
            errorReceived.countDown();
        });
        try {
            ws.connect().get(5, TimeUnit.SECONDS);

            assertThat(errorReceived.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(error.get()).hasMessageContaining("FutureAgentControl");
        } finally {
            ws.disconnect();
        }
    }
}
