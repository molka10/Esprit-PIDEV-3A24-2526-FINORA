package com.example.finora.finorainves;

import com.example.finora.entities.Investment;
import com.example.finora.services.InvestmentService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class ApiServer {

    private static HttpServer server;

    // Cache externe
    private static Map<String, Double> trendCache = new HashMap<>();

    public static void start() {
        try {

            server = HttpServer.create(new InetSocketAddress(7777), 0);

            server.createContext("/api/recommendation/internal",
                    exchange -> handleRequest(exchange, getTopInternal(1)));

            server.createContext("/api/recommendation/external",
                    exchange -> handleRequest(exchange, getTopExternal(1)));

            server.createContext("/api/recommendation/internal/top3",
                    exchange -> handleRequest(exchange, getTopInternal(3)));

            server.createContext("/api/recommendation/external/top3",
                    exchange -> handleRequest(exchange, getTopExternal(3)));

            server.start();

            System.out.println("✅ API started on:");
            System.out.println("   /internal");
            System.out.println("   /external");
            System.out.println("   /internal/top3");
            System.out.println("   /external/top3");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // COMMON HANDLER
    // =============================
    private static void handleRequest(HttpExchange exchange, String response) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");

        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // =============================
    // INTERNAL TOP N
    // =============================

    private static String getTopInternal(int limit) {

        InvestmentService service = new InvestmentService();
        List<Investment> investments = service.getAll();

        if (investments.isEmpty())
            return "{\"message\":\"No investments found\"}";

        double minValue = investments.stream()
                .mapToDouble(inv -> inv.getEstimatedValue().doubleValue())
                .min().orElse(0);

        double maxValue = investments.stream()
                .mapToDouble(inv -> inv.getEstimatedValue().doubleValue())
                .max().orElse(1);

        List<Map<String, Object>> ranked = investments.stream()
                .map(inv -> {

                    double value = inv.getEstimatedValue().doubleValue();
                    double normalizedValue =
                            (value - minValue) / (maxValue - minValue + 1);

                    double riskScore = mapRiskToScore(inv.getRiskLevel());

                    double score =
                            (0.6 * normalizedValue) +
                                    (0.4 * riskScore);

                    Map<String, Object> map = new HashMap<>();
                    map.put("investment", inv.getName());
                    map.put("score", score);
                    return map;
                })
                .sorted((a, b) ->
                        Double.compare(
                                (Double) b.get("score"),
                                (Double) a.get("score")))
                .limit(limit)
                .collect(Collectors.toList());

        return toJson(ranked);
    }

    // =============================
    // EXTERNAL TOP N
    // =============================

    private static String getTopExternal(int limit) {

        InvestmentService service = new InvestmentService();
        List<Investment> investments = service.getAll();

        if (investments.isEmpty())
            return "{\"message\":\"No investments found\"}";

        List<Map<String, Object>> ranked = investments.stream()
                .map(inv -> {

                    double trend =
                            getExternalTrendScore(inv.getCategory());

                    Map<String, Object> map = new HashMap<>();
                    map.put("investment", inv.getName());
                    map.put("trendScore", trend);
                    return map;
                })
                .sorted((a, b) ->
                        Double.compare(
                                (Double) b.get("trendScore"),
                                (Double) a.get("trendScore")))
                .limit(limit)
                .collect(Collectors.toList());

        return toJson(ranked);
    }

    // =============================
    // GNEWS TREND
    // =============================

    private static double getExternalTrendScore(String category) {

        if (trendCache.containsKey(category)) {
            return trendCache.get(category);
        }

        try {

            String API_KEY = "371557068804ff4ea6f27be68c3f8b6a"; // ⚠️ remplace par ta nouvelle clé

            String url = "https://gnews.io/api/v4/search?q=investment+"
                    + category +
                    "+Tunisia"
                    + "&lang=fr&max=10&token=" + API_KEY;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();

            int articleCount = body.split("\"url\"").length - 1;

            double score = articleCount / 20.0;

            if (score > 1.0) score = 1.0;

            trendCache.put(category, score);

            return score;

        } catch (Exception e) {
            return 0.2;
        }
    }

    // =============================
    // RISK MAPPING
    // =============================

    private static double mapRiskToScore(String risk) {

        if (risk == null) return 0.5;

        return switch (risk.toUpperCase()) {
            case "LOW" -> 1.0;
            case "MEDIUM" -> 0.6;
            case "HIGH" -> 0.2;
            default -> 0.5;
        };
    }

    // =============================
    // JSON BUILDER
    // =============================

    private static String toJson(List<Map<String, Object>> list) {

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("{");

            Map<String, Object> item = list.get(i);
            int count = 0;

            for (Map.Entry<String, Object> entry : item.entrySet()) {

                sb.append("\"")
                        .append(entry.getKey())
                        .append("\":");

                if (entry.getValue() instanceof String)
                    sb.append("\"").append(entry.getValue()).append("\"");
                else
                    sb.append(String.format("%.2f", entry.getValue()));

                if (++count < item.size())
                    sb.append(",");
            }

            sb.append("}");

            if (i < list.size() - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}