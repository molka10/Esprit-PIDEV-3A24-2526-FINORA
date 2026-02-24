package tn.finora.services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SpeechService {

    private MediaPlayer mediaPlayer;

    public void speakFrench(String text) {

        try {
            if (text == null || text.isBlank()) {
                return;
            }

            // Encode text for URL
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            // French voice (Celine)
            String apiUrl =
                    "https://api.streamelements.com/kappa/v2/speech" +
                            "?voice=Celine&text=" + encodedText;

            // Download MP3 to temp file
            URL url = new URL(apiUrl);
            InputStream in = url.openStream();

            File tempFile = File.createTempFile("finora_tts_", ".mp3");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            // Stop previous audio if playing
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            Media media = new Media(tempFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}