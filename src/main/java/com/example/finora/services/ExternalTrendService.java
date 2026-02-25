package com.example.finora.recommendation_api.services;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ExternalTrendService {

    private final String API_KEY = "PUT_YOUR_KEY_HERE";

    public String fetchTrends(String query) {

        try {
            String url =
                    "https://newsapi.org/v2/everything?q="
                            + query +
                            "&apiKey=" + API_KEY;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request,
                            HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}