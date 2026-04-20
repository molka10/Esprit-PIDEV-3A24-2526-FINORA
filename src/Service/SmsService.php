<?php

namespace App\Service;

use Symfony\Component\HttpClient\HttpClient;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Psr\Log\LoggerInterface;

class SmsService
{
    private $httpClient;
    private $logger;
    private $twilioSid;
    private $twilioToken;
    private $twilioFrom;

    public function __construct(
        HttpClientInterface $httpClient, 
        LoggerInterface $logger,
        string $twilioSid,
        string $twilioToken,
        string $twilioFrom
    ) {
        $this->httpClient = $httpClient;
        $this->logger = $logger;
        $this->twilioSid = $twilioSid;
        $this->twilioToken = $twilioToken;
        $this->twilioFrom = $twilioFrom;
    }

    /**
     * Envoie un SMS via Twilio
     * 
     * @param string $to Numéro de téléphone (ex: +21622111333)
     * @param string $message Contenu du message
     * @return bool Succès de l'envoi
     */
    public function sendSms(string $to, string $message): bool
    {
        // Nettoyage et formatage du numéro (Twilio nécessite souvent le format E.164, ex: +216...)
        if (!str_starts_with($to, '+')) {
            // Si le numéro commence par 00, on remplace par +
            if (str_starts_with($to, '00')) {
                $to = '+' . substr($to, 2);
            } else {
                // Par défaut on assume un numéro tunisien si pas de préfixe (ajuster si besoin)
                $to = '+216' . ltrim($to, '0');
            }
        }
        
        $this->logger->info("Tentative d'envoi SMS Twilio à $to : $message");

        try {
            $response = $this->httpClient->request('POST', "https://api.twilio.com/2010-04-01/Accounts/{$this->twilioSid}/Messages.json", [
                'auth_basic' => [$this->twilioSid, $this->twilioToken],
                'body' => [
                    'To' => $to,
                    'From' => $this->twilioFrom,
                    'Body' => $message,
                ],
            ]);

            $statusCode = $response->getStatusCode();
            if ($statusCode >= 200 && $statusCode < 300) {
                return true;
            }

            $content = $response->toArray(false);
            $errorDetail = $content['message'] ?? ($content['more_info'] ?? 'Détails inconnus');
            $this->logger->error("Erreur Twilio ({$statusCode}) pour {$to} : {$errorDetail}");
            return false;

        } catch (\Exception $e) {
            $this->logger->error("Exception grave lors de l'envoi SMS à {$to} : " . $e->getMessage());
            return false;
        }
    }
}
