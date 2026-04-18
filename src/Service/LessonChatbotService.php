<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class LessonChatbotService
{
    private const GROQ_URL = 'https://api.groq.com/openai/v1/chat/completions';
    private const MODEL = 'llama-3.1-8b-instant';

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $groqApiKey
    ) {
    }

    /**
     * Send a chat message to Groq with lesson context.
     *
     * @param string $lessonTitle   Title of the lesson
     * @param string $lessonContent Full text content of the lesson
     * @param array  $history       Previous messages [['role'=>'user|assistant','content'=>'...']]
     * @param string $userMessage   The new user message
     * @param string $lang          Preferred response language code (fr, en, ar, etc.)
     *
     * @return string The assistant's reply
     */
    public function chat(string $lessonTitle, string $lessonContent, array $history, string $userMessage, string $lang = 'fr'): string
    {
        // Truncate content to avoid token limits (keep first ~3000 chars)
        $truncatedContent = mb_strlen($lessonContent) > 3000
            ? mb_substr($lessonContent, 0, 3000) . "\n\n[... contenu tronqué ...]"
            : $lessonContent;

        // Build messages array
        $messages = [];

        // Language labels for the system prompt
        $langNames = [
            'fr' => 'French', 'en' => 'English', 'ar' => 'Arabic',
            'es' => 'Spanish', 'de' => 'German', 'it' => 'Italian',
            'zh' => 'Chinese', 'tr' => 'Turkish', 'pt' => 'Portuguese',
            'ru' => 'Russian', 'ja' => 'Japanese',
        ];
        $langLabel = $langNames[$lang] ?? 'French';

        // System prompt with lesson context
        $messages[] = [
            'role' => 'system',
            'content' => <<<SYSTEM
You are "Finora Assistant", a smart, friendly AI tutor embedded in the Finora learning platform.
You are currently in the lesson titled: "{$lessonTitle}".

Here is the full content of this lesson:
---
{$truncatedContent}
---

CRITICAL LANGUAGE RULE:
- The user's preferred language is: {$langLabel} ({$lang}).
- You MUST reply ENTIRELY in {$langLabel}.
- If the user writes in a different language than their preference, still reply in {$langLabel}.
- For Arabic, write in Modern Standard Arabic (فصحى).

Your responsibilities:
- Answer the student's questions about this lesson.
- Summarize the content if asked.
- Explain difficult concepts simply.
- Provide practical examples related to the lesson topic.
- Suggest review questions if asked.
- Be concise, clear, and encouraging.
- If the question is unrelated to the lesson, politely say you specialize in this lesson's content.
SYSTEM
        ];

        // Add conversation history (limit to last 10 messages to save tokens)
        $recentHistory = array_slice($history, -10);
        foreach ($recentHistory as $msg) {
            if (isset($msg['role'], $msg['content'])) {
                $messages[] = [
                    'role' => $msg['role'] === 'assistant' ? 'assistant' : 'user',
                    'content' => $msg['content'],
                ];
            }
        }

        // Add the new user message
        $messages[] = [
            'role' => 'user',
            'content' => $userMessage,
        ];

        $body = [
            'model' => self::MODEL,
            'temperature' => 0.6,
            'max_tokens' => 1024,
            'messages' => $messages,
        ];

        try {
            $response = $this->client->request('POST', self::GROQ_URL, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->groqApiKey,
                    'Content-Type' => 'application/json',
                    'Accept' => 'application/json',
                ],
                'json' => $body,
                'timeout' => 30,
            ]);

            $decoded = json_decode($response->getContent(), true);

            if (isset($decoded['choices'][0]['message']['content'])) {
                return trim($decoded['choices'][0]['message']['content']);
            }

            return "Désolé, je n'ai pas pu générer une réponse. Veuillez réessayer.";
        } catch (\Throwable $e) {
            return "Erreur de connexion au service IA. Veuillez réessayer dans quelques instants.";
        }
    }
}
