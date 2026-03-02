package com.example.finora.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * 🔐 GoogleAuthService
 * Implements the OAuth 2.0 Authorization Code flow for a JavaFX desktop app.
 *
 * Flow:
 * 1. Open browser → Google login page
 * 2. Google redirects to localhost:8085/callback?code=...
 * 3. Exchange code for access token
 * 4. Fetch user info (name, email, picture)
 */
public class GoogleAuthService {

    private static final String CLIENT_ID = getEnvOrDefault("GOOGLE_CLIENT_ID", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXachnager");
    private static final String CLIENT_SECRET = getEnvOrDefault("GOOGLE_CLIENT_SECRET", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    private static final String REDIRECT_URI = getEnvOrDefault("GOOGLE_REDIRECT_URI", "http://localhost:8085/callback");
    private static final int PORT = 8085;

    // ── Public result record ────────────────────────────────────
    public record GoogleUser(String name, String email, String pictureUrl) {
    }

    // ── Entry point ─────────────────────────────────────────────
    /**
     * Starts the Google OAuth flow:
     * 1. Opens the browser on the Google consent page
     * 2. Waits for the callback (max 2 minutes)
     * 3. Exchanges the code for a token and fetches user info
     *
     * @return GoogleUser with name, email, picture — or null on failure
     */
    public GoogleUser authenticate() throws Exception {
        // 1. Build the authorization URL
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(CLIENT_ID)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&access_type=offline"
                + "&prompt=consent";

        // 2. Open browser
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            throw new UnsupportedOperationException("Cannot open browser. Please open manually: " + authUrl);
        }

        // 3. Wait for the callback on localhost:8085/callback
        String code = waitForAuthCode();
        if (code == null)
            throw new Exception("Google login timed out or was cancelled.");

        // 4. Exchange the code for an access token
        String accessToken = exchangeCodeForToken(code);
        if (accessToken == null)
            throw new Exception("Failed to obtain access token from Google.");

        // 5. Fetch user info using the token
        return fetchUserInfo(accessToken);
    }

    // ── Step 3: Local HTTP server to receive the code ───────────
    private String waitForAuthCode() throws Exception {
        CompletableFuture<String> codeFuture = new CompletableFuture<>();

        ServerSocket server = new ServerSocket(PORT);
        server.setSoTimeout(120_000); // 2 minute timeout

        Thread listenerThread = new Thread(() -> {
            try (Socket socket = server.accept()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                String requestLine = reader.readLine();
                System.out.println("[GoogleAuth] Received: " + requestLine);

                // Extract code from GET /callback?code=XXXX
                String code = null;
                if (requestLine != null && requestLine.contains("code=")) {
                    String query = requestLine.split(" ")[1]; // e.g. /callback?code=4/...
                    for (String param : query.substring(query.indexOf('?') + 1).split("&")) {
                        if (param.startsWith("code=")) {
                            code = URLDecoder.decode(param.substring(5), StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }

                // Send success page back to the browser
                String html = "<html><body style='font-family:sans-serif;text-align:center;padding:60px'>"
                        + "<h2 style='color:#6D28D9'>✅ Connexion réussie !</h2>"
                        + "<p>Vous pouvez fermer cet onglet et revenir sur Finora.</p>"
                        + "</body></html>";
                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n" + html;
                socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();

                codeFuture.complete(code);

            } catch (Exception e) {
                codeFuture.completeExceptionally(e);
            } finally {
                try {
                    server.close();
                } catch (Exception ignored) {
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        try {
            return codeFuture.get(120, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            server.close();
            return null;
        }
    }

    // ── Step 4: Exchange authorization code for access token ────
    private String exchangeCodeForToken(String code) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String body = "code=" + encode(code)
                + "&client_id=" + encode(CLIENT_ID)
                + "&client_secret=" + encode(CLIENT_SECRET)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[GoogleAuth] Token response: " + response.body());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (json.has("access_token")) {
            return json.get("access_token").getAsString();
        }
        return null;
    }

    // ── Step 5: Fetch user info ──────────────────────────────────
    private GoogleUser fetchUserInfo(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[GoogleAuth] UserInfo response: " + response.body());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        String name = json.has("name") ? json.get("name").getAsString() : "Google User";
        String email = json.has("email") ? json.get("email").getAsString() : null;
        String picture = json.has("picture") ? json.get("picture").getAsString() : null;

        if (email == null)
            throw new Exception("Google account has no email.");

        return new GoogleUser(name, email, picture);
    }

    // ── Helper ───────────────────────────────────────────────────
    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }

    private static String mustGet(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Environment variable missing: " + key);
        }
        return v;
    }
}
