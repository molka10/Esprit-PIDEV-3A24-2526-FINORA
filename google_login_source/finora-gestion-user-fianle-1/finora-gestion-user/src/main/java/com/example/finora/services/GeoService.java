package com.example.finora.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeoService {

    // ✅ safer than hardcoding in code
    private static String apiKey() {
        String k = System.getenv("POSITIONSTACK_API_KEY");
        if (k == null || k.isBlank()) k = System.getProperty("POSITIONSTACK_API_KEY");


        if (k == null || k.isBlank()) {
            throw new IllegalStateException(
                    "Missing POSITIONSTACK_API_KEY (env or -DPOSITIONSTACK_API_KEY=...)"
            );
        }
        return k.trim();
    }

    public double[] getCoordinates(String city) throws Exception {

        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a city.");
        }

        // ✅ encode (so “New York” won’t break)
        String query = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);

        // ✅ use HTTPS
        String endpoint = "https://api.positionstack.com/v1/forward"
                + "?access_key=" + apiKey()
                + "&query=" + query
                + "&limit=1";

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        String response = readAll(is);

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Geocoding API error (" + code + "): " + response);
        }

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonArray data = root.getAsJsonArray("data");

        if (data == null || data.size() == 0) {
            throw new RuntimeException("City not found: " + city);
        }

        JsonObject first = data.get(0).getAsJsonObject();

        if (!first.has("latitude") || !first.has("longitude")
                || first.get("latitude").isJsonNull()
                || first.get("longitude").isJsonNull()) {
            throw new RuntimeException("No coordinates returned for: " + city);
        }

        double lat = first.get("latitude").getAsDouble();
        double lng = first.get("longitude").getAsDouble();

        return new double[]{lat, lng};
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}