package com.example.project_pi.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Currency rates using ExchangeRate-API open endpoint (no key):
 * https://open.er-api.com/v6/latest/{BASE}
 */
public class CurrencyApiService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    // Finds: "TND":2.87152 inside "rates" object
    private static final Pattern RATE_PATTERN =
            Pattern.compile("\"([A-Z]{3})\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    public OptionalDouble getRate(String from, String to) throws IOException, InterruptedException {
        if (from == null || to == null) return OptionalDouble.empty();

        String base = from.trim().toUpperCase();
        String target = to.trim().toUpperCase();

        if (base.isEmpty() || target.isEmpty()) return OptionalDouble.empty();
        if (base.equals(target)) return OptionalDouble.of(1.0);

        // Open endpoint (no key)
        String url = "https://open.er-api.com/v6/latest/" + base;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return OptionalDouble.empty();

        String body = res.body();
        if (body == null || body.isBlank()) return OptionalDouble.empty();

        // Quick check for success
        if (!body.contains("\"result\":\"success\"")) return OptionalDouble.empty();

        Matcher m = RATE_PATTERN.matcher(body);
        while (m.find()) {
            String code = m.group(1);
            if (code.equals(target)) {
                return OptionalDouble.of(Double.parseDouble(m.group(2)));
            }
        }
        return OptionalDouble.empty();
    }
}