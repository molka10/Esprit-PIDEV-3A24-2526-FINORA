package com.example.project_pi.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Currency rates using exchangerate.host (no API key)
 * Docs: exchangerate.host/documentation
 */
public class CurrencyApiService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    // Very small JSON parsing (no extra dependency)
    // Response example: {"success":true,"base":"EUR","date":"...","rates":{"TND":3.39}}
    private static final Pattern RATE_PATTERN = Pattern.compile("\"([A-Z]{3})\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    public OptionalDouble getRate(String from, String to) throws IOException, InterruptedException {
        if (from == null || to == null) return OptionalDouble.empty();
        if (from.equalsIgnoreCase(to)) return OptionalDouble.of(1.0);

        String url = "https://api.exchangerate.host/latest?base="
                + enc(from.toUpperCase()) + "&symbols=" + enc(to.toUpperCase());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return OptionalDouble.empty();

        String body = res.body();
        Matcher m = RATE_PATTERN.matcher(body);

        // Find the "to" rate
        while (m.find()) {
            String code = m.group(1);
            if (code.equalsIgnoreCase(to)) {
                return OptionalDouble.of(Double.parseDouble(m.group(2)));
            }
        }
        return OptionalDouble.empty();
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}