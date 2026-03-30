package com.deepgram.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link Environment} class. */
class EnvironmentTest {

    @Nested
    @DisplayName("PRODUCTION environment")
    class ProductionEnvironment {

        @Test
        @DisplayName("base URL points to api.deepgram.com")
        void testBaseUrl() {
            assertThat(Environment.PRODUCTION.getBaseURL()).isEqualTo("https://api.deepgram.com");
        }

        @Test
        @DisplayName("agent URL points to wss://agent.deepgram.com")
        void testAgentUrl() {
            assertThat(Environment.PRODUCTION.getAgentURL()).isEqualTo("wss://agent.deepgram.com");
        }

        @Test
        @DisplayName("production URL uses wss://api.deepgram.com")
        void testProductionUrl() {
            assertThat(Environment.PRODUCTION.getProductionURL()).isEqualTo("wss://api.deepgram.com");
        }
    }

    @Nested
    @DisplayName("AGENT environment")
    class AgentEnvironment {

        @Test
        @DisplayName("base URL points to agent.deepgram.com")
        void testBaseUrl() {
            assertThat(Environment.AGENT.getBaseURL()).isEqualTo("https://agent.deepgram.com");
        }

        @Test
        @DisplayName("agent URL points to wss://agent.deepgram.com")
        void testAgentUrl() {
            assertThat(Environment.AGENT.getAgentURL()).isEqualTo("wss://agent.deepgram.com");
        }

        @Test
        @DisplayName("production URL uses wss://api.deepgram.com")
        void testProductionUrl() {
            assertThat(Environment.AGENT.getProductionURL()).isEqualTo("wss://api.deepgram.com");
        }
    }

    @Nested
    @DisplayName("Custom environment builder")
    class CustomEnvironment {

        @Test
        @DisplayName("builds environment with all custom URLs")
        void testCustomBuilder() {
            Environment custom = Environment.custom()
                    .base("https://custom-api.example.com")
                    .agent("wss://custom-agent.example.com")
                    .production("wss://custom-api.example.com")
                    .build();

            assertThat(custom.getBaseURL()).isEqualTo("https://custom-api.example.com");
            assertThat(custom.getAgentURL()).isEqualTo("wss://custom-agent.example.com");
            assertThat(custom.getProductionURL()).isEqualTo("wss://custom-api.example.com");
        }

        @Test
        @DisplayName("builds environment with partial custom URLs")
        void testPartialCustomBuilder() {
            Environment custom =
                    Environment.custom().base("https://my-proxy.example.com").build();

            assertThat(custom.getBaseURL()).isEqualTo("https://my-proxy.example.com");
            // Unset fields should be null
            assertThat(custom.getAgentURL()).isNull();
            assertThat(custom.getProductionURL()).isNull();
        }
    }
}
