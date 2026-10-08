package com.deepgram.resources.listen.v1.websocket;

import com.deepgram.core.DeepgramApiException;
import com.deepgram.resources.listen.v1.types.ListenV1Error;

/**
 * Carries a Listen V1 server {@code Error} message to the generic {@code onError} handler when no
 * {@code onErrorMessage} handler is registered, so callers on the generic path can still read the
 * typed {@link ListenV1Error} fields.
 */
public final class ListenV1ErrorException extends DeepgramApiException {
    private final ListenV1Error error;

    public ListenV1ErrorException(ListenV1Error error) {
        super(buildMessage(error));
        this.error = error;
    }

    /** The server error message as received, with {@code variant}, {@code description}, and optional {@code code}. */
    public ListenV1Error getError() {
        return error;
    }

    private static String buildMessage(ListenV1Error error) {
        String message = error.getVariant() + ": " + error.getDescription();
        if (error.getCode().isPresent()) {
            message += " (" + error.getCode().get() + ")";
        }
        return message;
    }
}
