import com.deepgram.DeepgramClient;
import com.deepgram.resources.agent.v1.types.AgentV1InjectDynamicAgentMessage;
import com.deepgram.resources.agent.v1.types.AgentV1InjectDynamicAgentMessageBehavior;
import com.deepgram.resources.agent.v1.types.AgentV1Settings;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextThink;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAudio;
import com.deepgram.resources.agent.v1.types.AgentV1UpdateThinkLive;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.DeepgramThinkLiveProviderV1;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.ThinkLiveSettingsV1;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Configures an Agent V1 session with Think Live, updates its live behavior, and injects a dynamic agent message.
 *
 * <p>Usage: {@code java ThinkLiveAgent}
 */
public class ThinkLiveAgent {
    public static void main(String[] args) {
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build();
        V1WebSocketClient wsClient = client.agent().v1().v1WebSocket();
        CountDownLatch closeLatch = new CountDownLatch(1);

        try {
            wsClient.onWelcome(welcome -> wsClient.sendSettings(initialSettings()));
            wsClient.onSettingsApplied(event -> {
                System.out.println("Think Live settings applied; updating backchannel behavior.");
                wsClient.sendUpdateThinkLive(AgentV1UpdateThinkLive.builder()
                        .thinkLive(thinkLiveSettings(0.65))
                        .build());
            });
            wsClient.onThinkLiveUpdated(event -> {
                System.out.println("Think Live configuration updated; requesting a dynamic response.");
                wsClient.sendInjectDynamicAgentMessage(AgentV1InjectDynamicAgentMessage.builder()
                        .behavior(AgentV1InjectDynamicAgentMessageBehavior.QUEUE)
                        .build());
            });
            wsClient.onConversationText(text -> System.out.printf(
                    "[%s, %s] %s%n",
                    text.getRole(),
                    text.getConversationalBehavior().map(Object::toString).orElse("default"),
                    text.getContent()));
            wsClient.onFrustrationDetected(event -> System.out.printf(
                    "Frustration detected: count=%d, escalated=%s%n", event.getCount(), event.getEscalated()));
            wsClient.onInjectionRefused(event -> System.out.println("Dynamic injection refused: " + event));
            wsClient.onErrorMessage(event -> System.err.println("Agent error: " + event));
            wsClient.onError(error -> System.err.println("WebSocket error: " + error.getMessage()));
            wsClient.onDisconnected(reason -> closeLatch.countDown());

            wsClient.connect().get(10, TimeUnit.SECONDS);
            closeLatch.await(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wsClient.disconnect();
        }
    }

    private static AgentV1Settings initialSettings() {
        OpenAiThinkProvider provider = OpenAiThinkProvider.builder()
                .model(OpenAiThinkProviderModel.GPT4O_MINI)
                .build();
        return AgentV1Settings.builder()
                .audio(AgentV1SettingsAudio.builder().build())
                .agent(AgentV1SettingsAgent.of(AgentV1SettingsAgentContext.builder()
                        .think(AgentV1SettingsAgentContextThink.of(ThinkSettingsV1.builder()
                                .provider(ThinkSettingsV1Provider.openAi(provider))
                                .prompt("You are a concise, helpful voice assistant.")
                                .build()))
                        .thinkLive(thinkLiveSettings(0.35))
                        .greeting("Hello. I can respond while you are speaking.")
                        .build()))
                .build();
    }

    private static ThinkLiveSettingsV1 thinkLiveSettings(double backchannelSensitivity) {
        return ThinkLiveSettingsV1.builder()
                .provider(DeepgramThinkLiveProviderV1.builder()
                        .agentBackchannelSensitivity(backchannelSensitivity)
                        .presenceCheckMaxCount(2)
                        .presenceCheckIntervalMs(10_000)
                        .frustrationMaxCount(3)
                        .build())
                .build();
    }
}
