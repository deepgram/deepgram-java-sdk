import com.deepgram.DeepgramClient;
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudio;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import okio.ByteString;

/**
 * Voice agent example using the Agent V1 WebSocket. Demonstrates connecting to a conversational AI agent, configuring
 * settings, sending audio, and handling conversation events.
 *
 * <p>This example downloads a sample audio file and streams it to the agent, then listens for the agent's response.
 *
 * <p>Usage: java VoiceAgent [path-to-audio-file]
 */
public class VoiceAgent {
    private static final String SAMPLE_AUDIO_URL =
            "https://static.deepgram.com/examples/Bueller-Life-moves-pretty-fast.wav";

    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Voice Agent (Agent V1 WebSocket)");
        System.out.println();

        // Create client
        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();

        // Get the Agent WebSocket client
        V1WebSocketClient wsClient = client.agent().v1().v1WebSocket();

        CountDownLatch closeLatch = new CountDownLatch(1);

        try {
            // Register event handlers before connecting
            wsClient.onConnected(() -> {
                System.out.println("WebSocket connected");
            });

            wsClient.onWelcome(welcome -> {
                System.out.println("Agent welcome: request_id=" + welcome.getRequestId());

                // Send agent settings after receiving welcome
                try {
                    // Configure the LLM think provider (OpenAI)
                    OpenAiThinkProvider openAiProvider = OpenAiThinkProvider.builder()
                            .model(OpenAiThinkProviderModel.GPT4O_MINI)
                            .build();

                    ThinkSettingsV1 thinkSettings = ThinkSettingsV1.builder()
                            .provider(ThinkSettingsV1Provider.openAi(openAiProvider))
                            .prompt("You are a helpful voice assistant. Keep your responses brief.")
                            .build();

                    AgentV1SettingsAgentContext agentContext = AgentV1SettingsAgentContext.builder()
                            .think(AgentV1SettingsAgentContextThink.of(thinkSettings))
                            .greeting("Hello! How can I help you today?")
                            .build();
                    AgentV1SettingsAgent agentConfig = AgentV1SettingsAgent.of(agentContext);

                    AgentV1Settings settings = AgentV1Settings.builder()
                            .audio(AgentV1SettingsAudio.builder().build())
                            .agent(agentConfig)
                            .build();

                    wsClient.sendSettings(settings);
                    System.out.println("Settings sent to agent");
                } catch (Exception e) {
                    System.err.println("Error sending settings: " + e.getMessage());
                }
            });

            wsClient.onSettingsApplied(applied -> {
                System.out.println("Settings applied successfully");
                System.out.println("Streaming audio to agent...");

                // Stream audio in a separate thread so we don't block the event handler
                new Thread(() -> streamAudioToAgent(wsClient, args)).start();
            });

            wsClient.onConversationText(text -> {
                System.out.printf("[%s] %s%n", text.getRole(), text.getContent());
            });

            wsClient.onUserStartedSpeaking(event -> {
                System.out.println(">> User started speaking");
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
                // In a real application, you would play this audio
                System.out.printf("Received agent audio (%d bytes)%n", audioData.size());
            });

            wsClient.onFunctionCallRequest(request -> {
                System.out.println("Function call requested: " + request);
            });

            wsClient.onErrorMessage(error -> {
                System.err.println("Agent error: " + error);
            });

            wsClient.onWarning(warning -> {
                System.out.println("Agent warning: " + warning);
            });

            wsClient.onError(error -> {
                System.err.println("Error: " + error.getMessage());
            });

            wsClient.onDisconnected(reason -> {
                System.out.println(
                        "\nConnection closed (code: " + reason.getCode() + ", reason: " + reason.getReason() + ")");
                closeLatch.countDown();
            });

            // Connect to the WebSocket
            CompletableFuture<Void> connectFuture = wsClient.connect();
            connectFuture.get(10, TimeUnit.SECONDS);

            System.out.println("Waiting for agent response...");

            // Wait for the conversation to play out
            closeLatch.await(60, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            wsClient.disconnect();
        }
    }

    /**
     * Streams audio data to the agent. Uses a local file if provided as an argument, otherwise downloads a sample audio
     * file.
     */
    private static void streamAudioToAgent(V1WebSocketClient wsClient, String[] args) {
        try {
            InputStream audioStream;
            if (args.length > 0) {
                System.out.println("Streaming audio from file: " + args[0]);
                audioStream = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(args[0]));
            } else {
                System.out.println("Downloading sample audio: " + SAMPLE_AUDIO_URL);
                audioStream = URI.create(SAMPLE_AUDIO_URL).toURL().openStream();
            }

            // Stream audio in chunks
            byte[] buffer = new byte[8192];
            int bytesRead;
            int totalBytes = 0;
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                wsClient.sendMedia(ByteString.of(chunk, 0, bytesRead));
                totalBytes += bytesRead;
            }
            audioStream.close();

            System.out.printf("Finished streaming audio (%d bytes)%n", totalBytes);
            System.out.println("Waiting for agent to respond...");

            // Give the agent time to process and respond
            Thread.sleep(15000);

            System.out.println("\nDisconnecting...");
            wsClient.disconnect();

        } catch (Exception e) {
            System.err.println("Error streaming audio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
