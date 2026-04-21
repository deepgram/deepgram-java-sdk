import com.deepgram.DeepgramClient;
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeak;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeakEndpoint;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeakEndpointProvider;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeakOneItemProviderDeepgramModel;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThinkOneItem;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThinkOneItemProvider;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudio;
import com.deepgram.resources.agent.v1.types.Deepgram;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.Anthropic;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates configuring an agent with different LLM (think) and TTS (speak) providers. Uses Anthropic for thinking
 * and Deepgram for speech synthesis.
 *
 * <p>Usage: java CustomProviders
 */
public class CustomProviders {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Agent with Custom Providers");
        System.out.println("  Think: Anthropic Claude Sonnet 4");
        System.out.println("  Speak: Deepgram Aura 2 Asteria");
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
                    // Configure Anthropic as the think provider
                    Anthropic anthropicProvider = Anthropic.of(Map.of("model", "claude-sonnet-4-20250514"));

                    // Configure Deepgram as the speak provider
                    Deepgram deepgramSpeakProvider = Deepgram.builder()
                            .model(AgentV1SettingsAgentSpeakOneItemProviderDeepgramModel.AURA2ASTERIA_EN)
                            .build();

                    AgentV1SettingsAgentSpeak speakSettings =
                            AgentV1SettingsAgentSpeak.of(AgentV1SettingsAgentSpeakEndpoint.builder()
                                    .provider(AgentV1SettingsAgentSpeakEndpointProvider.deepgram(deepgramSpeakProvider))
                                    .build());

                    AgentV1SettingsAgent agentConfig = AgentV1SettingsAgent.builder()
                            .think(AgentV1SettingsAgentThink.of(List.of(AgentV1SettingsAgentThinkOneItem.builder()
                                    .provider(AgentV1SettingsAgentThinkOneItemProvider.of(anthropicProvider))
                                    .prompt("You are a helpful assistant. Keep responses concise.")
                                    .build())))
                            .speak(speakSettings)
                            .greeting("Hello! I'm powered by Anthropic Claude with Deepgram voices.")
                            .build();

                    AgentV1Settings settings = AgentV1Settings.builder()
                            .audio(AgentV1SettingsAudio.builder().build())
                            .agent(agentConfig)
                            .build();

                    wsClient.sendSettings(settings);
                    System.out.println("Settings sent with Anthropic + Deepgram providers");
                } catch (Exception e) {
                    System.err.println("Error sending settings: " + e.getMessage());
                }
            });

            wsClient.onSettingsApplied(applied -> {
                System.out.println("Settings applied successfully");
                System.out.println("Agent is ready with custom providers.");
                System.out.println("In a real application, you would stream audio here.");

                // Disconnect after demonstrating configuration
                new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                                wsClient.disconnect();
                            } catch (Exception e) {
                                // ignore
                            }
                        })
                        .start();
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
                System.out.printf("Received agent audio (%d bytes)%n", audioData.size());
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
            closeLatch.await(30, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            wsClient.disconnect();
        }
    }
}
