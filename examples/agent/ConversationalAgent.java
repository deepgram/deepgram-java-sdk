import com.deepgram.DeepgramClient;
import com.deepgram.resources.agent.v1.types.AgentV1InjectDynamicAgentMessage;
import com.deepgram.resources.agent.v1.types.AgentV1InjectDynamicAgentMessageBehavior;
import com.deepgram.resources.agent.v1.types.AgentV1UpdateThinkConversational;
import com.deepgram.resources.agent.v1.websocket.V1WebSocketClient;
import com.deepgram.types.DeepgramThinkConversationalProviderV1;
import com.deepgram.types.ThinkConversationalSettingsV1;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Demonstrates Conversational Mode control frames and event handlers for Agent V1. */
public class ConversationalAgent {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("DEEPGRAM_API_KEY environment variable is required");
        }

        try (DeepgramClient client = DeepgramClient.builder().apiKey(apiKey).build()) {
            V1WebSocketClient ws = client.agent().v1().v1WebSocket();
            ThinkConversationalSettingsV1 conversational = ThinkConversationalSettingsV1.builder()
                    .provider(DeepgramThinkConversationalProviderV1.builder()
                            .frustrationMaxCount(2)
                            .restrictedTerms(List.of("account number"))
                            .build())
                    .build();

            ws.onConversationText(text -> text.getConversationalBehavior()
                    .ifPresent(behavior -> System.out.println("Conversation behavior: " + behavior)));
            ws.onFrustrationDetected(event -> System.out.println(
                    "Frustration count=" + event.getCount() + ", escalated=" + event.getEscalated()));
            ws.onThinkConversationalUpdated(event -> System.out.println("Conversational settings updated"));

            try {
                ws.connect().get(10, TimeUnit.SECONDS);
                ws.sendUpdateThinkConversational(AgentV1UpdateThinkConversational.builder()
                                .thinkConversational(conversational)
                                .build())
                        .get(10, TimeUnit.SECONDS);
                ws.sendInjectDynamicAgentMessage(AgentV1InjectDynamicAgentMessage.builder()
                                .behavior(AgentV1InjectDynamicAgentMessageBehavior.QUEUE)
                                .build())
                        .get(10, TimeUnit.SECONDS);
            } finally {
                ws.close();
            }
        }
    }
}
