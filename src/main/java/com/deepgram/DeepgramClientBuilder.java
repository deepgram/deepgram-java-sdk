package com.deepgram;

/**
 * Custom client builder extending the generated DeepgramApiClientBuilder.
 *
 * <p>Adds support for:
 *
 * <ul>
 *   <li>{@code accessToken} — Alternative to {@code apiKey}. Uses Bearer token authentication. If provided, takes
 *       precedence over apiKey for the Authorization header.
 *   <li>{@code sessionId} — Session identifier sent as {@code x-deepgram-session-id} header. If not provided, a UUID is
 *       auto-generated.
 * </ul>
 */
import com.deepgram.core.ClientOptions;
import com.deepgram.core.Environment;
import com.deepgram.core.LogConfig;
import com.deepgram.core.transport.DeepgramTransportFactory;
import com.deepgram.core.transport.TransportWebSocketFactory;
import java.util.UUID;
import okhttp3.OkHttpClient;

public class DeepgramClientBuilder extends DeepgramApiClientBuilder {
    private String accessToken;

    private String sessionId;

    private String apiKeyValue = System.getenv("DEEPGRAM_API_KEY");

    private DeepgramTransportFactory transportFactory;

    /**
     * Sets a custom transport factory for all WebSocket connections. When set, WebSocket clients will use this factory
     * instead of the default OkHttp WebSocket. Use this to route Deepgram API calls through alternative transports such
     * as SageMaker HTTP/2 streaming.
     */
    public DeepgramClientBuilder transportFactory(DeepgramTransportFactory transportFactory) {
        this.transportFactory = transportFactory;
        return this;
    }

    /**
     * Sets an access token (JWT) for Bearer authentication. If provided, this takes precedence over apiKey and sets the
     * Authorization header to {@code Bearer <token>}.
     */
    public DeepgramClientBuilder accessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Sets a session identifier sent as the {@code x-deepgram-session-id} header with every request and WebSocket
     * connection. If not provided, a UUID is auto-generated.
     */
    public DeepgramClientBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    // Override parent methods to return DeepgramClientBuilder for fluent chaining

    @Override
    public DeepgramClientBuilder apiKey(String apiKey) {
        this.apiKeyValue = apiKey;
        super.apiKey(apiKey);
        return this;
    }

    @Override
    public DeepgramClientBuilder environment(Environment environment) {
        super.environment(environment);
        return this;
    }

    @Override
    public DeepgramClientBuilder timeout(int timeout) {
        super.timeout(timeout);
        return this;
    }

    @Override
    public DeepgramClientBuilder maxRetries(int maxRetries) {
        super.maxRetries(maxRetries);
        return this;
    }

    @Override
    public DeepgramClientBuilder httpClient(OkHttpClient httpClient) {
        super.httpClient(httpClient);
        return this;
    }

    @Override
    public DeepgramClientBuilder logging(LogConfig logging) {
        super.logging(logging);
        return this;
    }

    @Override
    public DeepgramClientBuilder addHeader(String name, String value) {
        super.addHeader(name, value);
        return this;
    }

    @Override
    protected void setAuthentication(ClientOptions.Builder builder) {
        if (accessToken != null) {
            builder.addHeader("Authorization", "Bearer " + accessToken);
        } else {
            super.setAuthentication(builder);
        }
    }

    @Override
    protected void setAdditional(ClientOptions.Builder builder) {
        String sid = (sessionId != null) ? sessionId : UUID.randomUUID().toString();
        builder.addHeader("x-deepgram-session-id", sid);
        if (transportFactory != null) {
            builder.webSocketFactory(new TransportWebSocketFactory(transportFactory));
            // Custom transports manage their own connection lifecycle and retry semantics.
            // Wrapping them in ReconnectingWebSocketListener creates double-retry layering
            // that compounds failures under burst load. Auto-disable here; users can
            // re-enable explicitly via ClientOptions if they have a reason to.
            builder.reconnect(false);
        }
    }

    @Override
    public DeepgramClient build() {
        if (accessToken != null) {
            // accessToken flow — set placeholder apiKey so parent doesn't NPE
            super.apiKey("token");
        } else if (apiKeyValue == null) {
            throw new RuntimeException(
                    "Please provide apiKey, accessToken, or set the DEEPGRAM_API_KEY environment variable.");
        }
        validateConfiguration();
        return new DeepgramClient(buildClientOptions());
    }
}
