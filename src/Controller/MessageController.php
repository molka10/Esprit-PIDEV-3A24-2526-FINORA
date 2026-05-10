<?php

namespace App\Controller;

use App\Entity\Message;
use App\Entity\User;
use App\Service\NotificationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class MessageController extends AbstractController
{
    #[Route('/messages', name: 'app_messages')]
    public function index(EntityManagerInterface $em): Response
    {
        /** @var User $currentUser */
        $currentUser = $this->getUser();
        if (!$currentUser) return $this->redirectToRoute('app_login');

        $users = $em->getRepository(User::class)->findAll();
        $contacts = [];

        foreach ($users as $user) {
            if ($user->getId() === $currentUser->getId()) continue;

            // Get last message
            $lastMsg = $em->getRepository(Message::class)->createQueryBuilder('m')
                ->where('(m.sender = :user AND m.receiver = :contact) OR (m.sender = :contact AND m.receiver = :user)')
                ->setParameter('user', $currentUser)
                ->setParameter('contact', $user)
                ->orderBy('m.createdAt', 'DESC')
                ->setMaxResults(1)
                ->getQuery()
                ->getOneOrNullResult();

            $contacts[] = [
                'user' => $user,
                'lastMessage' => $lastMsg ? $lastMsg->getContent() : 'Aucun message',
                'lastTime' => $lastMsg ? $lastMsg->getCreatedAt()->format('H:i') : ''
            ];
        }
        
        return $this->render('message/index.html.twig', [
            'contacts' => $contacts
        ]);
    }

    #[Route('/messages/conversation/{id}', name: 'app_messages_conversation')]
    public function conversation(User $contact, EntityManagerInterface $em): JsonResponse
    {
        /** @var User $user */
        $user = $this->getUser();
        
        $messages = $em->getRepository(Message::class)->createQueryBuilder('m')
            ->where('(m.sender = :user AND m.receiver = :contact) OR (m.sender = :contact AND m.receiver = :user)')
            ->setParameter('user', $user)
            ->setParameter('contact', $contact)
            ->orderBy('m.createdAt', 'ASC')
            ->getQuery()
            ->getResult();

        $data = array_map(fn(Message $m) => [
            'id' => $m->getId(),
            'content' => $m->getContent(),
            'senderId' => $m->getSender()->getId(),
            'createdAt' => $m->getCreatedAt()->format('H:i'),
            'isMine' => $m->getSender() === $user
        ], $messages);

        return new JsonResponse($data);
    }

    #[Route('/messages/send', name: 'app_messages_send', methods: ['POST'])]
    public function send(Request $request, EntityManagerInterface $em, NotificationService $notifService): JsonResponse
    {
        /** @var User $user */
        $user = $this->getUser();
        $data = json_decode($request->getContent(), true);
        
        $receiverId = $data['receiverId'] ?? null;
        $content = $data['content'] ?? null;

        if (!$receiverId || !$content) {
            return new JsonResponse(['success' => false], 400);
        }

        $receiver = $em->getRepository(User::class)->find($receiverId);
        if (!$receiver) return new JsonResponse(['success' => false], 404);

        $message = new Message();
        $message->setSender($user);
        $message->setReceiver($receiver);
        $message->setContent($content);

        try {
            $em->persist($message);
            $em->flush();

            // Notify receiver
            $notifService->send(
                $receiver,
                'message',
                'Nouveau message',
                sprintf('Vous avez reçu un message de %s.', $user->getUsername())
            );
        } catch (\Exception $e) {
            return new JsonResponse(['success' => false, 'message' => 'Erreur : ' . $e->getMessage()], 500);
        }

        return new JsonResponse(['success' => true]);
    }
}
