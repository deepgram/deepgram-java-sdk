import com.deepgram.DeepgramClient;
import com.deepgram.resources.agent.v1.types.AgentV1InjectAgentMessage;
import com.deepgram.resources.agent.v1.types.AgentV1InjectUserMessage;
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudio;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates injecting messages into an agent conversation. You can inject both user messages (as if the user said
 * something) and agent messages (as if the agent said something).
 *
 * <p>Usage: java InjectMessage
 */
public class InjectMessage {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Agent Message Injection");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        // Get the Agent WebSocket client
        V1WebSocketClient wsClient = client.agent().v1().v1WebSocket();

        CountDownLatch closeLatch = new CountDownLatch(1);

        try {
            // Register event handlers
            wsClient.onConnected(() -> {
                System.out.println("WebSocket connected");
            });

            wsClient.onWelcome(welcome -> {
                System.out.println("Agent welcome: request_id=" + welcome.getRequestId());

                try {
                    // Configure the agent
                    OpenAiThinkProvider openAiProvider = OpenAiThinkProvider.builder()
                            .model(OpenAiThinkProviderModel.GPT4O_MINI)
                            .build();

                    AgentV1Settings settings = AgentV1Settings.builder()
                            .audio(AgentV1SettingsAudio.builder().build())
                            .agent(AgentV1SettingsAgent.builder()
                                    .think(AgentV1SettingsAgentThink.of(ThinkSettingsV1.builder()
                                            .provider(ThinkSettingsV1Provider.openAi(openAiProvider))
                                            .prompt(
                                                    "You are a helpful voice assistant. Keep responses brief and conversational.")
                                            .build()))
                                    .greeting("Hello! I'm ready to chat.")
                                    .build())
                            .build();

                    wsClient.sendSettings(settings);
                    System.out.println("Settings sent");
                } catch (Exception e) {
                    System.err.println("Error sending settings: " + e.getMessage());
                }
            });

            wsClient.onSettingsApplied(applied -> {
                System.out.println("Settings applied successfully");
                System.out.println();

                // Inject messages in a separate thread
                new Thread(() -> injectMessages(wsClient, closeLatch)).start();
            });

            wsClient.onConversationText(text -> {
                System.out.printf("[%s] %s%n", text.getRole(), text.getContent());
            });

            wsClient.onAgentThinking(event -> {
                System.out.println(">> Agent thinking...");
            });

            wsClient.onAgentStartedSpeaking(event -> {
                System.out.println(">> Agent started speaking");
            });

            wsClient.onAgentAudioDone(event -> {
                System.out.println(">> Agent finished speaking");
            });

            wsClient.onAgentV1Audio(audioData -> {
                // In a real application, play this audio
            });

            wsClient.onInjectionRefused(refused -> {
                System.out.println("Injection refused: " + refused);
            });

            wsClient.onError(error -> {
                System.err.println("Error: " + error.getMessage());
            });

            wsClient.onErrorMessage(error -> {
                System.err.println("Agent error: " + error);
            });

            wsClient.onDisconnected(reason -> {
                System.out.println(
                        "\nConnection closed (code: " + reason.getCode() + ", reason: " + reason.getReason() + ")");
                closeLatch.countDown();
            });

            // Connect to the WebSocket
            CompletableFuture<Void> connectFuture = wsClient.connect();
            connectFuture.get(10, TimeUnit.SECONDS);

            // Wait for completion
            closeLatch.await(60, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            wsClient.disconnect();
        }
    }

    /** Inject user and agent messages into the conversation. */
    private static void injectMessages(V1WebSocketClient wsClient, CountDownLatch closeLatch) {
        try {
            // Wait for greeting to be processed
            Thread.sleep(3000);

            // Inject a user message (as if the user spoke)
            System.out.println();
            System.out.println("--- Injecting user message ---");
            wsClient.sendInjectUserMessage(AgentV1InjectUserMessage.builder()
                    .content("What is the capital of France?")
                    .build());

            // Wait for the agent to respond
            Thread.sleep(8000);

            // Inject an agent message (force the agent to say something)
            System.out.println();
            System.out.println("--- Injecting agent message ---");
            wsClient.sendInjectAgentMessage(AgentV1InjectAgentMessage.builder()
                    .message("By the way, I can also help you with math and science questions!")
                    .build());

            // Wait for the response to be processed
            Thread.sleep(5000);

            System.out.println();
            System.out.println("Message injection demo complete. Disconnecting...");
            wsClient.disconnect();

        } catch (Exception e) {
            System.err.println("Error injecting messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
