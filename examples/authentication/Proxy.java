import java.util.Collections;
import java.util.List;
import core.Environment;
import types.ListProjectsV1Response;
import types.ListProjectsV1ResponseProjectsItem;

/**
 * Demonstrates configuring a custom endpoint or proxy using Environment.custom().
 * Useful when routing through a proxy server or using a self-hosted deployment.
 *
 * <p>Usage: java Proxy
 */
public class Proxy {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        // Custom proxy or self-hosted endpoint
        String proxyUrl = System.getenv("DEEPGRAM_PROXY_URL");
        if (proxyUrl == null || proxyUrl.isEmpty()) {
            proxyUrl = "http://localhost:8080";
        }

        System.out.println("Custom Proxy / Endpoint Configuration");
        System.out.println("Proxy URL: " + proxyUrl);
        System.out.println();

        // Create a custom environment pointing all traffic through the proxy
        Environment customEnv = Environment.custom()
                .base(proxyUrl)
                .agent(proxyUrl.replace("http://", "wss://").replace("https://", "wss://"))
                .production(proxyUrl.replace("http://", "wss://").replace("https://", "wss://"))
                .build();

        // Create client with the custom environment
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .environment(customEnv)
                .build();

        try {
            // Verify connectivity through the proxy
            ListProjectsV1Response response = client.manage().v1().projects().list();

            List<ListProjectsV1ResponseProjectsItem> projects =
                    response.getProjects().orElse(Collections.emptyList());

            System.out.println("Connection through proxy successful!");
            System.out.printf("Found %d project(s):%n", projects.size());

            for (ListProjectsV1ResponseProjectsItem project : projects) {
                String id = project.getProjectId().orElse("unknown");
                String name = project.getName().orElse("unnamed");
                System.out.printf("  - %s (%s)%n", name, id);
            }

        } catch (Exception e) {
            System.err.println("Error connecting through proxy: " + e.getMessage());
            System.out.println();
            System.out.println("Note: This example requires a running proxy at " + proxyUrl);
            System.out.println("Set DEEPGRAM_PROXY_URL to point to your proxy server.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
