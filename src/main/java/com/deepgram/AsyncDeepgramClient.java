package com.deepgram;

/**
 * Async version of {@link DeepgramClient}. Extends the generated AsyncDeepgramApiClient with the same custom features.
 */
import com.deepgram.core.ClientOptions;

public class AsyncDeepgramClient extends AsyncDeepgramApiClient {
    public AsyncDeepgramClient(ClientOptions clientOptions) {
        super(clientOptions);
    }

    public static AsyncDeepgramClientBuilder builder() {
        return new AsyncDeepgramClientBuilder();
    }
}
