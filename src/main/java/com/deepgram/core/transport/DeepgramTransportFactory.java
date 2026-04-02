package com.deepgram.core.transport;

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
@FunctionalInterface
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
}
