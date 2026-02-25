package com.example.finora.services;

import com.example.finora.entities.categorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCategorie {

    private servicecategorie service;

    @BeforeEach
    void setUp() {
        service = new servicecategorie();
    }

    @AfterEach
    void cleanUp() {

        List<categorie> list = service.afficher();

        for (categorie c : list) {
            if (c.getNom().contains("Test") ||
                    c.getNom().contains("Temp")) {

                service.supprimer(c.getId_category());
            }
        }
    }

    @Test
    void testAjouterCategorie() {

        categorie c = new categorie("FoodTest", "HAUTE", "INCOME");
        service.ajouter(c);

        List<categorie> list = service.afficher();

        boolean exists = list.stream()
                .anyMatch(cat -> cat.getNom().equals("FoodTest"));

        assertTrue(exists);
    }

    @Test
    void testSupprimerCategorie() {

        categorie c = new categorie("TempDelete", "BASSE", "OUTCOME");
        service.ajouter(c);

        int id = service.getIdByName("TempDelete");
        assertTrue(id > 0);

        service.supprimer(id);

        List<categorie> list = service.afficher();

        boolean exists = list.stream()
                .anyMatch(cat -> cat.getNom().equals("TempDelete"));

        assertFalse(exists);
    }
}