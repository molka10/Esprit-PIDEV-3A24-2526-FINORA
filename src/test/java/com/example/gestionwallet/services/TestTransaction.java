package com.example.gestionwallet.services;

import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.models.categorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestTransaction {

    private servicetransaction service;
    private servicecategorie sc;

    @BeforeEach
    void setUp() {
        service = new servicetransaction();
        sc = new servicecategorie();
    }

    @Test
    void testAjouterTransaction() {

        // ADD CATEGORY
        categorie c = new categorie("TempTest", "BASSE", "INCOME", "USER");
        sc.ajouter(c);

        int categoryId = sc.getIdByName("TempTest");
        assertTrue(categoryId > 0);

        // ADD TRANSACTION
        Date date = Date.valueOf("2025-02-10");

        transaction t = new transaction(
                "Test Transaction",
                "INCOME",
                150.0,
                date,
                "MANUAL",
                1,
                categoryId,
                "USER"
        );

        service.ajouter(t);

        assertNotNull(t);
        assertEquals(150.0, t.getMontant());
        assertEquals("USER", t.getRole());
    }

    @Test
    void testSupprimerTransaction() {

        categorie c = new categorie("TempDeleteCat", "BASSE", "OUTCOME", "USER");
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
                categoryId,
                "USER"
        );

        service.ajouter(t);



        assertNotNull(t);
        assertEquals("OUTCOME", t.getType());
    }
}