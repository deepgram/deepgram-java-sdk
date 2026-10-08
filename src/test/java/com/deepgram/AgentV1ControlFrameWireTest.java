package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.core.ObjectMappers;
import com.deepgram.resources.agent.v1.types.AgentV1CustomFromThinkProvider;
import com.deepgram.resources.agent.v1.types.AgentV1CustomToThinkProvider;
import com.deepgram.resources.agent.v1.types.AgentV1FunctionCallCancelled;
import com.deepgram.resources.agent.v1.types.AgentV1ForceEndTurn;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentV1ControlFrameWireTest {
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
    void sendForceEndTurnSerializesNoPayloadControlFrame() throws Exception {
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                received.add(text);
                webSocket.close(1000, null);
            }
        }));

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        try {
            ws.connect().get(5, TimeUnit.SECONDS);
            ws.sendForceEndTurn(AgentV1ForceEndTurn.builder().build());

            assertThat(received.poll(5, TimeUnit.SECONDS)).isEqualTo("{\"type\":\"ForceEndTurn\"}");
        } finally {
            ws.disconnect();
        }
    }

    @Test
    void dispatchesFunctionCallCancelled() throws Exception {
        CountDownLatch received = new CountDownLatch(1);
        AtomicReference<AgentV1FunctionCallCancelled> cancelled = new AtomicReference<>();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.send("{\"type\":\"FunctionCallCancelled\",\"functions\":[{\"id\":\"call-1\",\"name\":\"charge_card\"}]}");
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        ws.onFunctionCallCancelled(event -> {
            cancelled.set(event);
            received.countDown();
        });
        try {
            ws.connect().get(5, TimeUnit.SECONDS);

            assertThat(received.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(cancelled.get().getFunctions()).singleElement().satisfies(function -> {
                assertThat(function.getId()).isEqualTo("call-1");
                assertThat(function.getName()).isEqualTo("charge_card");
            });
        } finally {
            ws.disconnect();
        }
    }

    @Test
    void sendsCustomPayloadToThinkProvider() throws Exception {
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

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        try {
            ws.connect().get(5, TimeUnit.SECONDS);
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("action", "lookup");
            content.put("arguments", Map.of("city", "Seattle", "units", "metric"));
            content.put("candidates", List.of("weather", 7, true));
            content.put("attempt", 2);
            content.put("enabled", false);
            content.put("empty", null);
            ws.sendCustomToThinkProvider(AgentV1CustomToThinkProvider.builder()
                            .content(content)
                            .build())
                    .get(5, TimeUnit.SECONDS);

            String frame = received.poll(5, TimeUnit.SECONDS);
            assertThat(frame).isNotNull();
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("type").asText())
                    .isEqualTo("__customToThinkProvider");
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("content").path("action").asText())
                    .isEqualTo("lookup");
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("content").path("arguments").path("city").asText())
                    .isEqualTo("Seattle");
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("content").path("candidates").get(1).asInt())
                    .isEqualTo(7);
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("content").path("candidates").get(2).asBoolean())
                    .isTrue();
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("content").path("attempt").asInt())
                    .isEqualTo(2);
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("content").path("enabled").asBoolean())
                    .isFalse();
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("content").path("empty").isNull())
                    .isTrue();
        } finally {
            ws.disconnect();
        }
    }

    @Test
    void dispatchesCustomPayloadFromThinkProvider() throws Exception {
        CountDownLatch received = new CountDownLatch(1);
        AtomicReference<AgentV1CustomFromThinkProvider> custom = new AtomicReference<>();
        AtomicInteger genericErrorCount = new AtomicInteger();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.send("{\"type\":\"__customFromThinkProvider\",\"content\":{"
                        + "\"decision\":\"continue\",\"arguments\":{\"city\":\"Seattle\"},"
                        + "\"candidates\":[\"weather\",7,true],\"attempt\":2,\"enabled\":false,\"empty\":null}}");
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        V1WebSocketClient ws = client.agent().v1().v1WebSocket();
        ws.onCustomFromThinkProvider(event -> {
            custom.set(event);
            received.countDown();
        });
        ws.onError(event -> genericErrorCount.incrementAndGet());
        try {
            ws.connect().get(5, TimeUnit.SECONDS);

            assertThat(received.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(custom.get().getContent()).isInstanceOf(Map.class);
            Map<?, ?> content = (Map<?, ?>) custom.get().getContent();
            assertThat(content.get("decision")).isEqualTo("continue");
            Map<?, ?> arguments = (Map<?, ?>) content.get("arguments");
            assertThat(arguments.get("city")).isEqualTo("Seattle");
            List<?> candidates = (List<?>) content.get("candidates");
            assertThat(candidates).hasSize(3);
            assertThat(candidates.get(0)).isEqualTo("weather");
            assertThat(candidates.get(1)).isEqualTo(7);
            assertThat(candidates.get(2)).isEqualTo(true);
            assertThat(content.get("attempt")).isEqualTo(2);
            assertThat(content.get("enabled")).isEqualTo(false);
            assertThat(content.get("empty")).isNull();
            assertThat(genericErrorCount).hasValue(0);
        } finally {
            ws.disconnect();
        }
    }
}
