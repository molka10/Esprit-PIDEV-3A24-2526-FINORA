package com.example.finora.services;

import com.example.finora.entities.transaction;
import com.example.finora.entities.categorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTransaction {

    private servicetransaction service;
    private servicecategorie sc;

    @BeforeEach
    void setUp() {
        service = new servicetransaction();
        sc = new servicecategorie();
    }

    @AfterEach
    void cleanUp() {

        // delete test transactions
        List<transaction> list = service.afficher();

        for (transaction t : list) {
            if (t.getNom_transaction().contains("Test")) {
                service.supprimer(t.getId_transaction());
            }
        }

        // delete test categories
        List<categorie> cats = sc.afficher();

        for (categorie c : cats) {
            if (c.getNom().contains("Temp")) {
                sc.supprimer(c.getId_category());
            }
        }
    }

    @Test
    void testAjouterTransaction() {

        categorie c = new categorie("TempTest", "BASSE", "INCOME");
        sc.ajouter(c);

        int categoryId = sc.getIdByName("TempTest");
        assertTrue(categoryId > 0);

        Date date = Date.valueOf("2025-02-10");

        transaction t = new transaction(
                "Test Transaction",
                "INCOME",
                150.0,
                date,
                "MANUAL",
                1,          // user_id
                categoryId
        );

        service.ajouter(t);

        List<transaction> list = service.afficher();

        boolean exists = list.stream()
                .anyMatch(tr -> tr.getNom_transaction().equals("Test Transaction"));

        assertTrue(exists);
    }

    @Test
    void testSupprimerTransaction() {

        categorie c = new categorie("TempDeleteCat", "BASSE", "OUTCOME");
        sc.ajouter(c);

        int categoryId = sc.getIdByName("TempDeleteCat");

        Date date = Date.valueOf("2025-02-10");

        transaction t = new transaction(
                "TestDelete",
                "OUTCOME",
                -50.0,
                date,
                "MANUAL",
                1,
                categoryId
        );

        service.ajouter(t);

        List<transaction> list = service.afficher();

        transaction added = list.stream()
                .filter(tr -> tr.getNom_transaction().equals("TestDelete"))
                .findFirst()
                .orElse(null);

        assertNotNull(added);

        service.supprimer(added.getId_transaction());

        List<transaction> afterDelete = service.afficher();

        boolean exists = afterDelete.stream()
                .anyMatch(tr -> tr.getNom_transaction().equals("TestDelete"));

        assertFalse(exists);
    }
}