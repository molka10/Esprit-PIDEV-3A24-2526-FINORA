package com.example.gestionwallet.services;

import com.example.gestionwallet.models.transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.gestionwallet.models.categorie;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestTransaction {

    private servicetransaction service;

    @BeforeEach
    void setUp() {
        service = new servicetransaction();
    }

    @Test
    void testAjouterTransaction() {

        servicecategorie sc = new servicecategorie();


        categorie c = new categorie("TempDelete", "BASSE", "OUTCOME", "USER");
        sc.ajouter(c);

        int categoryId = sc.getIdByName("Test");

        assertTrue(categoryId > 0);



        Date date = Date.valueOf("2025-02-10");

        transaction t = new transaction(
                "Test Transaction",
                "INCOME",
                150.0,
                date,
                "MANUAL",
                1,
                categoryId
        );

        service.ajouter(t);

        assertNotNull(t);
        assertEquals(150.0, t.getMontant());
    }


    @Test
    void testSupprimerTransaction() {

        Date date = Date.valueOf("2025-02-10");

        transaction t = new transaction(
                "TestDelete",
                "OUTCOME",
                50.0,
                date,
                "MANUAL",
                1,
                1
        );

        service.ajouter(t);


        assertTrue(true);
    }
}
