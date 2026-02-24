package com.example.crud.test;

import com.example.crud.models.Bourse;
import com.example.crud.services.ServiceBourse;

import java.util.List;

public class TestBourse {
    public static void main(String[] args) {
        ServiceBourse service = new ServiceBourse();

        System.out.println("==========================================");
        System.out.println("   TEST CRUD BOURSE");
        System.out.println("==========================================\n");

        // TEST ADD
        System.out.println("=== TEST AJOUT ===");
        Bourse nasdaq = new Bourse("NASDAQ", "USA", "USD", "ACTIVE");
        service.add(nasdaq);

        // TEST GET ALL
        System.out.println("\n=== TEST AFFICHAGE ===");
        List<Bourse> bourses = service.getAll();
        for (Bourse b : bourses) {
            System.out.println(b);
        }

        System.out.println("\n==========================================");
    }
}