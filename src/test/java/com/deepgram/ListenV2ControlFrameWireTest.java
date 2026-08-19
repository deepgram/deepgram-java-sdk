package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.Environment;
import com.deepgram.resources.listen.v2.types.ListenV2CloseStream;
import com.deepgram.resources.listen.v2.types.ListenV2ForceEndTurn;
import com.deepgram.resources.listen.v2.websocket.V2ConnectOptions;
import com.deepgram.resources.listen.v2.websocket.V2WebSocketClient;
import com.deepgram.types.ListenV2Model;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Wire coverage for the listen v2 client-to-server control frames, driven against MockWebServer.
 *
 * <p>{@code ForceEndTurn} is the reason this class exists. The message is currently rejected by the
 * live API on every deployment we can reach — the server recognises the type but refuses it with
 * {@code UNPARSABLE_CLIENT_MESSAGE} / "The ForceEndTurn message is not enabled on this deployment."
 * That gate makes an end-to-end integration test impossible, but it does not stop us from asserting
 * the half we own: that {@link V2WebSocketClient#sendForceEndTurn} puts a correctly serialized frame
 * on the wire. Without this, a public SDK method would ship with nothing verifying it at all, and a
 * future regen could reshape the frame silently.
 *
 * <p>{@code CloseStream} is included as a control: it exercises the same send path with a message the
 * server does accept, so a failure here distinguishes "the send path broke" from "ForceEndTurn
 * specifically broke".
 *
 * <p>Frozen via {@code src/test/} in .fernignore.
 */
class ListenV2ControlFrameWireTest {
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

    /** Connects, keeps the socket open, runs {@code action}, and returns the first frame the server received. */
    private String connectAndCaptureSentFrame(java.util.function.Consumer<V2WebSocketClient> action) throws Exception {
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                received.add(text);
                // Close server-side once we've captured a frame so MockWebServer can shut down cleanly.
                webSocket.close(1000, null);
            }
        }));
        V2WebSocketClient ws = client.listen().v2().v2WebSocket();
        try {
            ws.connect(V2ConnectOptions.builder()
                            .model(ListenV2Model.FLUX_GENERAL_EN)
                            .build())
                    .get(5, TimeUnit.SECONDS);
            action.accept(ws);
            return received.poll(5, TimeUnit.SECONDS);
        } finally {
            ws.disconnect();
        }
    }

    @Test
    @DisplayName("sendForceEndTurn serializes a ForceEndTurn frame")
    void sendForceEndTurnFrame() throws Exception {
        String frame =
                connectAndCaptureSentFrame(ws -> ws.sendForceEndTurn(ListenV2ForceEndTurn.builder().build()));

        assertThat(frame).as("ForceEndTurn frame reached the server").isNotNull();
        assertThat(frame).contains("\"type\":\"ForceEndTurn\"");
    }

    @Test
    @DisplayName("ForceEndTurn is a no-payload control message: type is its only field")
    void forceEndTurnCarriesNoPayload() throws Exception {
        String frame =
                connectAndCaptureSentFrame(ws -> ws.sendForceEndTurn(ListenV2ForceEndTurn.builder().build()));

        // The server rejects unknown fields on control messages, so the frame must carry nothing but
        // the discriminator. Asserted exactly rather than with contains() to catch stray fields.
        assertThat(frame).isEqualTo("{\"type\":\"ForceEndTurn\"}");
    }

    @Test
    @DisplayName("sendCloseStream serializes a CloseStream frame (control for the send path)")
    void sendCloseStreamFrame() throws Exception {
        String frame = connectAndCaptureSentFrame(ws -> ws.sendCloseStream(ListenV2CloseStream.builder().build()));

        assertThat(frame).as("CloseStream frame reached the server").isNotNull();
        assertThat(frame).contains("\"type\":\"CloseStream\"");
    }
}
