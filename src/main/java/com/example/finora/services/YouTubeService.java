package com.example.finora.services;


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
public class YouTubeService {

    private static String apiKey() {
        String key = System.getenv("YOUTUBE_API_KEY");

        if (key == null || key.isBlank()) {
            key = System.getProperty("YOUTUBE_API_KEY");
        }

        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "YouTube API key missing. Set environment variable YOUTUBE_API_KEY " +
                            "or run with -DYOUTUBE_API_KEY=your_key"
            );
        }

        return key.trim();
    }

    public List<YouTubeVideo> search(String query, int maxResults, String order) throws Exception {

        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String safeOrder = (order == null || order.isBlank()) ? "relevance" : order.trim();

        int safeMax = Math.min(Math.max(maxResults, 1), 10);

        String urlStr =
                "https://www.googleapis.com/youtube/v3/search" +
                        "?part=snippet&type=video" +
                        "&maxResults=" + safeMax +
                        "&order=" + URLEncoder.encode(safeOrder, StandardCharsets.UTF_8) +
                        "&q=" + encodedQuery +
                        "&key=" + apiKey();

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder response = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        if (code < 200 || code >= 300) {
            throw new RuntimeException("YouTube API error HTTP " + code + ": " + response);
        }

        conn.disconnect();

        return parseSearch(response.toString());
    }

    private List<YouTubeVideo> parseSearch(String json) {

        List<YouTubeVideo> results = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray items = root.getAsJsonArray("items");

        if (items == null) return results;

        for (JsonElement element : items) {

            JsonObject item = element.getAsJsonObject();

            JsonObject idObj = item.getAsJsonObject("id");
            if (idObj == null || !idObj.has("videoId")) continue;

            String videoId = idObj.get("videoId").getAsString();

            JsonObject snippet = item.getAsJsonObject("snippet");
            if (snippet == null) continue;

            String title = snippet.has("title")
                    ? snippet.get("title").getAsString()
                    : "No title";

            String channel = snippet.has("channelTitle")
                    ? snippet.get("channelTitle").getAsString()
                    : "Unknown channel";

            String thumb = "";

            if (snippet.has("thumbnails")) {
                JsonObject thumbs = snippet.getAsJsonObject("thumbnails");

                if (thumbs.has("medium")) {
                    thumb = thumbs.getAsJsonObject("medium")
                            .get("url").getAsString();
                } else if (thumbs.has("default")) {
                    thumb = thumbs.getAsJsonObject("default")
                            .get("url").getAsString();
                }
            }

            results.add(new YouTubeVideo(videoId, title, channel, thumb));
        }

        return results;
    }

    // ================= INNER CLASS =================

    public static class YouTubeVideo {

        public final String videoId;
        public final String title;
        public final String channelTitle;
        public final String thumbnailUrl;

        public YouTubeVideo(String videoId,
                            String title,
                            String channelTitle,
                            String thumbnailUrl) {
            this.videoId = videoId;
            this.title = title;
            this.channelTitle = channelTitle;
            this.thumbnailUrl = thumbnailUrl;
        }

        public String watchUrl() {
            return "https://www.youtube.com/watch?v=" + videoId;
        }

        @Override
        public String toString() {
            return title + " — " + channelTitle;
        }
    }
}