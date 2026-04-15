<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use App\Form\FormationType;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\String\Slugger\SluggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

final class FormationController extends AbstractController
{
    #[Route('/formation', name: 'formation_index')]
    public function index(Request $request, EntityManagerInterface $entityManager, PaginatorInterface $paginator): Response
    {
        $titre = trim((string) $request->query->get('titre', ''));
        $categorie = trim((string) $request->query->get('categorie', ''));
        $niveau = trim((string) $request->query->get('niveau', ''));
        $tri = (string) $request->query->get('tri', 'id');
        $ordre = strtolower((string) $request->query->get('ordre', 'desc'));

        $qb = $entityManager->getRepository(Formation::class)->createQueryBuilder('f');

        if ($titre !== '') {
            $qb->andWhere('LOWER(f.titre) LIKE LOWER(:titre)')
                ->setParameter('titre', '%' . $titre . '%');
        }

        if ($categorie !== '') {
            $qb->andWhere('LOWER(f.categorie) LIKE LOWER(:categorie)')
                ->setParameter('categorie', '%' . $categorie . '%');
        }

        if ($niveau !== '') {
            $qb->andWhere('f.niveau = :niveau')
                ->setParameter('niveau', $niveau);
        }

        $allowedSortFields = ['id', 'titre', 'categorie', 'niveau'];
        if (!in_array($tri, $allowedSortFields, true)) {
            $tri = 'id';
        }

        $ordre = $ordre === 'asc' ? 'ASC' : 'DESC';
        $qb->orderBy('f.' . $tri, $ordre);

        $query = $qb->getQuery();

        $page = $request->query->getInt('page', 1);

        $formations = $paginator->paginate(
            $query,
            $page,
            5
        );

        $categories = [
            'Bourse',
            'Investissement',
            'Trading',
            'Actions',
            'ETF',
            'Obligations',
            'Crypto',
            'Forex',
            'Analyse technique',
            'Analyse fondamentale',
            'Gestion des risques',
            'Portefeuille',
            'Dividendes',
            'Marchés financiers',
            'Psychologie du trader',
            'Économie',
            'Inflation',
            'Taux d’intérêt',
            'Fiscalité',
        ];

        $lessonCounts = [];
        foreach ($formations as $formation) {
            $lessonCounts[$formation->getId()] = $entityManager->getRepository(Lesson::class)
                ->createQueryBuilder('l')
                ->select('COUNT(l.id)')
                ->leftJoin('l.formation', 'f')
                ->andWhere('f.id = :formationId')
                ->setParameter('formationId', $formation->getId())
                ->getQuery()
                ->getSingleScalarResult();
        }

        return $this->render('formation/index.html.twig', [
            'formations' => $formations,
            'titre' => $titre,
            'categorie' => $categorie,
            'niveau' => $niveau,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
            'categories' => $categories,
            'lessonCounts' => $lessonCounts,
        ]);
    }

    #[Route('/formation/new', name: 'formation_new')]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $formation = new Formation();

        $form = $this->createForm(FormationType::class, $formation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            
            // Check if user selected an image online
            $remoteImageFilename = $request->request->get('remoteImageFilename');
            if ($remoteImageFilename) {
                // By manually setting imageUrl, VichUploader will ignore imageFile 
                // and it will just save the filename we downloaded into db
                $formation->setImageUrl($remoteImageFilename);
            }

            $entityManager->persist($formation);
            $entityManager->flush();

            return $this->redirectToRoute('formation_index');
        }

        return $this->render('formation/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/formation/{id}/edit', name: 'formation_edit')]
    public function edit(int $id, Request $request, EntityManagerInterface $entityManager): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        $form = $this->createForm(FormationType::class, $formation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            
            // Check if user selected an image online
            $remoteImageFilename = $request->request->get('remoteImageFilename');
            if ($remoteImageFilename) {
                $formation->setImageUrl($remoteImageFilename);
            }

            $entityManager->flush();

            return $this->redirectToRoute('formation_index');
        }

        return $this->render('formation/edit.html.twig', [
            'form' => $form->createView(),
            'formation' => $formation,
        ]);
    }

    #[Route('/formation/{id}', name: 'formation_delete', methods: ['POST'])]
    public function delete(int $id, Request $request, EntityManagerInterface $entityManager): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        if ($this->isCsrfTokenValid('delete' . $formation->getId(), $request->request->get('_token'))) {
            $entityManager->remove($formation);
            $entityManager->flush();
        }

        return $this->redirectToRoute('formation_index');
    }

    #[Route('/admin/formation/api/search-images', name: 'api_formation_search_images', methods: ['POST'])]
    public function searchImages(Request $request, HttpClientInterface $httpClient): JsonResponse
    {
        $query = $request->request->get('query', 'finance');
        $apiKey = $this->getParameter('kernel.environment') === 'test' ? 'test' : ($_ENV['PEXELS_API_KEY'] ?? $_SERVER['PEXELS_API_KEY'] ?? null);

        if (!$apiKey) {
            return new JsonResponse(['error' => 'La clé API Pexels n\'est pas configurée.'], 500);
        }

        try {
            $response = $httpClient->request('GET', 'https://api.pexels.com/v1/search', [
                'query' => [
                    'query' => $query,
                    'per_page' => 20,
                    'orientation' => 'landscape'
                ],
                'headers' => [
                    'Authorization' => $apiKey
                ]
            ]);

            $data = $response->toArray();
            return new JsonResponse($data);
        } catch (\Exception $e) {
            return new JsonResponse(['error' => 'Erreur lors de la recherche des images.'], 500);
        }
    }

    #[Route('/admin/formation/api/download-image', name: 'api_formation_download_image', methods: ['POST'])]
    public function downloadImage(Request $request, HttpClientInterface $httpClient, SluggerInterface $slugger): JsonResponse
    {
        $imageUrl = $request->request->get('url');

        if (!$imageUrl) {
            return new JsonResponse(['error' => 'L\'URL de l\'image est manquante.'], 400);
        }

        try {
            // Download the image bytes
            $response = $httpClient->request('GET', $imageUrl);
            $content = $response->getContent();

            // Generate a secure filename
            $extension = 'jpg'; // Pexels usually serves jpg, or we could parse MIME. Jpg is safe.
            $filename = 'pexels-' . uniqid() . '.' . $extension;
            
            // The vich uploader destination directory is defined in vich_uploader.yaml usually.
            // Typically it maps to public/uploads/formations
            $targetDir = $this->getParameter('kernel.project_dir') . '/public/uploads/formations';
            
            if (!is_dir($targetDir)) {
                mkdir($targetDir, 0777, true);
            }

            file_put_contents($targetDir . '/' . $filename, $content);

            return new JsonResponse([
                'success' => true,
                'filename' => $filename,
                'url' => '/uploads/formations/' . $filename // for preview purposes
            ]);

        } catch (\Exception $e) {
            return new JsonResponse(['error' => 'Erreur lors du téléchargement de l\'image.'], 500);
        }
    }
}