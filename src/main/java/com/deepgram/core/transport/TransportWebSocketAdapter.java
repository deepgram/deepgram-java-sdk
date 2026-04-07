package com.deepgram.core.transport;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Adapts a {@link DeepgramTransport} to the OkHttp {@link WebSocket} interface.
 *
 * <p>This allows custom transports to be used transparently with the generated WebSocket clients
 * via the existing {@link com.deepgram.core.WebSocketFactory} seam — no modifications to
 * Fern-generated code required.
 *
 * <p>The adapter:
 * <ul>
 *   <li>Bridges {@code send(String)} / {@code send(ByteString)} to the transport's async methods
 *   <li>Routes transport callbacks ({@code onOpen}, {@code onTextMessage}, etc.) back to the
 *       OkHttp {@link WebSocketListener}
 *   <li>Bridges {@code close()} to the transport's close method
 * </ul>
 */
class TransportWebSocketAdapter implements WebSocket {
    private final DeepgramTransport transport;
    private final Request request;

    TransportWebSocketAdapter(DeepgramTransport transport, Request request, WebSocketListener listener) {
        this.transport = transport;
        this.request = request;

        transport.onOpen(() -> listener.onOpen(this, new Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(101)
                .message("Switching Protocols")
                .build()));

        transport.onTextMessage(text -> listener.onMessage(this, text));

        transport.onBinaryMessage(bytes -> listener.onMessage(this, ByteString.of(bytes)));

        transport.onError(error -> listener.onFailure(this, error, null));

        transport.onClose((code, reason) -> listener.onClosed(this, code, reason != null ? reason : ""));
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public long queueSize() {
        return 0;
    }

    @Override
    public boolean send(String text) {
        if (!transport.isOpen()) return false;
        transport.sendText(text);
        return true;
    }

    @Override
    public boolean send(ByteString bytes) {
        if (!transport.isOpen()) return false;
        transport.sendBinary(bytes.toByteArray());
        return true;
    }

    @Override
    public boolean close(int code, String reason) {
        transport.close();
        return true;
    }

    @Override
    public void cancel() {
        transport.close();
    }
}
