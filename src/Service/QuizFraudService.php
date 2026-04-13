<?php

namespace App\Service;

class QuizFraudService
{
    /**
     * @param int $focusLossCount
     * @param int $exitFullscreenCount
     * @param int $fastAnswers
     * @return array{
     *     isFraud: bool,
     *     fraudScore: int,
     *     details: array<string, mixed>
     * }
     */
    public function analyze(int $focusLossCount, int $exitFullscreenCount, int $fastAnswers): array
    {
        $fraudScore = 0;
        
        $fraudScore += $focusLossCount * 30;
        $fraudScore += $exitFullscreenCount * 25;
        $fraudScore += $fastAnswers * 10;
        
        $isFraud = false;
        
        if ($exitFullscreenCount >= 1) {
            $isFraud = true;
        } elseif ($focusLossCount >= 3) {
            $isFraud = true;
        } elseif ($fastAnswers >= 3) {
            $isFraud = true;
        } elseif ($fraudScore >= 60) {
            $isFraud = true;
        }
        
        return [
            'isFraud' => $isFraud,
            'fraudScore' => $fraudScore,
            'details' => [
                'focusLossCount' => $focusLossCount,
                'exitFullscreenCount' => $exitFullscreenCount,
                'fastAnswers' => $fastAnswers
            ]
        ];
    }
}
