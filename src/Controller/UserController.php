<?php

namespace App\Controller;

use App\Entity\User;
use App\Form\UserType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[IsGranted('ROLE_ADMIN')]
#[Route('/admin/user')]
final class UserController extends AbstractController
{
    #[Route('', name: 'admin_user_index', methods: ['GET'])]
    public function index(Request $request, EntityManagerInterface $entityManager, \App\Service\RiskScoringService $riskScoringService): Response
    {
        $search = $request->query->get('search');
        $page = $request->query->getInt('page', 1);
        $limit = 5;

        $qb = $entityManager->getRepository(User::class)->createQueryBuilder('u');

        if ($search) {
            $qb->where('u.username LIKE :search OR u.email LIKE :search')
               ->setParameter('search', '%' . $search . '%');
        }

        $qb->setFirstResult(($page - 1) * $limit)
           ->setMaxResults($limit);

        $users = $qb->getQuery()->getResult();

        $riskScores = [];
        foreach ($users as $u) {
            $riskScores[$u->getId()] = $riskScoringService->compute($u);
        }

        $countQb = $entityManager->getRepository(User::class)->createQueryBuilder('u')
            ->select('COUNT(u.id)');

        if ($search) {
            $countQb->where('u.username LIKE :search OR u.email LIKE :search')
                    ->setParameter('search', '%' . $search . '%');
        }

        $totalUsers = $countQb->getQuery()->getSingleScalarResult();
        $totalPages = ceil($totalUsers / $limit);

        if ($request->isXmlHttpRequest()) {
            return $this->render('admin/user/_table.html.twig', [
                'users' => $users,
                'riskScores' => $riskScores,
            ]);
        }

        return $this->render('admin/user/index.html.twig', [
            'users' => $users,
            'search' => $search,
            'currentPage' => $page,
            'totalPages' => $totalPages,
            'riskScores' => $riskScores,
        ]);
    }

    #[Route('/new', name: 'admin_user_new', methods: ['GET', 'POST'])]
    public function new(
        Request $request,
        EntityManagerInterface $entityManager,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
        $user = new User();
        $user->setCreatedAt(new \DateTime());

        $form = $this->createForm(UserType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $plainPassword = $form->get('password')->getData();

            if (empty($plainPassword)) {
                $this->addFlash('error', 'Password is required');

                return $this->render('admin/user/new.html.twig', [
                    'form' => $form->createView(),
                ]);
            }

            $user->setPassword(
                $passwordHasher->hashPassword($user, $plainPassword)
            );

            $imageFile = $form->get('image')->getData();

            if ($imageFile) {
                $newFilename = uniqid().'.'.$imageFile->guessExtension();

                $imageFile->move(
                    $this->getParameter('images_directory'),
                    $newFilename
                );

                $user->setImage($newFilename);
            }

            $entityManager->persist($user);
            $entityManager->flush();

            return $this->redirectToRoute('admin_user_index');
        }

        return $this->render('admin/user/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'admin_user_show', methods: ['GET'])]
    public function show(User $user, \App\Service\RiskScoringService $riskScoringService): Response
    {
        $risk = $riskScoringService->compute($user);

        return $this->render('admin/user/show.html.twig', [
            'user' => $user,
            'risk' => $risk,
        ]);
    }

    #[Route('/{id}/edit', name: 'admin_user_edit', methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        User $user,
        EntityManagerInterface $entityManager,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
        $oldImage = $user->getImage();

        $form = $this->createForm(UserType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $plainPassword = $form->get('password')->getData();

            if (!empty($plainPassword)) {
                $user->setPassword(
                    $passwordHasher->hashPassword($user, $plainPassword)
                );
            }

            $imageFile = $form->get('image')->getData();

            if ($imageFile) {
                $newFilename = uniqid().'.'.$imageFile->guessExtension();

                $imageFile->move(
                    $this->getParameter('images_directory'),
                    $newFilename
                );

                // 🔥 delete old image
                if ($oldImage && file_exists($this->getParameter('images_directory').'/'.$oldImage)) {
                    unlink($this->getParameter('images_directory').'/'.$oldImage);
                }

                $user->setImage($newFilename);
            }

            $entityManager->flush();

            return $this->redirectToRoute('admin_user_index');
        }

        return $this->render('admin/user/edit.html.twig', [
            'form' => $form->createView(),
            'user' => $user,
        ]);
    }

    #[Route('/{id}', name: 'admin_user_delete', methods: ['POST'])]
    public function delete(
        Request $request,
        User $user,
        EntityManagerInterface $entityManager
    ): Response {
        if ($this->isCsrfTokenValid('delete'.$user->getId(), $request->request->get('_token'))) {
            $entityManager->remove($user);
            $entityManager->flush();
        }

        return $this->redirectToRoute('admin_user_index');
    }
}