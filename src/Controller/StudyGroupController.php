<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\StudyGroup;
use App\Entity\StudyGroupMessage;
use App\Entity\User;
use App\Repository\StudyGroupRepository;
use App\Repository\StudyGroupMessageRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

class StudyGroupController extends AbstractController
{
    #[Route('/api/study-group/formation/{id}', name: 'api_study_group_get', methods: ['GET'])]
    public function getGroup(Formation $formation, StudyGroupRepository $repo, EntityManagerInterface $em): JsonResponse
    {
        $group = $repo->findOneBy(['formation' => $formation]);

        if (!$group) {
            $group = new StudyGroup();
            $group->setFormation($formation);
            $group->setName('Groupe d\'étude: ' . $formation->getTitre());
            $group->setDescription('Espace de discussion pour la formation ' . $formation->getTitre());
            
            // Assign to first admin found or current user
            $user = $this->getUser();
            if ($user instanceof User) {
                $group->setCreatedBy($user);
            } else {
                return $this->json(['error' => 'Auth required'], 401);
            }

            $em->persist($group);
            $em->flush();
        }

        return $this->json([
            'id' => $group->getId(),
            'name' => $group->getName(),
            'description' => $group->getDescription()
        ]);
    }

    #[Route('/api/study-group/{id}/messages', name: 'api_study_group_messages', methods: ['GET'])]
    public function getMessages(StudyGroup $group, StudyGroupMessageRepository $repo): JsonResponse
    {
        $messages = $repo->findRecentByGroup($group->getId());

        $data = array_map(fn($m) => [
            'id' => $m->getId(),
            'user' => $m->getUser()->getUsername(),
            'userId' => $m->getUser()->getId(),
            'content' => $m->getContent(),
            'at' => $m->getCreatedAt()->format('H:i')
        ], $messages);

        return $this->json($data);
    }

    #[Route('/api/study-group/{id}/send', name: 'api_study_group_send', methods: ['POST'])]
    public function sendMessage(StudyGroup $group, Request $request, EntityManagerInterface $em): JsonResponse
    {
        $user = $this->getUser();
        if (!$user instanceof User) {
            return $this->json(['error' => 'Auth required'], 401);
        }

        $data = json_decode($request->getContent(), true);
        $content = $data['content'] ?? '';

        if (empty($content)) {
            return $this->json(['error' => 'Empty message'], 400);
        }

        $message = new StudyGroupMessage();
        $message->setStudyGroup($group);
        $message->setUser($user);
        $message->setContent($content);

        $em->persist($message);
        $em->flush();

        return $this->json([
            'status' => 'ok',
            'message' => [
                'user' => $user->getUsername(),
                'content' => $content,
                'at' => $message->getCreatedAt()->format('H:i')
            ]
        ]);
    }
}
