package com.example.finora.finorainves;

import com.example.finora.entities.Investment;
import com.example.finora.services.InvestmentService;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.List;

public class ApiServer {

    private static HttpServer server;

    public static void start() {
        try {

            server = HttpServer.create(new InetSocketAddress(9095), 0);

            server.createContext("/api/recommendation", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {

                    if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                        exchange.sendResponseHeaders(405, -1);
                        return;
                    }

                    String response = getBestInvestment();

                    exchange.getResponseHeaders().set("Content-Type", "application/json");

                    byte[] bytes = response.getBytes();
                    exchange.sendResponseHeaders(200, bytes.length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                }
            });

            server.start();
            System.out.println("✅ API started on http://localhost:8080/api/recommendation");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getBestInvestment() {

        InvestmentService service = new InvestmentService();
        List<Investment> investments = service.getAll();

        if (investments.isEmpty()) {
            return "{\"message\":\"No investments found\"}";
        }

        Investment best = null;
        double bestScore = -1;

        // Normalisation valeur (BigDecimal → double)
        double minValue = investments.stream()
                .mapToDouble(inv -> inv.getEstimatedValue().doubleValue())
                .min().orElse(0);

        double maxValue = investments.stream()
                .mapToDouble(inv -> inv.getEstimatedValue().doubleValue())
                .max().orElse(1);

        for (Investment inv : investments) {

            double value = inv.getEstimatedValue().doubleValue();

            double normalizedValue =
                    (value - minValue) / (maxValue - minValue + 1);

            double riskScore = mapRiskToScore(inv.getRiskLevel());

            double finalScore =
                    (0.6 * normalizedValue) +
                            (0.4 * riskScore);

            if (finalScore > bestScore) {
                bestScore = finalScore;
                best = inv;
            }
        }

        return """
                {
                  "investment": "%s",
                  "category": "%s",
                  "estimatedValue": %.2f,
                  "riskLevel": "%s",
                  "finalScore": %.2f
                }
                """.formatted(
                best.getName(),
                best.getCategory(),
                best.getEstimatedValue().doubleValue(),
                best.getRiskLevel(),
                bestScore
        );
    }

    private static double mapRiskToScore(String risk) {

        if (risk == null) return 0.5;

        return switch (risk.toUpperCase()) {
            case "LOW" -> 1.0;
            case "MEDIUM" -> 0.6;
            case "HIGH" -> 0.2;
            default -> 0.5;
        };
    }
}