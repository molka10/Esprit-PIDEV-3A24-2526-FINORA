package com.example.gestionwallet.test;

import com.example.gestionwallet.models.categorie;
import com.example.gestionwallet.services.servicecategorie;

public class testcategorie {

    public static void main(String[] args) {

        servicecategorie sc = new servicecategorie();

        // ADD
        categorie c1 = new categorie("Transport", "MOYENNE", "OUTCOME");
        categorie c2 = new categorie("Salary", "HAUTE", "INCOME");

        sc.ajouter(c1);
        sc.ajouter(c2);

        // UPDATE
        categorie c3 = new categorie(1, "Groceries", "HAUTE", "OUTCOME");
        sc.modifier(c3);

        // READ
        sc.afficher().forEach(System.out::println);


        // DELETE
        sc.supprimer(9);
    }
}
