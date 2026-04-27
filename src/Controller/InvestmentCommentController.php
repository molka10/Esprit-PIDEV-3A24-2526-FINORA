<?php

namespace App\Controller;

use App\Entity\Investment;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/investment')]
class InvestmentCommentController extends AbstractController
{
    #[Route('/{id}/comment', name: 'app_investment_add_comment', methods: ['POST'])]
    #[IsGranted('ROLE_USER')]
    public function addComment(
        Investment $investment,
        Request $request,
        EntityManagerInterface $entityManager,
        \App\Repository\InvestmentManagementRepository $mgmtRepo
    ): Response {
        $content = $request->request->get('content');
        $user = $this->getUser();

        if ($content && strlen(trim($content)) >= 3) {
            // Check if user is an investor in this specific project
            $isInvestor = $mgmtRepo->findOneBy(['user' => $user, 'investment' => $investment]) !== null;

            $investment->addCommentJson([
                'username' => $user->getUserIdentifier(),
                'content' => htmlspecialchars($content),
                'userId' => $user->getId(),
                'isInvestor' => $isInvestor,
                'userRole' => in_array('ROLE_ADMIN', $user->getRoles()) ? 'ADMIN' : 'USER',
            ]);

            $entityManager->flush();
            $this->addFlash('success', 'Votre avis a été publié.');
        } else {
            $this->addFlash('error', 'Le commentaire est trop court.');
        }

        return $this->redirectToRoute('app_investment_show', ['id' => $investment->getId()]);
    }
}
