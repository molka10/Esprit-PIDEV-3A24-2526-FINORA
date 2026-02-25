package com.example.crud.services;

import com.example.crud.entities.Bourse;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ServiceBourse
 *
 * @TestMethodOrder : Permet d'exécuter les tests dans un ordre précis
 * @Order : Définit l'ordre d'exécution de chaque test
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceBourseTest {

    // ========================================
    // ATTRIBUTS STATIQUES
    // ========================================

    /**
     * Service à tester (partagé entre tous les tests)
     * static = une seule instance pour tous les tests
     */
    static ServiceBourse service;

    /**
     * ID de la bourse de test (sera utilisé dans plusieurs tests)
     */
    static int idBourseTest;

    // ========================================
    // SETUP - Exécuté UNE FOIS avant tous les tests
    // ========================================

    /**
     * Cette méthode s'exécute UNE SEULE FOIS avant tous les tests
     * Elle initialise le service
     */
    @BeforeAll
    static void setup() {
        System.out.println("==========================================");
        System.out.println("   DÉBUT DES TESTS - SERVICE BOURSE");
        System.out.println("==========================================\n");

        // Créer l'instance du service
        service = new ServiceBourse();

        System.out.println("✅ Service initialisé\n");
    }

    // ========================================
    // TEST 1 : AJOUTER UNE BOURSE (CREATE)
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Test 1 : Ajouter une bourse")
    void testAjouterBourse() {
        System.out.println("--- TEST 1 : AJOUT D'UNE BOURSE ---");

        // GIVEN (Étant donné) - Préparer les données
        Bourse bourseTest = new Bourse("TEST_BOURSE", "Test Country", "TST", "ACTIVE");
        int nbAvant = service.getAll().size();
        System.out.println("Nombre de bourses AVANT : " + nbAvant);

        // WHEN (Quand) - Exécuter l'action
        service.add(bourseTest);

        // THEN (Alors) - Vérifier les résultats
        int nbApres = service.getAll().size();
        System.out.println("Nombre de bourses APRÈS : " + nbApres);

        assertEquals(nbAvant + 1, nbApres, "Le nombre de bourses doit augmenter de 1");

        // Récupérer l'ID de la bourse ajoutée (pour les tests suivants)
        List<Bourse> bourses = service.getAll();
        for (Bourse b : bourses) {
            if ("TEST_BOURSE".equals(b.getNomBourse())) {
                idBourseTest = b.getIdBourse();
                System.out.println("ID de la bourse de test : " + idBourseTest);
                break;
            }
        }

        System.out.println("✅ Test 1 RÉUSSI\n");
    }

    // ========================================
    // TEST 2 : RÉCUPÉRER TOUTES LES BOURSES (READ ALL)
    // ========================================

    @Test
    @Order(2)
    @DisplayName("Test 2 : Récupérer toutes les bourses")
    void testGetAll() {
        System.out.println("--- TEST 2 : RÉCUPÉRATION DE TOUTES LES BOURSES ---");

        // WHEN
        List<Bourse> bourses = service.getAll();

        // THEN
        assertNotNull(bourses, "La liste ne doit pas être null");
        assertTrue(bourses.size() > 0, "La liste doit contenir au moins une bourse");

        System.out.println("Nombre de bourses trouvées : " + bourses.size());

        // Afficher toutes les bourses
        for (Bourse b : bourses) {
            System.out.println("  - " + b.getNomBourse() + " (" + b.getPays() + ")");
        }

        System.out.println("✅ Test 2 RÉUSSI\n");
    }

    // ========================================
    // TEST 3 : RÉCUPÉRER UNE BOURSE PAR ID (READ BY ID)
    // ========================================

    @Test
    @Order(3)
    @DisplayName("Test 3 : Récupérer une bourse par ID")
    void testGetById() {
        System.out.println("--- TEST 3 : RÉCUPÉRATION PAR ID ---");

        // GIVEN
        System.out.println("Recherche de la bourse avec ID : " + idBourseTest);

        // WHEN
        Bourse bourse = service.getById(idBourseTest);

        // THEN
        assertNotNull(bourse, "La bourse ne doit pas être null");
        assertEquals(idBourseTest, bourse.getIdBourse(), "L'ID doit correspondre");
        assertEquals("TEST_BOURSE", bourse.getNomBourse(), "Le nom doit être TEST_BOURSE");

        System.out.println("Bourse trouvée : " + bourse.getNomBourse());
        System.out.println("✅ Test 3 RÉUSSI\n");
    }

    // ========================================
    // TEST 4 : MODIFIER UNE BOURSE (UPDATE)
    // ========================================

    @Test
    @Order(4)
    @DisplayName("Test 4 : Modifier une bourse")
    void testModifierBourse() {
        System.out.println("--- TEST 4 : MODIFICATION D'UNE BOURSE ---");

        // GIVEN
        Bourse bourse = service.getById(idBourseTest);
        assertNotNull(bourse, "La bourse doit exister");

        String ancienStatut = bourse.getStatut();
        System.out.println("Statut AVANT : " + ancienStatut);

        // Changer le statut
        String nouveauStatut = ancienStatut.equals("ACTIVE") ? "INACTIVE" : "ACTIVE";

        // WHEN
        bourse.setStatut(nouveauStatut);
        service.update(bourse);

        // THEN
        Bourse bourseModifiee = service.getById(idBourseTest);
        assertNotNull(bourseModifiee, "La bourse modifiée doit exister");
        assertEquals(nouveauStatut, bourseModifiee.getStatut(), "Le statut doit être modifié");

        System.out.println("Statut APRÈS : " + bourseModifiee.getStatut());

        // Remettre l'ancien statut
        bourse.setStatut(ancienStatut);
        service.update(bourse);

        System.out.println("✅ Test 4 RÉUSSI\n");
    }

    // ========================================
    // TEST 5 : RECHERCHER DES BOURSES (SEARCH)
    // ========================================

    @Test
    @Order(5)
    @DisplayName("Test 5 : Rechercher des bourses")
    void testRechercher() {
        System.out.println("--- TEST 5 : RECHERCHE DE BOURSES ---");

        // WHEN
        List<Bourse> resultats = service.searchByName("TEST");

        // THEN
        assertNotNull(resultats, "Les résultats ne doivent pas être null");
        assertTrue(resultats.size() > 0, "Doit trouver au moins TEST_BOURSE");

        System.out.println("Résultats de la recherche 'TEST' : " + resultats.size());

        // Vérifier que TEST_BOURSE est dans les résultats
        boolean trouve = false;
        for (Bourse b : resultats) {
            System.out.println("  - " + b.getNomBourse());
            if ("TEST_BOURSE".equals(b.getNomBourse())) {
                trouve = true;
            }
        }

        assertTrue(trouve, "TEST_BOURSE doit être dans les résultats");

        System.out.println("✅ Test 5 RÉUSSI\n");
    }

    // ========================================
    // TEST 6 : SUPPRIMER UNE BOURSE (DELETE)
    // ========================================

    @Test
    @Order(6)
    @DisplayName("Test 6 : Supprimer une bourse")
    void testSupprimerBourse() {
        System.out.println("--- TEST 6 : SUPPRESSION D'UNE BOURSE ---");

        // GIVEN
        int nbAvant = service.getAll().size();
        System.out.println("Nombre de bourses AVANT suppression : " + nbAvant);

        Bourse bourseASupprimer = service.getById(idBourseTest);
        assertNotNull(bourseASupprimer, "La bourse à supprimer doit exister");

        // WHEN
        service.delete(bourseASupprimer);

        // THEN
        int nbApres = service.getAll().size();
        System.out.println("Nombre de bourses APRÈS suppression : " + nbApres);

        assertEquals(nbAvant - 1, nbApres, "Le nombre doit diminuer de 1");

        // Vérifier que la bourse n'existe plus
        Bourse bourseSupprimee = service.getById(idBourseTest);
        assertNull(bourseSupprimee, "La bourse ne doit plus exister");

        System.out.println("✅ Test 6 RÉUSSI\n");
    }

    // ========================================
    // CLEANUP - Exécuté UNE FOIS après tous les tests
    // ========================================

    @AfterAll
    static void cleanup() {
        System.out.println("==========================================");
        System.out.println("   FIN DES TESTS - SERVICE BOURSE");
        System.out.println("==========================================");
    }
}