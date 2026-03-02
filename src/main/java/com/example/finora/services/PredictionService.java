package com.example.finora.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PredictionService {

    private final String apiKey = System.getenv("GROQ_API_KEY");

    public String callAI(String prompt) {

        try {

            org.json.JSONObject json = new org.json.JSONObject();
            json.put("model", "llama3-8b-8192");

            org.json.JSONArray messages = new org.json.JSONArray();

            org.json.JSONObject message = new org.json.JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            json.put("model", "llama-3.1-8b-instant");
            messages.put(message);
            json.put("messages", messages);

            String body = json.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("RAW RESPONSE:");
            System.out.println(response.body());

            org.json.JSONObject responseJson =
                    new org.json.JSONObject(response.body());

            if (!responseJson.has("choices")) {
                return "Erreur API : " + response.body();
            }

            String result = responseJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur Groq : " + e.getMessage();
        }
    }
}