package com.example.finora.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechToTextService {

    private final AtomicBoolean listening = new AtomicBoolean(false);

    // Keep refs so stopListening() can REALLY stop the mic immediately
    private volatile TargetDataLine currentLine;
    private volatile Thread writerThread;
    private volatile File currentWavFile;

    private final HttpClient client = HttpClient.newHttpClient();

    // Deepgram config
    private static final String DG_URL =
            "https://api.deepgram.com/v1/listen?model=nova-2&language=fr&smart_format=true";

    // Audio config (16kHz mono PCM)
    private static final AudioFormat FORMAT = new AudioFormat(
            16000.0f, 16, 1, true, false
    );

    // UX: keep short for search
    private static final long MAX_RECORD_MS = 6000;

    public interface Listener {
        void onTextRecognized(String text);
    }

    public boolean isListening() {
        return listening.get();
    }

    public void startListening(Listener listener) {
        if (listener == null) return;
        if (listening.getAndSet(true)) return;

        writerThread = new Thread(() -> {
            TargetDataLine line = null;
            File wavFile = null;

            try {
                String apiKey = System.getenv("DEEPGRAM_API_KEY");
                if (apiKey == null || apiKey.isBlank()) {
                    throw new IllegalStateException("DEEPGRAM_API_KEY not set in environment variables.");
                }

                // Open mic line
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
                if (!AudioSystem.isLineSupported(info)) {
                    throw new IllegalStateException("Microphone line not supported on this system.");
                }

                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(FORMAT);
                line.start();

                currentLine = line;

                // Temp file
                wavFile = Files.createTempFile("finora_stt_", ".wav").toFile();
                wavFile.deleteOnExit();
                currentWavFile = wavFile;

                // Record until stopped or timeout
                AudioInputStream ais = new AudioInputStream(line);



// ✅ Make effectively final copies
                File finalWavFile = wavFile;
                AudioInputStream finalAis = ais;

                Thread wavWriter = new Thread(() -> {
                    try {
                        AudioSystem.write(finalAis, AudioFileFormat.Type.WAVE, finalWavFile);
                    } catch (Exception ignored) {
                    } finally {
                        try { finalAis.close(); } catch (Exception ignored) {}
                    }
                }, "FINORA-STT-WAV-WRITER");

                wavWriter.setDaemon(true);
                wavWriter.start();

                long start = System.currentTimeMillis();
                while (listening.get() && (System.currentTimeMillis() - start) < MAX_RECORD_MS) {
                    Thread.sleep(30);
                }

                // ✅ Stop mic NOW (this unblocks AudioSystem.write)
                safeStopAndCloseLine(line);

                // wait a bit for writer to finish flushing WAV header
                try { wavWriter.join(800); } catch (Exception ignored) {}

                // If file is too small, no speech captured
                if (!wavFile.exists() || wavFile.length() < 500) {
                    return;
                }

                // Send to Deepgram
                String transcript = transcribeWavWithDeepgram(apiKey, wavFile);
                if (transcript != null && !transcript.isBlank()) {
                    listener.onTextRecognized(transcript.trim());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                listening.set(false);

                // cleanup refs
                safeStopAndCloseLine(line);
                currentLine = null;

                File f = currentWavFile;
                currentWavFile = null;
                if (f != null) {
                    try { f.delete(); } catch (Exception ignored) {}
                }
            }
        }, "FINORA-STT");

        writerThread.setDaemon(true);
        writerThread.start();
    }

    public void stopListening() {
        listening.set(false);

        // ✅ FORCE stop mic immediately so UI doesn't get stuck
        TargetDataLine line = currentLine;
        if (line != null) safeStopAndCloseLine(line);

        // We don’t block UI waiting for thread
    }

    private void safeStopAndCloseLine(TargetDataLine line) {
        if (line == null) return;
        try { if (line.isRunning()) line.stop(); } catch (Exception ignored) {}
        try { if (line.isOpen()) line.close(); } catch (Exception ignored) {}
    }

    private String transcribeWavWithDeepgram(String apiKey, File wavFile) throws Exception {
        byte[] audioBytes = Files.readAllBytes(wavFile.toPath());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(DG_URL))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Token " + apiKey)
                .header("Content-Type", "audio/wav")
                .POST(HttpRequest.BodyPublishers.ofByteArray(audioBytes))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("Deepgram STT failed: " + resp.statusCode() + " => " + resp.body());
        }

        JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
        JsonObject results = root.getAsJsonObject("results");
        if (results == null) return "";

        JsonArray channels = results.getAsJsonArray("channels");
        if (channels == null || channels.size() == 0) return "";

        JsonObject ch0 = channels.get(0).getAsJsonObject();
        JsonArray alts = ch0.getAsJsonArray("alternatives");
        if (alts == null || alts.size() == 0) return "";

        JsonObject alt0 = alts.get(0).getAsJsonObject();
        return alt0.has("transcript") ? alt0.get("transcript").getAsString() : "";
    }
}