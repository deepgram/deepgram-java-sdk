import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContext;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextSpeak;
import com.deepgram.resources.agent.v1.types.AgentV1SettingsAgentContextThink;
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
        SpeakSettingsV1 speakSettings = SpeakSettingsV1.builder()
                .provider(SpeakSettingsV1Provider.deepgram(deepgramSpeak))
                .build();

        // Combination 1: OpenAI GPT-4o Mini + Deepgram
        System.out.println("=== Combination 1: OpenAI + Deepgram ===");
        OpenAiThinkProvider openAiProvider = OpenAiThinkProvider.builder()
                .model(OpenAiThinkProviderModel.GPT4O_MINI)
                .build();
        ThinkSettingsV1 openAiThink = ThinkSettingsV1.builder()
                .provider(ThinkSettingsV1Provider.openAi(openAiProvider))
                .prompt("You are a helpful assistant powered by OpenAI.")
                .build();

        AgentV1SettingsAgentContext openAiConfig = AgentV1SettingsAgentContext.builder()
                .think(AgentV1SettingsAgentContextThink.of(openAiThink))
                .speak(AgentV1SettingsAgentContextSpeak.of(speakSettings))
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
        ThinkSettingsV1 anthropicThink = ThinkSettingsV1.builder()
                .provider(ThinkSettingsV1Provider.anthropic(anthropicProvider))
                .prompt("You are a helpful assistant powered by Anthropic Claude.")
                .build();

        AgentV1SettingsAgentContext anthropicConfig = AgentV1SettingsAgentContext.builder()
                .think(AgentV1SettingsAgentContextThink.of(anthropicThink))
                .speak(AgentV1SettingsAgentContextSpeak.of(speakSettings))
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
        ThinkSettingsV1 googleThink = ThinkSettingsV1.builder()
                .provider(ThinkSettingsV1Provider.google(googleProvider))
                .prompt("You are a helpful assistant powered by Google Gemini.")
                .build();

        AgentV1SettingsAgentContext googleConfig = AgentV1SettingsAgentContext.builder()
                .think(AgentV1SettingsAgentContextThink.of(googleThink))
                .speak(AgentV1SettingsAgentContextSpeak.of(speakSettings))
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
