package tn.finora.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class RecommendationApiService {

    private static final String API_URL =
            "http://localhost:8081/api/recommendations";

    public List<String> fetchRecommendations() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body();

            // Suppression des crochets et guillemets
            json = json.replace("[", "")
                    .replace("]", "")
                    .replace("\"", "");

            return Arrays.asList(json.split(","));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of();
    }
}