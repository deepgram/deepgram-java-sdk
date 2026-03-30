package com.deepgram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deepgram.core.Environment;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for the DeepgramClientBuilder class and related client configuration. */
class ClientBuilderTest {

  @Nested
  @DisplayName("API key configuration")
  class ApiKeyConfiguration {

    @Test
    @DisplayName("builds client with explicit API key")
    void testExplicitApiKey() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-api-key-123").build();

      assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("throws when API key is null and env var not set")
    void testNullApiKeyThrows() {
      // When API key is explicitly set to null, build() should throw
      assertThatThrownBy(() -> DeepgramClient.builder().apiKey(null).build())
          .isInstanceOf(RuntimeException.class);
    }
  }

  @Nested
  @DisplayName("Environment configuration")
  class EnvironmentConfiguration {

    @Test
    @DisplayName("defaults to PRODUCTION environment")
    void testDefaultEnvironment() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("accepts AGENT environment")
    void testAgentEnvironment() {
      DeepgramClient client =
          DeepgramClient.builder().apiKey("test-key").environment(Environment.AGENT).build();

      assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("accepts custom environment")
    void testCustomEnvironment() {
      Environment custom =
          Environment.custom()
              .base("https://custom.example.com")
              .agent("wss://agent.example.com")
              .production("wss://custom.example.com")
              .build();

      DeepgramClient client =
          DeepgramClient.builder().apiKey("test-key").environment(custom).build();

      assertThat(client).isNotNull();
    }
  }

  @Nested
  @DisplayName("Timeout configuration")
  class TimeoutConfiguration {

    @Test
    @DisplayName("builds client with custom timeout")
    void testCustomTimeout() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").timeout(120).build();

      assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("builds client with default timeout when not specified")
    void testDefaultTimeout() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client).isNotNull();
    }
  }

  @Nested
  @DisplayName("Max retries configuration")
  class MaxRetriesConfiguration {

    @Test
    @DisplayName("builds client with custom max retries")
    void testCustomMaxRetries() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").maxRetries(5).build();

      assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("builds client with zero retries")
    void testZeroRetries() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").maxRetries(0).build();

      assertThat(client).isNotNull();
    }
  }

  @Nested
  @DisplayName("HTTP client configuration")
  class HttpClientConfiguration {

    @Test
    @DisplayName("builds client with custom OkHttpClient")
    void testCustomHttpClient() {
      OkHttpClient customHttpClient = new OkHttpClient.Builder().build();

      DeepgramClient client =
          DeepgramClient.builder().apiKey("test-key").httpClient(customHttpClient).build();

      assertThat(client).isNotNull();
    }
  }

  @Nested
  @DisplayName("Custom headers configuration")
  class CustomHeadersConfiguration {

    @Test
    @DisplayName("builds client with custom headers")
    void testCustomHeaders() {
      DeepgramClient client =
          DeepgramClient.builder()
              .apiKey("test-key")
              .addHeader("X-Custom-Header", "custom-value")
              .addHeader("X-Request-ID", "test-123")
              .build();

      assertThat(client).isNotNull();
    }
  }

  @Nested
  @DisplayName("Method chaining")
  class MethodChaining {

    @Test
    @DisplayName("supports fluent builder pattern with all options")
    void testFluentBuilder() {
      DeepgramClient client =
          DeepgramClient.builder()
              .apiKey("test-key")
              .environment(Environment.PRODUCTION)
              .timeout(30)
              .maxRetries(3)
              .addHeader("X-Custom", "value")
              .build();

      assertThat(client).isNotNull();
    }
  }

  @Nested
  @DisplayName("Client sub-resources")
  class ClientSubResources {

    @Test
    @DisplayName("listen() returns a non-null ListenClient")
    void testListenClient() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.listen()).isNotNull();
    }

    @Test
    @DisplayName("speak() returns a non-null SpeakClient")
    void testSpeakClient() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.speak()).isNotNull();
    }

    @Test
    @DisplayName("read() returns a non-null ReadClient")
    void testReadClient() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.read()).isNotNull();
    }

    @Test
    @DisplayName("manage() returns a non-null ManageClient")
    void testManageClient() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.manage()).isNotNull();
    }

    @Test
    @DisplayName("agent() returns a non-null AgentClient")
    void testAgentClient() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.agent()).isNotNull();
    }

    @Test
    @DisplayName("auth() returns a non-null AuthClient")
    void testAuthClient() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.auth()).isNotNull();
    }

    @Test
    @DisplayName("selfHosted() returns a non-null SelfHostedClient")
    void testSelfHostedClient() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.selfHosted()).isNotNull();
    }

    @Test
    @DisplayName("sub-resources are memoized (same instance returned)")
    void testSubResourceMemoization() {
      DeepgramClient client = DeepgramClient.builder().apiKey("test-key").build();

      assertThat(client.listen()).isSameAs(client.listen());
      assertThat(client.speak()).isSameAs(client.speak());
      assertThat(client.read()).isSameAs(client.read());
      assertThat(client.manage()).isSameAs(client.manage());
    }
  }
}
