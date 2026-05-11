<?php

namespace App\Service;

use App\Entity\Lesson;
use App\Entity\Flashcard;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class FlashcardGeneratorService
{
    private const GROQ_URL = 'https://api.groq.com/openai/v1/chat/completions';
    private const MODEL = 'llama-3.1-8b-instant';

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $groqApiKey,
        private readonly EntityManagerInterface $entityManager
    ) {
    }

    /**
     * @return Flashcard[]
     */
    public function generateForLesson(Lesson $lesson): array
    {
        $content = $lesson->getContenu();
        $title = $lesson->getTitre();

        $prompt = <<<PROMPT
Tu es un expert en pédagogie. Génère 5 flashcards (Question/Réponse) basées sur le contenu de la leçon suivante.
RÈGLES:
- Réponds UNIQUEMENT en JSON valide.
- Format: [{"q": "Question", "a": "Réponse"}]
- Les questions doivent être concises.
- Les réponses doivent être claires et courtes.

TITRE: {$title}
CONTENU: {$content}
PROMPT;

        $response = $this->client->request('POST', self::GROQ_URL, [
            'headers' => [
                'Authorization' => 'Bearer ' . $this->groqApiKey,
                'Content-Type' => 'application/json',
            ],
            'json' => [
                'model' => self::MODEL,
                'messages' => [
                    ['role' => 'user', 'content' => $prompt]
                ],
                'temperature' => 0.5,
            ]
        ]);

        $data = $response->toArray();
        $jsonStr = $data['choices'][0]['message']['content'] ?? '[]';
        
        // Extract JSON array using regex in case the model adds conversational text
        if (preg_match('/\[\s*\{.*?\}\s*\]/s', $jsonStr, $matches)) {
            $jsonStr = $matches[0];
        } else {
            // Fallback: clean standard markdown blocks
            $jsonStr = preg_replace('/^```json\s*|```$/m', '', $jsonStr);
        }
        
        $cards = json_decode($jsonStr, true);

        if (!is_array($cards)) {
            // Error handling or logging could go here
            return [];
        }

        $flashcards = [];
        foreach ($cards as $card) {
            if (!isset($card['q'], $card['a'])) continue;

            $flashcard = new Flashcard();
            $flashcard->setLesson($lesson);
            $flashcard->setQuestion($card['q']);
            $flashcard->setAnswer($card['a']);
            
            $this->entityManager->persist($flashcard);
            $flashcards[] = $flashcard;
        }

        $this->entityManager->flush();
        return $flashcards;
    }
}
