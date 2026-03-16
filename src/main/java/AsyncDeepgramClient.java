/**
 * Async version of {@link DeepgramClient}. Extends the generated AsyncDeepgramApiClient with the
 * same custom features.
 */
import core.ClientOptions;

public class AsyncDeepgramClient extends AsyncDeepgramApiClient {
  public AsyncDeepgramClient(ClientOptions clientOptions) {
    super(clientOptions);
  }

  public static AsyncDeepgramClientBuilder builder() {
    return new AsyncDeepgramClientBuilder();
  }
}
