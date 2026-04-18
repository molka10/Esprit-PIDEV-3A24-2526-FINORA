<?php

namespace App\Service;

use App\Entity\Lesson;
use App\Entity\User;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Address;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

class LessonNotificationService
{
    private MailerInterface $mailer;
    private UrlGeneratorInterface $urlGenerator;

    public function __construct(MailerInterface $mailer, UrlGeneratorInterface $urlGenerator)
    {
        $this->mailer = $mailer;
        $this->urlGenerator = $urlGenerator;
    }

    public function notifyNewLesson(Lesson $lesson): void
    {
        $formation = $lesson->getFormation();
        if (!$formation) {
            return;
        }

        $purchasedUsers = $formation->getPurchasedBy();
        
        foreach ($purchasedUsers as $user) {
            if (!$user instanceof User || !$user->getEmail()) {
                continue;
            }

            // Generate absolute URL to the lesson
            $lessonUrl = $this->urlGenerator->generate('app_lesson_show', [
                'id' => $lesson->getId()
            ], UrlGeneratorInterface::ABSOLUTE_URL);

            $email = (new TemplatedEmail())
                ->from(new Address('no-reply@finora.com', 'FINORA Education'))
                ->to($user->getEmail())
                ->subject('🚀 Nouvelle Leçon Disponible : ' . $lesson->getTitre())
                ->htmlTemplate('emails/new_lesson.html.twig')
                ->context([
                    'user' => $user,
                    'lesson' => $lesson,
                    'formation' => $formation,
                    'lessonUrl' => $lessonUrl,
                ]);

            $this->mailer->send($email);
        }
    }
}
