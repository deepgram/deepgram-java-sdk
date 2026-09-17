package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.ObjectMappers;
import org.junit.jupiter.api.Test;

class DeepgramJsonTest {

    @Test
    void exposesTheSdkConfiguredMapper() {
        assertThat(DeepgramJson.MAPPER).isSameAs(ObjectMappers.JSON_MAPPER);
    }
}
