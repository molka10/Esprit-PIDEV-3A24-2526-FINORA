<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class FaceIdService
{
    private $apiKey;
    
    // ✅ Same model + task that works in the Java app
    private const MODEL_URL = "https://router.huggingface.co/hf-inference/models/google/vit-base-patch16-224/pipeline/image-feature-extraction";

    private $client;

    public function __construct(HttpClientInterface $client)
    {
        $this->client = $client;
        $this->apiKey = $_ENV['HUGGING_FACE_API_KEY'] ?? '';
    }

    /**
     * Accepts a base64 encoded image string (data:image/jpeg;base64,...)
     * or raw base64 string.
     */
    public function getFaceEmbedding(string $base64Image): ?array
    {
        // Ensure it has the data URI prefix as sent to HF in the Java code
        if (!str_starts_with($base64Image, 'data:')) {
            $base64Image = "data:image/jpeg;base64," . $base64Image;
        }

        try {
            $response = $this->client->request('POST', self::MODEL_URL, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                    'X-Wait-For-Model' => 'true'
                ],
                'json' => [
                    'inputs' => $base64Image
                ],
                // Timeout is important just in case HF is cold starting.
                'timeout' => 30
            ]);

            if ($response->getStatusCode() === 200) {
                $content = $response->getContent();
                $data = json_decode($content, true);

                if (is_array($data)) {
                    // Try to flatten the nested array structure to 1D array
                    // if it is [[float, float, ...]]
                    if (!empty($data) && is_array($data[0])) {
                        if (!empty($data[0]) && is_array($data[0][0])) {
                            // [[[float]]]
                            return $data[0][0];
                        }
                        // [[float]]
                        return $data[0];
                    }
                    
                    // flat array
                    return $data;
                }
            }
            
            return null;

        } catch (\Exception $e) {
            error_log("FaceIdService Error: " . $e->getMessage());
            return null;
        }
    }

    /**
     * Cosine similarity of two vectors.
     */
    public function calculateSimilarity(array $vectorA, array $vectorB): float
    {
        if (count($vectorA) !== count($vectorB)) {
            return 0.0;
        }

        $dot = 0.0;
        $normA = 0.0;
        $normB = 0.0;

        for ($i = 0; $i < count($vectorA); $i++) {
            $dot += $vectorA[$i] * $vectorB[$i];
            $normA += $vectorA[$i] * $vectorA[$i];
            $normB += $vectorB[$i] * $vectorB[$i];
        }

        $norm = sqrt($normA) * sqrt($normB);

        return $norm <= 0 ? 0.0 : $dot / $norm;
    }

    public function isSamePerson(array $vectorA, array $vectorB): bool
    {
        // Target similarity threshold = 0.5 (as per Java FaceIdService)
        return $this->calculateSimilarity($vectorA, $vectorB) >= 0.5;
    }
}
