package com.example.finora.main;

import com.example.finora.entities.Investment;
import com.example.finora.services.InvestmentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class TestInvestmentCRUD {

    public static void main(String[] args) {

        InvestmentService service = new InvestmentService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== MENU INVESTMENT CRUD =====");
            System.out.println("1) Add Investment");
            System.out.println("2) List Investments");
            System.out.println("3) Update Investment");
            System.out.println("4) Delete Investment");
            System.out.println("0) Exit");
            System.out.print("Choix: ");

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {

                case 1 -> {
                    Investment inv = new Investment();

                    System.out.print("Name: ");
                    inv.setName(sc.nextLine());

                    System.out.print("Category: ");
                    inv.setCategory(sc.nextLine());

                    System.out.print("Location: ");
                    inv.setLocation(sc.nextLine());

                    System.out.print("Estimated Value (ex: 15000.50): ");
                    inv.setEstimatedValue(new BigDecimal(sc.nextLine()));

                    System.out.print("Risk Level (LOW/MEDIUM/HIGH): ");
                    inv.setRiskLevel(sc.nextLine());

                    System.out.print("Image URL (optional): ");
                    inv.setImageUrl(sc.nextLine());

                    System.out.print("Description: ");
                    inv.setDescription(sc.nextLine());

                    service.add(inv);
                }

                case 2 -> {
                    List<Investment> list = service.getAll();
                    System.out.println("\n--- LIST INVESTMENTS ---");
                    for (Investment inv : list) {
                        System.out.println(inv);
                    }
                }

                case 3 -> {
                    Investment inv = new Investment();

                    System.out.print("ID to update: ");
                    inv.setInvestmentId(Integer.parseInt(sc.nextLine()));

                    System.out.print("New Name: ");
                    inv.setName(sc.nextLine());

                    System.out.print("New Category: ");
                    inv.setCategory(sc.nextLine());

                    System.out.print("New Location: ");
                    inv.setLocation(sc.nextLine());

                    System.out.print("New Estimated Value: ");
                    inv.setEstimatedValue(new BigDecimal(sc.nextLine()));

                    System.out.print("New Risk Level: ");
                    inv.setRiskLevel(sc.nextLine());

                    System.out.print("New Image URL: ");
                    inv.setImageUrl(sc.nextLine());

                    System.out.print("New Description: ");
                    inv.setDescription(sc.nextLine());

                    service.update(inv);
                }

                case 4 -> {
                    System.out.print("ID to delete: ");
                    int id = Integer.parseInt(sc.nextLine());
                    service.delete(id);
                }

                case 0 -> {
                    System.out.println("Bye 👋");
                    return;
                }

                default -> System.out.println("❌ Choix invalide !");
            }
        }
    }
}
