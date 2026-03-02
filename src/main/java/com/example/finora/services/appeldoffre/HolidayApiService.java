package com.example.finora.services.appeldoffre;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Public holidays using Nager.Date (no API key)
 * Docs: https://date.nager.at/Api
 *
 * Endpoint:
 * GET https://date.nager.at/api/v3/PublicHolidays/{year}/{countryCode}
 */
public class HolidayApiService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    public boolean isPublicHoliday(LocalDate date, String countryCode) throws IOException, InterruptedException {
        if (date == null || countryCode == null || countryCode.isBlank()) return false;

        int year = date.getYear();
        String url = "https://date.nager.at/api/v3/PublicHolidays/" + year + "/" + countryCode.toUpperCase();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return false;

        // Response contains objects with "date":"YYYY-MM-DD"
        String target = "\"date\":\"" + date + "\"";
        return res.body() != null && res.body().contains(target);
    }

    public boolean isWeekend(LocalDate date) {
        if (date == null) return false;
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }
}