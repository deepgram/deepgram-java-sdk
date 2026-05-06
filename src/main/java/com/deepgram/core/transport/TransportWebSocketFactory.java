package com.deepgram.core.transport;

import com.deepgram.core.ReconnectingWebSocketListener;
import com.deepgram.core.WebSocketFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * A {@link WebSocketFactory} that delegates to a {@link DeepgramTransportFactory}.
 *
 * <p>This bridges the generated SDK's {@code WebSocketFactory} seam with the pluggable transport
 * system. When the generated WebSocket clients call {@code webSocketFactory.create(request,
 * listener)}, this class:
 *
 * <ol>
 *   <li>Extracts the URL and headers from the OkHttp {@link Request}
 *   <li>Calls the {@link DeepgramTransportFactory} to create a {@link DeepgramTransport}
 *   <li>Wraps the transport in a {@link TransportWebSocketAdapter} that implements {@link WebSocket}
 * </ol>
 *
 * <p>This means zero changes to any Fern-generated code.
 */
public class TransportWebSocketFactory implements WebSocketFactory {
    private final DeepgramTransportFactory transportFactory;

    public TransportWebSocketFactory(DeepgramTransportFactory transportFactory) {
        this.transportFactory = transportFactory;
    }

    @Override
    public WebSocket create(Request request, WebSocketListener listener) {
        // Apply the plugin-declared reconnect policy to the SDK's wrapping listener. Plugins that
        // own their own retry/backoff (SageMaker) return maxRetries(0) here so the wrapper-level
        // reconnect doesn't compound their internal retries into a storm.
        if (listener instanceof ReconnectingWebSocketListener) {
            ((ReconnectingWebSocketListener) listener).applyOptionsOverride(transportFactory.reconnectOptions());
        }

        String url = request.url().toString();
        // Restore wss:// scheme — OkHttp's HttpUrl normalizes to https://
        if (url.startsWith("https://")) {
            url = "wss://" + url.substring("https://".length());
        } else if (url.startsWith("http://")) {
            url = "ws://" + url.substring("http://".length());
        }

        Map<String, String> headers = new LinkedHashMap<>();
        for (String name : request.headers().names()) {
            headers.put(name, request.header(name));
        }

        DeepgramTransport transport = transportFactory.create(url, headers);
        return new TransportWebSocketAdapter(transport, request, listener);
    }
}
