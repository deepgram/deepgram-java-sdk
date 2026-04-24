import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgent;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentSpeak;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentThink;
import com.deepgram.types.Anthropic;
import com.deepgram.types.AnthropicThinkProviderModel;
import com.deepgram.types.Deepgram;
import com.deepgram.types.DeepgramSpeakProviderModel;
import com.deepgram.types.Google;
import com.deepgram.types.GoogleThinkProviderModel;
import com.deepgram.types.OpenAiThinkProvider;
import com.deepgram.types.OpenAiThinkProviderModel;
import com.deepgram.types.SpeakSettingsV1;
import com.deepgram.types.SpeakSettingsV1Provider;
import com.deepgram.types.ThinkSettingsV1;
import com.deepgram.types.ThinkSettingsV1Provider;
import java.util.List;

/**
 * Demonstrates building different provider combination configurations for comparison. Shows how to configure OpenAI,
 * Anthropic, and Google as think providers, each paired with Deepgram as the speak provider.
 *
 * <p>Usage: java ProviderCombinations
 */
public class ProviderCombinations {
    public static void main(String[] args) {
        System.out.println("Agent Provider Combinations");
        System.out.println("Comparing different LLM provider configurations.");
        System.out.println();

        // Shared speak provider (Deepgram TTS)
        Deepgram deepgramSpeak = Deepgram.builder()
                .model(DeepgramSpeakProviderModel.AURA2ASTERIA_EN)
                .build();
        AgentV1SettingsAgentSpeak speakSettings = AgentV1SettingsAgentSpeak.of(SpeakSettingsV1.builder()
                .provider(SpeakSettingsV1Provider.deepgram(deepgramSpeak))
                .build());

        // Combination 1: OpenAI GPT-4o Mini + Deepgram
        System.out.println("=== Combination 1: OpenAI + Deepgram ===");
        OpenAiThinkProvider openAiProvider = OpenAiThinkProvider.builder()
                .model(OpenAiThinkProviderModel.GPT4O_MINI)
                .build();

        AgentV1SettingsAgent openAiConfig = AgentV1SettingsAgent.builder()
                .think(AgentV1SettingsAgentThink.of(List.of(ThinkSettingsV1.builder()
                        .provider(ThinkSettingsV1Provider.openAi(openAiProvider))
                        .prompt("You are a helpful assistant powered by OpenAI.")
                        .build())))
                .speak(speakSettings)
                .greeting("Hello! I'm powered by OpenAI GPT-4o Mini.")
                .build();
        System.out.println("  Think: OpenAI GPT-4o Mini");
        System.out.println("  Speak: Deepgram Aura 2 Asteria");
        System.out.println("  Config built successfully.");
        System.out.println();

        // Combination 2: Anthropic Claude + Deepgram
        System.out.println("=== Combination 2: Anthropic + Deepgram ===");
        Anthropic anthropicProvider = Anthropic.builder()
                .model(AnthropicThinkProviderModel.CLAUDE_SONNET420250514)
                .build();

        AgentV1SettingsAgent anthropicConfig = AgentV1SettingsAgent.builder()
                .think(AgentV1SettingsAgentThink.of(List.of(ThinkSettingsV1.builder()
                        .provider(ThinkSettingsV1Provider.anthropic(anthropicProvider))
                        .prompt("You are a helpful assistant powered by Anthropic Claude.")
                        .build())))
                .speak(speakSettings)
                .greeting("Hello! I'm powered by Anthropic Claude.")
                .build();
        System.out.println("  Think: Anthropic Claude Sonnet 4");
        System.out.println("  Speak: Deepgram Aura 2 Asteria");
        System.out.println("  Config built successfully.");
        System.out.println();

        // Combination 3: Google Gemini + Deepgram
        System.out.println("=== Combination 3: Google + Deepgram ===");
        Google googleProvider =
                Google.builder().model(GoogleThinkProviderModel.GEMINI25FLASH).build();

        AgentV1SettingsAgent googleConfig = AgentV1SettingsAgent.builder()
                .think(AgentV1SettingsAgentThink.of(List.of(ThinkSettingsV1.builder()
                        .provider(ThinkSettingsV1Provider.google(googleProvider))
                        .prompt("You are a helpful assistant powered by Google Gemini.")
                        .build())))
                .speak(speakSettings)
                .greeting("Hello! I'm powered by Google Gemini.")
                .build();
        System.out.println("  Think: Google Gemini 2.5 Flash");
        System.out.println("  Speak: Deepgram Aura 2 Asteria");
        System.out.println("  Config built successfully.");
        System.out.println();

        System.out.println("All three provider combinations built successfully.");
        System.out.println("In a real application, you would pass these configurations");
        System.out.println("to wsClient.sendSettings() after connecting to the Agent WebSocket.");
    }
}
