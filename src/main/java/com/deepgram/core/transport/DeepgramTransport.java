package com.deepgram.core.transport;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Pluggable transport interface for Deepgram WebSocket APIs.
 *
 * <p>The default SDK transport uses OkHttp WebSockets. Alternative implementations — such as
 * SageMaker HTTP/2 streaming — can be injected via {@link DeepgramTransportFactory}.
 *
 * <p>This interface models the same bidirectional messaging pattern as a WebSocket: send
 * text/binary messages, receive text/binary messages via callbacks, and close when done.
 */
public interface DeepgramTransport extends AutoCloseable {

    /** Send binary data (audio frames) asynchronously. */
    CompletableFuture<Void> sendBinary(byte[] data);

    /** Send text data (JSON control messages) asynchronously. */
    CompletableFuture<Void> sendText(String data);

    /** Register a listener for incoming text messages (JSON responses). */
    void onTextMessage(Consumer<String> listener);

    /** Register a listener for incoming binary messages (audio data). */
    void onBinaryMessage(Consumer<byte[]> listener);

    /** Register a listener invoked when the connection is established. */
    void onOpen(Runnable listener);

    /** Register a listener for transport errors. */
    void onError(Consumer<Throwable> listener);

    /**
     * Register a listener for transport close events.
     *
     * @param listener receives the close code (e.g. 1000 for normal) and reason
     */
    void onClose(CloseListener listener);

    /** Returns true if the transport connection is open. */
    boolean isOpen();

    /** Close the transport gracefully. */
    @Override
    void close();

    /** Listener for close events. */
    @FunctionalInterface
    interface CloseListener {
        void onClose(int code, String reason);
    }
}
