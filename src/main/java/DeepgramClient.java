/**
 * Custom Deepgram client extending the generated DeepgramApiClient.
 *
 * <p>This is the primary entry point for using the Deepgram Java SDK. It extends the generated
 * client with additional features matching the Python and JavaScript SDKs:
 *
 * <ul>
 *   <li>Bearer token authentication via {@code accessToken()}
 *   <li>Automatic session ID header via {@code sessionId()}
 *   <li>Renamed from DeepgramApiClient to match JS/Python naming
 * </ul>
 *
 * <p>Usage with API key (default):
 *
 * <pre>{@code
 * DeepgramClient client = DeepgramClient.builder()
 *     .apiKey("your-api-key")
 *     .build();
 * }</pre>
 *
 * <p>Usage with access token (Bearer auth):
 *
 * <pre>{@code
 * DeepgramClient client = DeepgramClient.builder()
 *     .accessToken("your-jwt-token")
 *     .build();
 * }</pre>
 */
import core.ClientOptions;

public class DeepgramClient extends DeepgramApiClient {
  public DeepgramClient(ClientOptions clientOptions) {
    super(clientOptions);
  }

  public static DeepgramClientBuilder builder() {
    return new DeepgramClientBuilder();
  }
}
