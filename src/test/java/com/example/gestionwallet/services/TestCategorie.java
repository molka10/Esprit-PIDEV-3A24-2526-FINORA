package com.example.gestionwallet.services;

import com.example.gestionwallet.models.categorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCategorie {

    private servicecategorie service;

    @BeforeEach
    void setUp() {
        service = new servicecategorie();
    }

    @Test
    void testAjouterCategorie() {

        categorie c = new categorie("FoodTest", "HAUTE", "INCOME", "USER");
        service.ajouter(c);

        List<categorie> list = service.afficher();

        boolean exists = list.stream()
                .anyMatch(cat -> cat.getNom().equals("FoodTest"));

        assertTrue(exists);
    }

    @Test
    void testSupprimerCategorie() {

        categorie c = new categorie("TempDelete", "BASSE", "OUTCOME", "USER");
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