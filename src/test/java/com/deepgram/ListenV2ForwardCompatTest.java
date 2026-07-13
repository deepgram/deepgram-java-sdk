package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.ClientOptions;
import com.deepgram.resources.listen.v2.websocket.V2WebSocketClient;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Forward-compatibility guard for the Listen v2 WebSocket dispatcher.
 *
 * <p>A message type this SDK version does not recognize must NOT be routed to the error handler:
 * GA adds benign control frames to this endpoint over time, and a deployed client must survive
 * them rather than treat them as fatal. The raw frame is still delivered to {@code onMessage(String)}.
 * Mirrors {@code SpeakV2ForwardCompatTest} and the JS/Python SDKs.
 *
 * <p>Drives the private {@code handleIncomingMessage} dispatcher directly (no network) via reflection.
 */
public class ListenV2ForwardCompatTest {

    @Test
    @DisplayName("unknown server message is delivered raw and not routed to onError")
    void unknownMessageIsNotAnError() throws Exception {
        V2WebSocketClient client = new V2WebSocketClient(ClientOptions.builder().build());

        AtomicInteger errorCount = new AtomicInteger();
        AtomicReference<String> rawMessage = new AtomicReference<>();
        client.onError(e -> errorCount.incrementAndGet());
        client.onMessage(rawMessage::set);

        String unknownFrame = "{\"type\":\"FutureMessage\",\"brand_new_field\":123}";

        Method handle = V2WebSocketClient.class.getDeclaredMethod("handleIncomingMessage", String.class);
        handle.setAccessible(true);
        handle.invoke(client, unknownFrame);

        // The unknown frame reached onMessage(String) verbatim...
        assertThat(rawMessage.get()).isEqualTo(unknownFrame);
        // ...and did NOT look like a fatal error to the consumer.
        assertThat(errorCount).hasValue(0);
    }
}
