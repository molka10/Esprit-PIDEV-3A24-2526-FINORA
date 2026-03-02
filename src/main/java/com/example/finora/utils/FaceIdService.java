package com.example.finora.utils;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class FaceIdService {

    // ✅ Same key that works in your Symfony app
    private static final String API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    // ✅ Same model + task that works in your Symfony app
    private static final String MODEL_URL =
            "https://router.huggingface.co/hf-inference/models/google/vit-base-patch16-224/pipeline/image-feature-extraction";

    private final HttpClient httpClient;
    private final Gson gson;

    public FaceIdService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public double[] getFaceEmbedding(byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length < 100) {
                System.err.println("HF API Error: invalid image bytes, length="
                        + (imageBytes == null ? "null" : imageBytes.length));
                return null;
            }

            // ✅ Convert to base64 data URL — exactly like the PHP does
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String dataUrl = "data:image/jpeg;base64," + base64;

            // ✅ Send as JSON body with "inputs" key — exactly like the PHP does
            String jsonBody = "{\"inputs\":\"" + dataUrl + "\"}";

            System.out.println("Sending JSON to HF API, image size: " + imageBytes.length + " bytes");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MODEL_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")  // ✅ JSON not image/jpeg
                    .header("X-Wait-For-Model", "true")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("HF API status: " + response.statusCode());

            String preview = response.body().substring(0, Math.min(300, response.body().length()));
            System.out.println("HF API raw response: " + preview);

            if (response.statusCode() == 200) {
                String body = response.body().trim();

              // Shape: [[float, float, ...]] — ViT default, flatten to first row
if (body.startsWith("[[")) {
    // Could be [[[ ]]] (3 deep) or [[ ]] (2 deep) — handle both
    try {
        double[][][] deep = gson.fromJson(body, double[][][].class);
        if (deep != null && deep.length > 0 && deep[0].length > 0) return deep[0][0];
        return null;
    } catch (Exception e1) {
        try {
            double[][] nested = gson.fromJson(body, double[][].class);
            if (nested != null && nested.length > 0) return nested[0];
            return null;
        } catch (Exception e2) {
            System.err.println("HF parse failed: " + e2.getMessage());
            return null;
        }
    }
}
                // Shape: [[[float, ...]]] — 3 levels deep, flatten twice
                if (body.startsWith("[[[")) {
                    double[][][] deep = gson.fromJson(body, double[][][].class);
                    if (deep != null && deep.length > 0 && deep[0].length > 0) return deep[0][0];
                    return null;
                }

                // Shape: [float, float, ...] — flat array
                if (body.startsWith("[") && !body.startsWith("[{")) {
                    return gson.fromJson(body, double[].class);
                }

                System.err.println("HF API Error: unexpected response format. Body=" + preview);
                return null;

            } else {
                System.err.println("HF API Error: " + response.statusCode() + " - " + response.body());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cosine similarity — same logic as PHP FaceVerificationService.
     * Returns value between -1 and 1. Above 0.5 = same person (matches PHP threshold).
     */
    public double calculateSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < vectorA.length; i++) {
            dot   += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        double norm = Math.sqrt(normA) * Math.sqrt(normB);
        return norm <= 0 ? 0.0 : dot / norm;
    }

    /**
     * Same threshold as PHP FaceVerificationService (COSINE_THRESHOLD = 0.5)
     */
    public boolean isSamePerson(double[] vectorA, double[] vectorB) {
        return calculateSimilarity(vectorA, vectorB) >= 0.5;
    }
}