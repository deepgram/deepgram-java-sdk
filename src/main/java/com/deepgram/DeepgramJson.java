package com.deepgram;

import com.deepgram.core.ObjectMappers;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides the Jackson mapper configured for Deepgram SDK request and response objects.
 */
public final class DeepgramJson {
    public static final ObjectMapper MAPPER = ObjectMappers.JSON_MAPPER;

    private DeepgramJson() {}
}
