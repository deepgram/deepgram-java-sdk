package com.deepgram.core.transport;

import com.deepgram.core.ReconnectingWebSocketListener;
import java.util.Map;

/**
 * Factory for creating {@link DeepgramTransport} instances. Injected into the Deepgram client
 * builder to replace the default OkHttp WebSocket transport with alternatives like SageMaker HTTP/2
 * streaming.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * DeepgramClient client = DeepgramClient.builder()
 *     .apiKey("unused")  // SageMaker uses AWS credentials
 *     .transportFactory(mySageMakerFactory)
 *     .build();
 * }</pre>
 *
 * <p>When a transport factory is set, all WebSocket clients (Listen, Speak, Agent) will use it
 * instead of the default OkHttp WebSocket connection.
 */
public interface DeepgramTransportFactory {

    /**
     * Create a transport connection.
     *
     * @param url the full Deepgram endpoint URL including query parameters (e.g.
     *     wss://api.deepgram.com/v1/listen?model=nova-3)
     * @param headers authentication and configuration headers
     * @return a connected or connecting transport instance
     */
    DeepgramTransport create(String url, Map<String, String> headers);

    /**
     * Reconnect policy the SDK should apply when wrapping connections produced by this factory.
     * Returning {@code null} (the default) leaves the SDK's {@link ReconnectingWebSocketListener}
     * defaults in place.
     *
     * <p>Plugins that own their own connection lifecycle and retry/backoff (e.g. SageMaker bidi
     * streaming) should return {@code ReconnectOptions.builder().maxRetries(0).build()} so the
     * SDK's wrapper-level reconnect doesn't compound their internal retries into a storm.
     *
     * @return reconnect options to apply, or {@code null} for SDK defaults
     */
    default ReconnectingWebSocketListener.ReconnectOptions reconnectOptions() {
        return null;
    }
}
