package tn.finora.services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SpeechService {

    private MediaPlayer player;

    // ✅ Correct Voice ID (case sensitive)
    private static final String VOICE_ID = "sANWqF1bCMzR6eyZbCGw";

    public void speak(String text) {

        try {

            String apiKey = System.getenv("ELEVENLABS_API_KEY");

            if (apiKey == null || apiKey.isBlank()) {
                System.out.println("❌ ELEVENLABS_API_KEY not set.");
                return;
            }

            URL url = new URL("https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("xi-api-key", apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String safeText = text.replace("\"", "\\\"");

            String jsonBody = """
                    {
                      "text": "%s",
                      "model_id": "eleven_monolingual_v1",
                      "voice_settings": {
                        "stability": 0.5,
                        "similarity_boost": 0.7
                      }
                    }
                    """.formatted(safeText);

            connection.getOutputStream()
                    .write(jsonBody.getBytes(StandardCharsets.UTF_8));

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                System.out.println("❌ ElevenLabs error code: " + responseCode);
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    System.out.println(new String(errorStream.readAllBytes(), StandardCharsets.UTF_8));
                }
                return;
            }

            InputStream audioStream = connection.getInputStream();

            // ✅ Unique temporary file
            File audioFile = File.createTempFile("elevenlabs_", ".mp3");
            audioFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                audioStream.transferTo(fos);
            }

            if (player != null) {
                player.stop();
                player.dispose();
            }

            Media media = new Media(audioFile.toURI().toString());
            player = new MediaPlayer(media);

            player.setOnEndOfMedia(() -> {
                player.dispose();
            });

            player.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
            player.dispose();
        }
    }
}