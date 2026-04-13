<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class GroqQuizService
{
    private const GROQ_URL = 'https://api.groq.com/openai/v1/chat/completions';
    private const MODEL = 'llama-3.1-8b-instant';
    private const MAX_TITLE_CHARS = 200;
    private const MAX_CONTENT_CHARS = 4000;

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $apiKey,
    ) {
    }

    /**
     * @return array<int, array{
     *     question: string,
     *     options: array<int, string>,
     *     correct: int
     * }>
     */
    public function generateQuiz(string $lessonTitle, string $lessonContent): array
    {
        $title = $this->safe($lessonTitle);
        $content = $this->safe($lessonContent);

        if (mb_strlen($title) > self::MAX_TITLE_CHARS) {
            $title = mb_substr($title, 0, self::MAX_TITLE_CHARS);
        }

        if (mb_strlen($content) > self::MAX_CONTENT_CHARS) {
            $content = mb_substr($content, 0, self::MAX_CONTENT_CHARS);
        }

        $prompt = <<<PROMPT
Tu es un professeur.
Génère EXACTEMENT 5 questions QCM (4 choix) basées sur la leçon ci-dessous.

RÈGLES STRICTES:
- Réponds UNIQUEMENT avec un JSON valide, sans texte avant/après.
- Format EXACT:
{
  "questions": [
    {
      "question": "...",
      "options": ["A","B","C","D"],
      "correct": 0
    }
  ]
}
- "correct" est l'index (0-3) de la bonne réponse.
- 5 questions obligatoires.

TITRE:
{$title}

CONTENU:
{$content}
PROMPT;

        $body = [
            'model' => self::MODEL,
            'temperature' => 0.2,
            'max_tokens' => 900,
            'messages' => [
                [
                    'role' => 'system',
                    'content' => 'Tu réponds uniquement en JSON valide.',
                ],
                [
                    'role' => 'user',
                    'content' => $prompt,
                ],
            ],
        ];

        $responseText = $this->sendWithRetries($body, 3);

        $decoded = json_decode($responseText, true);
        if (!is_array($decoded)
            || !isset($decoded['choices'][0]['message']['content'])
            || !is_string($decoded['choices'][0]['message']['content'])) {
            return $this->fallbackQuestions('Réponse IA invalide.');
        }

        return $this->parseCleanJson($decoded['choices'][0]['message']['content']);
    }

    private function sendWithRetries(array $body, int $maxAttempts): string
    {
        $lastException = null;

        for ($attempt = 1; $attempt <= $maxAttempts; $attempt++) {
            try {
                $response = $this->client->request('POST', self::GROQ_URL, [
                    'headers' => [
                        'Authorization' => 'Bearer ' . $this->apiKey,
                        'Content-Type' => 'application/json',
                        'Accept' => 'application/json',
                    ],
                    'json' => $body,
                    'timeout' => 45,
                ]);

                $statusCode = $response->getStatusCode();
                $content = $response->getContent(false);

                if ($statusCode >= 200 && $statusCode < 300) {
                    return $content;
                }

                if (in_array($statusCode, [500, 502, 503, 504], true)) {
                    $lastException = new \RuntimeException('Groq transient error (' . $statusCode . '): ' . $content);
                    usleep(700000 * $attempt);
                    continue;
                }

                throw new \RuntimeException('Erreur API (' . $statusCode . '): ' . $content);
            } catch (\Throwable $e) {
                $lastException = $e;
                usleep(700000 * $attempt);
            }
        }

        throw $lastException instanceof \Throwable
            ? $lastException
            : new \RuntimeException('Groq failed.');
    }

    /**
     * @return array<int, array{
     *     question: string,
     *     options: array<int, string>,
     *     correct: int
     * }>
     */
    private function parseCleanJson(string $text): array
    {
        $text = $this->safe($text);
        $text = str_replace(['```json', '```'], '', $text);
        $text = trim($text);

        try {
            $cleanJson = $this->extractJson($text);
        } catch (\Throwable) {
            return $this->fallbackQuestions('Quiz generation failed. Please try again.');
        }

        $quizJson = json_decode($cleanJson, true);
        if (!is_array($quizJson) || !isset($quizJson['questions']) || !is_array($quizJson['questions'])) {
            return $this->fallbackQuestions('AI returned invalid JSON. Try again.');
        }

        $questions = [];

        foreach ($quizJson['questions'] as $q) {
            if (!is_array($q)) {
                continue;
            }

            $questionText = isset($q['question']) && is_string($q['question'])
                ? $this->safe($q['question'])
                : 'Question invalide';

            $options = [];
            if (isset($q['options']) && is_array($q['options'])) {
                foreach ($q['options'] as $option) {
                    if (is_string($option)) {
                        $options[] = $this->safe($option);
                    }
                }
            }

            while (count($options) < 4) {
                $options[] = 'Option manquante';
            }

            if (count($options) > 4) {
                $options = array_slice($options, 0, 4);
            }

            $correct = 0;
            if (isset($q['correct']) && is_numeric($q['correct'])) {
                $correct = (int) $q['correct'];
            }

            if ($correct < 0 || $correct > 3) {
                $correct = 0;
            }

            $questions[] = [
                'question' => $questionText,
                'options' => $options,
                'correct' => $correct,
            ];
        }

        if (count($questions) > 5) {
            $questions = array_slice($questions, 0, 5);
        }

        while (count($questions) < 5) {
            $questions[] = [
                'question' => 'Question manquante (réessaie).',
                'options' => ['Option A', 'Option B', 'Option C', 'Option D'],
                'correct' => 0,
            ];
        }

        return $questions;
    }

    private function extractJson(string $text): string
    {
        $start = strpos($text, '{');
        $end = strrpos($text, '}');

        if ($start !== false && $end !== false && $end > $start) {
            return substr($text, $start, $end - $start + 1);
        }

        throw new \RuntimeException('JSON invalide reçu de l’IA');
    }

    /**
     * @return array<int, array{
     *     question: string,
     *     options: array<int, string>,
     *     correct: int
     * }>
     */
    private function fallbackQuestions(string $message): array
    {
        $questions = [];

        for ($i = 0; $i < 5; $i++) {
            $questions[] = [
                'question' => $i === 0 ? $message : 'Question manquante (réessaie).',
                'options' => ['Option A', 'Option B', 'Option C', 'Option D'],
                'correct' => 0,
            ];
        }

        return $questions;
    }

    private function safe(?string $value): string
    {
        return trim((string) $value);
    }
}