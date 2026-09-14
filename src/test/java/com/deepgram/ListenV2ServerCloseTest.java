package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.core.ReconnectingWebSocketListener;
import com.deepgram.core.WebSocketFactory;
import com.deepgram.core.WebSocketReadyState;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.listen.v2.websocket.V2WebSocketClient;
import com.deepgram.types.ListenV2Model;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Request;
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

    @Test
    @DisplayName("CloseStream makes a following no-status close terminal")
    void closeStreamNoStatusCloseDoesNotReconnect() throws Exception {
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {}));
        V2WebSocketClient ws = client.listen().v2().v2WebSocket();
        CountDownLatch disconnected = new CountDownLatch(1);
        ws.reconnectOptions(ReconnectingWebSocketListener.ReconnectOptions.builder()
                .minReconnectionDelayMs(10)
                .maxReconnectionDelayMs(10)
                .build());
        ws.onDisconnected(reason -> disconnected.countDown());

        try {
            ws.connect(V2ConnectOptions.builder()
                            .model(ListenV2Model.FLUX_GENERAL_EN)
                            .build())
                    .get(5, TimeUnit.SECONDS);
            assertThat(server.takeRequest(5, TimeUnit.SECONDS)).isNotNull();

            ws.sendCloseStream(ListenV2CloseStream.builder().build()).get(5, TimeUnit.SECONDS);
            ReconnectingWebSocketListener listener = getListener(ws);
            listener.onClosed(listener.getWebSocket(), 1005, "");

            assertThat(disconnected.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(server.takeRequest(100, TimeUnit.MILLISECONDS)).isNull();
        } finally {
            ws.disconnect();
        }
    }

    @Test
    @DisplayName("a rejected CloseStream does not make a no-status close terminal")
    void rejectedCloseStreamDoesNotSuppressReconnect() throws Exception {
        AtomicInteger connectionCount = new AtomicInteger();
        RejectingWebSocket webSocket = new RejectingWebSocket();
        WebSocketFactory factory = (request, listener) -> {
            connectionCount.incrementAndGet();
            listener.onOpen(webSocket, null);
            return webSocket;
        };
        V2WebSocketClient ws = new V2WebSocketClient(com.deepgram.core.ClientOptions.builder()
                .environment(Environment.PRODUCTION)
                .webSocketFactory(factory)
                .build());
        ws.reconnectOptions(ReconnectingWebSocketListener.ReconnectOptions.builder()
                .minReconnectionDelayMs(10)
                .maxReconnectionDelayMs(10)
                .build());

        try {
            ws.connect(V2ConnectOptions.builder()
                            .model(ListenV2Model.FLUX_GENERAL_EN)
                            .build())
                    .get(5, TimeUnit.SECONDS);
            ws.sendCloseStream(ListenV2CloseStream.builder().build()).get(5, TimeUnit.SECONDS);

            ReconnectingWebSocketListener listener = getListener(ws);
            listener.onClosed(webSocket, 1005, "");

            Thread.sleep(100);
            assertThat(connectionCount).hasValue(2);
        } finally {
            ws.disconnect();
        }
    }

    @Test
    @DisplayName("a new connection clears CloseStream terminal state")
    void newConnectionClearsCloseStreamTerminalState() throws Exception {
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {}));
        V2WebSocketClient ws = client.listen().v2().v2WebSocket();
        ws.reconnectOptions(ReconnectingWebSocketListener.ReconnectOptions.builder()
                .minReconnectionDelayMs(10)
                .maxReconnectionDelayMs(10)
                .build());

        try {
            ws.connect(V2ConnectOptions.builder()
                            .model(ListenV2Model.FLUX_GENERAL_EN)
                            .build())
                    .get(5, TimeUnit.SECONDS);
            assertThat(server.takeRequest(5, TimeUnit.SECONDS)).isNotNull();

            ws.sendCloseStream(ListenV2CloseStream.builder().build()).get(5, TimeUnit.SECONDS);
            ReconnectingWebSocketListener listener = getListener(ws);
            WebSocket webSocket = listener.getWebSocket();
            listener.onOpen(webSocket, null);
            listener.onClosed(webSocket, 1005, "");

            assertThat(server.takeRequest(100, TimeUnit.MILLISECONDS)).isNotNull();
        } finally {
            ws.disconnect();
        }
    }

    private ReconnectingWebSocketListener getListener(V2WebSocketClient ws) throws Exception {
        Field field = V2WebSocketClient.class.getDeclaredField("reconnectingListener");
        field.setAccessible(true);
        return (ReconnectingWebSocketListener) field.get(ws);
    }

    private static final class RejectingWebSocket implements WebSocket {
        @Override
        public Request request() {
            return new Request.Builder().url("ws://localhost/").build();
        }

        @Override
        public long queueSize() {
            return 0;
        }

        @Override
        public boolean send(String text) {
            return false;
        }

        @Override
        public boolean send(okio.ByteString bytes) {
            return false;
        }

        @Override
        public boolean close(int code, String reason) {
            return true;
        }

        @Override
        public void cancel() {}
    }
}
