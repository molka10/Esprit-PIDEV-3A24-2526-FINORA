package com.example.finora.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class IpLocationService {

    public double[] getMyLocation() throws Exception {
        URL url = new URL("http://ip-api.com/json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
        double lat = root.get("lat").getAsDouble();
        double lon = root.get("lon").getAsDouble();
        return new double[]{lat, lon};
    }
}