<?php

namespace App\Controller;

use App\Service\AiAssistantService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/marketplace')]
class AiAssistantController extends AbstractController
{
    private function checkAccess(Request $request)
    {
        $role = $request->getSession()->get('role');
        if (!in_array($role, ['admin', 'investisseur', 'user'])) {
            return $this->redirectToRoute('choose_role');
        }
        return null;
    }

    #[Route('/assistant', name: 'app_marketplace_assistant', methods: ['GET'])]
    public function index(Request $request): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        return $this->render('marketplace/assistant.html.twig', [
            'controller_name' => 'AiAssistantController',
        ]);
    }

    #[Route('/api/assistant/chat', name: 'api_marketplace_assistant_chat', methods: ['POST'])]
    public function chat(Request $request, AiAssistantService $aiService): JsonResponse
    {
        if ($redirect = $this->checkAccess($request)) {
            return new JsonResponse(['error' => 'Non autorisé'], Response::HTTP_UNAUTHORIZED);
        }

        $data = json_decode($request->getContent(), true);
        $message = $data['message'] ?? '';

        if (empty(trim($message))) {
            return new JsonResponse(['error' => 'Message vide'], Response::HTTP_BAD_REQUEST);
        }

        $botResponse = $aiService->processUserMessage($message);

        return new JsonResponse($botResponse);
    }
}
