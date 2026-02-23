package tn.finora.services;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class YouTubeService {

    private static String apiKey() {
        String k = System.getenv("YOUTUBE_API_KEY");
        if (k == null || k.isBlank()) k = System.getProperty("YOUTUBE_API_KEY");
        if (k == null || k.isBlank()) {
            throw new IllegalStateException(
                    "YouTube API key missing. Set env YOUTUBE_API_KEY or JVM -DYOUTUBE_API_KEY=..."
            );
        }
        return k.trim();
    }

    public List<YouTubeVideo> search(String query, int maxResults, String order) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String safeOrder = (order == null || order.isBlank()) ? "relevance" : order.trim();

        String urlStr =
                "https://www.googleapis.com/youtube/v3/search" +
                        "?part=snippet&type=video" +
                        "&maxResults=" + Math.min(Math.max(maxResults, 1), 10) +
                        "&order=" + URLEncoder.encode(safeOrder, StandardCharsets.UTF_8) +
                        "&q=" + encoded +
                        "&key=" + apiKey();

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (code < 200 || code >= 300) {
            throw new RuntimeException("YouTube error HTTP " + code + ": " + sb);
        }

        return parseSearch(sb.toString());
    }

    private List<YouTubeVideo> parseSearch(String json) {
        List<YouTubeVideo> out = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        JsonArray items = root.getAsJsonArray("items");
        if (items == null) return out;

        for (JsonElement el : items) {
            JsonObject item = el.getAsJsonObject();

            String videoId = item.getAsJsonObject("id").get("videoId").getAsString();
            JsonObject snippet = item.getAsJsonObject("snippet");

            String title = snippet.get("title").getAsString();
            String channel = snippet.get("channelTitle").getAsString();

            // simple thumbnail
            String thumb = "";
            JsonObject thumbs = snippet.getAsJsonObject("thumbnails");
            if (thumbs != null) {
                if (thumbs.has("medium")) thumb = thumbs.getAsJsonObject("medium").get("url").getAsString();
                else if (thumbs.has("default")) thumb = thumbs.getAsJsonObject("default").get("url").getAsString();
            }

            out.add(new YouTubeVideo(videoId, title, channel, thumb));
        }
        return out;
    }

    public static class YouTubeVideo {
        public final String videoId;
        public final String title;
        public final String channelTitle;
        public final String thumbnailUrl;

        public YouTubeVideo(String videoId, String title, String channelTitle, String thumbnailUrl) {
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