package com.deepgram.resources.listen.v1.websocket;

import com.deepgram.core.DeepgramApiException;
import com.deepgram.resources.listen.v1.types.ListenV1ConfigureFailure;

/**
 * Carries a Listen V1 {@code ConfigureFailure} message to the generic {@code onError} handler
 * when no {@code onConfigureFailure} handler is registered.
 */
public final class ListenV1ConfigureFailureException extends DeepgramApiException {
    private final ListenV1ConfigureFailure failure;

    public ListenV1ConfigureFailureException(ListenV1ConfigureFailure failure) {
        super(buildMessage(failure));
        this.failure = failure;
    }

    /** The Configure failure frame received from the server. */
    public ListenV1ConfigureFailure getFailure() {
        return failure;
    }

    private static String buildMessage(ListenV1ConfigureFailure failure) {
        return failure.getCode() + ": " + failure.getDescription() + " (" + failure.getRequestId() + ")";
    }
}
