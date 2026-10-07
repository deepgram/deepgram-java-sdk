package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.core.ObjectMappers;
import com.deepgram.resources.listen.v1.types.ListenV1Configure;
import com.deepgram.resources.listen.v1.types.ListenV1Error;
import com.deepgram.resources.listen.v1.websocket.V1ConnectOptions;
import com.deepgram.resources.listen.v1.websocket.V1WebSocketClient;
import com.deepgram.types.ListenV1Model;
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

class ListenV1ConfigureErrorWebSocketTest {
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
    void sendsConfigureAndDispatchesConfigureError() throws Exception {
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                received.add(text);
                webSocket.send("{\"type\":\"Error\",\"variant\":\"InvalidConfigureMessage\","
                        + "\"description\":\"keyterms unavailable\",\"code\":\"KeytermsNotSupported\"}");
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }
        }));

        CountDownLatch errorReceived = new CountDownLatch(1);
        AtomicInteger genericErrorCount = new AtomicInteger();
        AtomicReference<ListenV1Error> error = new AtomicReference<>();
        V1WebSocketClient ws = client.listen().v1().v1WebSocket();
        ws.onErrorMessage(event -> {
            error.set(event);
            errorReceived.countDown();
        });
        ws.onError(event -> genericErrorCount.incrementAndGet());
        try {
            ws.connect(V1ConnectOptions.builder().model(ListenV1Model.NOVA3).build())
                    .get(5, TimeUnit.SECONDS);
            ws.sendConfigure(ListenV1Configure.builder()
                            .keyterms(List.of("Deepgram", "Flux"))
                            .features(Map.of("numerals", true))
                            .build())
                    .get(5, TimeUnit.SECONDS);

            String frame = received.poll(5, TimeUnit.SECONDS);
            assertThat(frame).isNotNull();
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("type").asText()).isEqualTo("Configure");
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("keyterms").get(0).asText())
                    .isEqualTo("Deepgram");
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("keyterms").get(1).asText())
                    .isEqualTo("Flux");
            assertThat(ObjectMappers.JSON_MAPPER.readTree(frame).path("features").path("numerals").asBoolean())
                    .isTrue();

            assertThat(errorReceived.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(error.get().getVariant()).isEqualTo("InvalidConfigureMessage");
            assertThat(error.get().getCode()).contains("KeytermsNotSupported");
            assertThat(genericErrorCount).hasValue(0);
        } finally {
            ws.disconnect();
        }
    }
}
