import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import resources.agent.v1.types.AgentV1Settings;
import resources.agent.v1.types.AgentV1SettingsAgent;
import resources.agent.v1.types.AgentV1SettingsAgentContext;
import resources.agent.v1.types.AgentV1SettingsAgentContextSpeak;
import resources.agent.v1.types.AgentV1SettingsAgentContextThink;
import resources.agent.v1.types.AgentV1SettingsAudio;
import resources.agent.v1.websocket.V1WebSocketClient;
import types.Anthropic;
import types.AnthropicThinkProviderModel;
import types.Deepgram;
import types.DeepgramSpeakProviderModel;
import types.SpeakSettingsV1;
import types.SpeakSettingsV1Provider;
import types.ThinkSettingsV1;
import types.ThinkSettingsV1Provider;

/**
 * Demonstrates configuring an agent with different LLM (think) and TTS (speak) providers.
 * Uses Anthropic for thinking and Deepgram for speech synthesis.
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
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

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
                    Anthropic anthropicProvider = Anthropic.builder()
                            .model(AnthropicThinkProviderModel.CLAUDE_SONNET420250514)
                            .build();

                    ThinkSettingsV1 thinkSettings = ThinkSettingsV1.builder()
                            .provider(ThinkSettingsV1Provider.anthropic(anthropicProvider))
                            .prompt("You are a helpful assistant. Keep responses concise.")
                            .build();

                    // Configure Deepgram as the speak provider
                    Deepgram deepgramSpeakProvider = Deepgram.builder()
                            .model(DeepgramSpeakProviderModel.AURA2ASTERIA_EN)
                            .build();

                    SpeakSettingsV1 speakSettings = SpeakSettingsV1.builder()
                            .provider(SpeakSettingsV1Provider.deepgram(deepgramSpeakProvider))
                            .build();

                    // Build agent settings with both providers
                    AgentV1SettingsAgentContext agentContext = AgentV1SettingsAgentContext.builder()
                            .think(AgentV1SettingsAgentContextThink.of(thinkSettings))
                            .speak(AgentV1SettingsAgentContextSpeak.of(speakSettings))
                            .greeting("Hello! I'm powered by Anthropic Claude with Deepgram voices.")
                            .build();

                    AgentV1Settings settings = AgentV1Settings.builder()
                            .audio(AgentV1SettingsAudio.builder().build())
                            .agent(AgentV1SettingsAgent.of(agentContext))
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
                }).start();
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
                System.out.println("\nConnection closed (code: " + reason.getCode()
                        + ", reason: " + reason.getReason() + ")");
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
