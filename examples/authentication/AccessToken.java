import types.GrantV1Response;

/**
 * Demonstrates generating a temporary access token (JWT) from an API key.
 * Access tokens are short-lived and suitable for use in client-side applications.
 *
 * <p>Usage: java AccessToken
 */
public class AccessToken {
    public static void main(String[] args) {
        // Get API key from environment
        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("DEEPGRAM_API_KEY environment variable is required");
            System.exit(1);
        }

        System.out.println("Generate Access Token (JWT)");
        System.out.println();

        // Create client using API key
        DeepgramClient client = DeepgramClient.builder()
                .apiKey(apiKey)
                .build();

        try {
            // Generate a temporary access token
            GrantV1Response tokenResponse = client.auth().v1().tokens().grant();

            String token = tokenResponse.getAccessToken();
            System.out.println("Access token generated successfully!");
            System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "...");

            tokenResponse.getExpiresIn().ifPresent(expiresIn ->
                    System.out.printf("Expires in: %.0f seconds%n", expiresIn));

            System.out.println();
            System.out.println("This token can be used for short-lived client-side authentication.");
            System.out.println("It provides usage::write permission for core voice APIs.");

        } catch (Exception e) {
            System.err.println("Error generating access token: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
