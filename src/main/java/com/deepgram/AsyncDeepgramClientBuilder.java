package com.deepgram;

/**
 * Async version of {@link DeepgramClientBuilder}. Extends the generated
 * AsyncDeepgramApiClientBuilder with the same custom features.
 *
 * <p>Adds support for:
 *
 * <ul>
 *   <li>{@code accessToken} — Alternative to {@code apiKey}. Uses Bearer token authentication. If
 *       provided, takes precedence over apiKey for the Authorization header.
 *   <li>{@code sessionId} — Session identifier sent as {@code x-deepgram-session-id} header. If not
 *       provided, a UUID is auto-generated.
 * </ul>
 */
import com.deepgram.core.ClientOptions;
import com.deepgram.core.Environment;
import com.deepgram.core.LogConfig;
import java.util.UUID;
import okhttp3.OkHttpClient;

public class AsyncDeepgramClientBuilder extends AsyncDeepgramApiClientBuilder {
  private String accessToken;

  private String sessionId;

  private String apiKeyValue = System.getenv("DEEPGRAM_API_KEY");

  /**
   * Sets an access token (JWT) for Bearer authentication. If provided, this takes precedence over
   * apiKey and sets the Authorization header to {@code Bearer <token>}.
   */
  public AsyncDeepgramClientBuilder accessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  /**
   * Sets a session identifier sent as the {@code x-deepgram-session-id} header with every request
   * and WebSocket connection. If not provided, a UUID is auto-generated.
   */
  public AsyncDeepgramClientBuilder sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  // Override parent methods to return AsyncDeepgramClientBuilder for fluent chaining

  @Override
  public AsyncDeepgramClientBuilder apiKey(String apiKey) {
    this.apiKeyValue = apiKey;
    super.apiKey(apiKey);
    return this;
  }

  @Override
  public AsyncDeepgramClientBuilder environment(Environment environment) {
    super.environment(environment);
    return this;
  }

  @Override
  public AsyncDeepgramClientBuilder timeout(int timeout) {
    super.timeout(timeout);
    return this;
  }

  @Override
  public AsyncDeepgramClientBuilder maxRetries(int maxRetries) {
    super.maxRetries(maxRetries);
    return this;
  }

  @Override
  public AsyncDeepgramClientBuilder httpClient(OkHttpClient httpClient) {
    super.httpClient(httpClient);
    return this;
  }

  @Override
  public AsyncDeepgramClientBuilder logging(LogConfig logging) {
    super.logging(logging);
    return this;
  }

  @Override
  public AsyncDeepgramClientBuilder addHeader(String name, String value) {
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
  }

  @Override
  public AsyncDeepgramClient build() {
    if (accessToken != null) {
      // accessToken flow — set placeholder apiKey so parent doesn't NPE
      super.apiKey("token");
    } else if (apiKeyValue == null) {
      throw new RuntimeException(
          "Please provide apiKey, accessToken, or set the DEEPGRAM_API_KEY environment variable.");
    }
    validateConfiguration();
    return new AsyncDeepgramClient(buildClientOptions());
  }
}
