package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.core.WebSocketReadyState;
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.listen.v2.websocket.V2WebSocketClient;
import com.deepgram.types.ListenV2Model;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regression coverage for a server-initiated streaming close. */
class ListenV2ServerCloseTest {
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
    @DisplayName("a server close notifies the client without an explicit disconnect")
    void serverInitiatedCloseNotifiesClient() throws Exception {
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.close(1000, "stream complete");
            }
        }));
        V2WebSocketClient ws = client.listen().v2().v2WebSocket();
        CountDownLatch disconnected = new CountDownLatch(1);
        AtomicInteger disconnectCount = new AtomicInteger();
        ws.onDisconnected(reason -> {
            disconnectCount.incrementAndGet();
            disconnected.countDown();
        });

        try {
            ws.connect(V2ConnectOptions.builder()
                            .model(ListenV2Model.FLUX_GENERAL_EN)
                            .build())
                    .get(5, TimeUnit.SECONDS);

            assertThat(disconnected.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(disconnectCount).hasValue(1);
            assertThat(ws.getReadyState()).isEqualTo(WebSocketReadyState.CLOSED);
        } finally {
            ws.disconnect();
        }
    }
}
