package com.example.gestionwallet.test;

import com.example.gestionwallet.models.categorie;
import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.services.servicecategorie;
import com.example.gestionwallet.services.servicetransaction;

import java.sql.Date;
import java.util.List;

public class testtransaction {

    public static void main(String[] args) {

        servicecategorie sc = new servicecategorie();
        servicetransaction st = new servicetransaction();

        // ADD CATEGORY
        categorie cat = new categorie("TestCategory", "HAUTE", "INCOME", "USER");
        sc.ajouter(cat);

        List<categorie> categories = sc.afficher();
        int lastCategoryId = categories.get(categories.size() - 1).getId_category();

        // ADD TRANSACTION
        transaction t1 = new transaction(
                "Freelance Work",
                "INCOME",
                200.0,
                Date.valueOf("2026-02-12"),
                "MANUAL",
                1,
                lastCategoryId,
                "USER"
        );

        st.ajouter(t1);

        System.out.println(" LISTE TRANSACTIONS ");
        st.afficher().forEach(System.out::println);

        // UPDATE
        transaction t2 = new transaction(
                1,
                "Freelance Updated",
                "INCOME",
                250.0,
                Date.valueOf("2026-02-13"),
                "MANUAL",
                1,
                lastCategoryId,
                "USER"
        );

        st.modifier(t2);

        System.out.println(" APRES UPDATE ");
        st.afficher().forEach(System.out::println);
    }
}