package com.example.finora.services;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

public class SpeechService {

    // =================== Deepgram config ===================
    private static final String DG_TTS_URL =
            "https://api.deepgram.com/v1/speak?model=aura-2-agathe-fr&encoding=linear16&container=wav";

    // smaller chunks => first audio returns faster
    private static final int MAX_CHARS_PER_CHUNK = 350;

    // =================== State ===================
    private MediaPlayer player;
    private int rate = 0;

    private volatile boolean stopped = false;

    // chunk queue for CURRENT paragraph
    private final Deque<String> chunkQueue = new ArrayDeque<>();

    // paragraph queue for whole lesson
    private final Deque<String> paragraphQueue = new ArrayDeque<>();
    private int paragraphTotal = 0;
    private int paragraphIndex = 0;

    private ParagraphListener paragraphListener;

    private final HttpClient client = HttpClient.newHttpClient();

    // =================== Public API ===================

    public void setRate(int rate) {
        this.rate = rate;
        Platform.runLater(() -> {
            if (player != null) player.setRate(mapRate(rate));
        });
    }

    public boolean isPaused() {
        return player != null && player.getStatus() == MediaPlayer.Status.PAUSED;
    }

    public boolean isPlaying() {
        return player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public void pause() {
        Platform.runLater(() -> {
            if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
            }
        });
    }

    public void resume() {
        Platform.runLater(() -> {
            if (player != null && player.getStatus() == MediaPlayer.Status.PAUSED) {
                player.play();
            }
        });
    }

    public void stop() {
        stopped = true;

        chunkQueue.clear();
        paragraphQueue.clear();
        paragraphListener = null;

        Platform.runLater(() -> {
            try {
                if (player != null) {
                    player.stop();
                    player.dispose();
                    player = null;
                }
            } catch (Exception ignored) {}
        });
    }

    // Simple speak (single text)
    public void speak(String text) {
        if (text == null || text.isBlank()) return;

        stop();          // full reset
        stopped = false;

        // treat as 1 paragraph
        paragraphQueue.clear();
        paragraphQueue.addLast(text.trim());
        paragraphTotal = 1;
        paragraphIndex = 0;
        paragraphListener = null;

        startNextParagraph();
    }

    // Paragraph mode (for highlighting)
    public interface ParagraphListener {
        void onParagraphStart(int index, int total);
        void onFinished();
    }

    public void speakParagraphs(java.util.List<String> paragraphs, ParagraphListener listener) {
        if (paragraphs == null || paragraphs.isEmpty()) return;

        stop();
        stopped = false;

        paragraphQueue.clear();
        for (String p : paragraphs) {
            if (p != null && !p.isBlank()) paragraphQueue.addLast(p.trim());
        }

        paragraphTotal = paragraphQueue.size();
        paragraphIndex = 0;
        paragraphListener = listener;

        if (paragraphTotal == 0) return;

        startNextParagraph();
    }

    // =================== Engine ===================

    private void startNextParagraph() {
        if (stopped) return;

        String apiKey = System.getenv("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("DEEPGRAM_API_KEY not set in environment variables.");
        }

        String paragraph = paragraphQueue.pollFirst();
        if (paragraph == null) {
            // done
            if (paragraphListener != null) {
                paragraphListener.onFinished();
            }
            return;
        }

        // notify UI highlight
        int current = paragraphIndex;
        paragraphIndex++;

        if (paragraphListener != null) {
            paragraphListener.onParagraphStart(current, paragraphTotal);
        }

        // fill chunk queue for this paragraph
        chunkQueue.clear();
        for (String chunk : splitIntoChunks(clean(paragraph), MAX_CHARS_PER_CHUNK)) {
            if (!chunk.isBlank()) chunkQueue.addLast(chunk);
        }

        playNextChunk(apiKey);
    }

    private void playNextChunk(String apiKey) {
        if (stopped) return;

        String chunk = chunkQueue.pollFirst();
        if (chunk == null) {
            // finished this paragraph -> start next one
            startNextParagraph();
            return;
        }

        String json = "{\"text\":" + quoteJson(chunk) + "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(DG_TTS_URL))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Token " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "audio/wav")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(resp -> {
                    if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                        throw new RuntimeException("Deepgram TTS failed: " + resp.statusCode());
                    }
                    return resp.body();
                })
                .thenAccept(wavBytes -> {
                    try {
                        File wav = Files.createTempFile("finora_tts_", ".wav").toFile();
                        wav.deleteOnExit();
                        Files.write(wav.toPath(), wavBytes);

                        Platform.runLater(() -> {
                            if (stopped) return;

                            try {
                                Media media = new Media(wav.toURI().toString());
                                player = new MediaPlayer(media);
                                player.setRate(mapRate(rate));

                                player.setOnError(() -> {
                                    safeDisposePlayer();
                                    playNextChunk(apiKey);
                                });

                                player.setOnEndOfMedia(() -> {
                                    safeDisposePlayer();
                                    playNextChunk(apiKey);
                                });

                                player.play();
                            } catch (Exception e) {
                                e.printStackTrace();
                                safeDisposePlayer();
                                playNextChunk(apiKey);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        playNextChunk(apiKey);
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    playNextChunk(apiKey); // skip chunk and continue
                    return null;
                });
    }

    private void safeDisposePlayer() {
        try {
            if (player != null) {
                player.dispose();
                player = null;
            }
        } catch (Exception ignored) {}
    }

    // =================== Helpers ===================

    private String clean(String s) {
        return s.replace("\u0000", " ")
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double mapRate(int r) {
        return Math.max(0.5, Math.min(2.0, 1.0 + (r * 0.06)));
    }

    private static Deque<String> splitIntoChunks(String text, int maxChars) {
        Deque<String> out = new ArrayDeque<>();
        if (text == null || text.isBlank()) return out;

        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + maxChars, text.length());
            int cut = lastGoodCut(text, i, end);
            if (cut <= i) cut = end;

            out.add(text.substring(i, cut).trim());
            i = cut;
        }
        return out;
    }

    private static int lastGoodCut(String t, int start, int end) {
        for (int k = end - 1; k > start; k--) {
            char c = t.charAt(k);
            if (c == '.' || c == '!' || c == '?') return k + 1;
        }
        for (int k = end - 1; k > start; k--) {
            if (Character.isWhitespace(t.charAt(k))) return k;
        }
        return end;
    }

    private String quoteJson(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}