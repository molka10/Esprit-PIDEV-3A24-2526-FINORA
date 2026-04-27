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
    private $lastError;

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

    public function getLastError(): ?string
    {
        return $this->lastError;
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
        // Nettoyage du numéro (suppression des espaces, tirets, etc.)
        $to = preg_replace('/[^0-9+]/', '', $to);
        
        if (!str_starts_with($to, '+')) {
            if (str_starts_with($to, '00')) {
                $to = '+' . substr($to, 2);
            } else {
                $to = '+216' . ltrim($to, '0');
            }
        }
        
        $this->logger->info("Tentative d'envoi SMS Twilio à $to : $message");

        try {
            $response = $this->httpClient->request('POST', "https://api.twilio.com/2010-04-01/Accounts/{$this->twilioSid}/Messages.json", [
                'auth_basic' => [$this->twilioSid, $this->twilioToken],
                'verify_peer' => false, // Désactivé pour compatibilité environnements locaux
                'body' => [
                    'To' => $to,
                    'From' => trim($this->twilioFrom),
                    'Body' => $message,
                ],
            ]);

            $statusCode = $response->getStatusCode();
            if ($statusCode >= 200 && $statusCode < 300) {
                return true;
            }

            $content = $response->toArray(false);
            $errorDetail = $content['message'] ?? ($content['more_info'] ?? 'Détails inconnus');
            $this->lastError = $errorDetail;
            $this->logger->error("Erreur Twilio ({$statusCode}) pour {$to} : {$errorDetail}");
            return false;

        } catch (\Exception $e) {
            $this->lastError = $e->getMessage();
            $this->logger->error("Exception grave lors de l'envoi SMS à {$to} : " . $e->getMessage());
            return false;
        }
    }
}
