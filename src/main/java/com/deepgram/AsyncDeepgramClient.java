package com.deepgram;

/**
 * Async version of {@link DeepgramClient}. Extends the generated AsyncDeepgramApiClient with the same custom features.
 */
import com.deepgram.core.ClientOptions;

public class AsyncDeepgramClient extends AsyncDeepgramApiClient implements AutoCloseable {
    private final boolean ownsHttpClient;

    public AsyncDeepgramClient(ClientOptions clientOptions) {
        this(clientOptions, false);
    }

    AsyncDeepgramClient(ClientOptions clientOptions, boolean ownsHttpClient) {
        super(clientOptions);
        this.ownsHttpClient = ownsHttpClient;
    }

    public static AsyncDeepgramClientBuilder builder() {
        return new AsyncDeepgramClientBuilder();
    }

    /**
     * Releases resources owned by an SDK-created HTTP client. Clients supplied through
     * {@link AsyncDeepgramClientBuilder#httpClient(okhttp3.OkHttpClient)} remain owned by the caller.
     */
    @Override
    public void close() {
        if (!ownsHttpClient) {
            return;
        }
        clientOptions.httpClient().dispatcher().executorService().shutdown();
        clientOptions.httpClient().connectionPool().evictAll();
    }
}
