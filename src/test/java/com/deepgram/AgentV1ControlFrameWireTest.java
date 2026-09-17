package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.resources.agent.v1.types.AgentV1FunctionCallCancelled;
import com.deepgram.resources.agent.v1.types.AgentV1ForceEndTurn;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
}
