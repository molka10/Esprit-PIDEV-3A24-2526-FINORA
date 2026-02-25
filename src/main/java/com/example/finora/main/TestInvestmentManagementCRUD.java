package com.exaple.finora.main;

import com.example.finora.entities.InvestmentManagement;
import com.example.finora.services.InvestmentManagementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class TestInvestmentManagementCRUD {

    private static LocalDate parseDate(String input) {
        // accepte 2024-03-01 ET 2024-3-1
        DateTimeFormatter f1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter f2 = DateTimeFormatter.ofPattern("yyyy-M-d");

        try {
            return LocalDate.parse(input, f1);
        } catch (Exception ignored) {
            return LocalDate.parse(input, f2);
        }
    }

    public static void main(String[] args) {

        InvestmentManagementService service = new InvestmentManagementService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== MENU InvestmentManagement CRUD =====");
            System.out.println("1) Add");
            System.out.println("2) List");
            System.out.println("3) Update");
            System.out.println("4) Delete");
            System.out.println("0) Exit");
            System.out.print("Choix: ");

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {

                case 1 -> {
                    InvestmentManagement im = new InvestmentManagement();

                    System.out.print("investment_id (existant dans table investment): ");
                    im.setInvestmentId(Integer.parseInt(sc.nextLine()));

                    System.out.print("investment_type (ex: LONG_TERM): ");
                    im.setInvestmentType(sc.nextLine());

                    System.out.print("amount_invested (ex: 5000.50): ");
                    im.setAmountInvested(new BigDecimal(sc.nextLine()));

                    System.out.print("ownership_percentage (ex: 25.5) ou vide: ");
                    String own = sc.nextLine().trim();
                    if (own.isEmpty()) im.setOwnershipPercentage(null);
                    else im.setOwnershipPercentage(new BigDecimal(own));

                    System.out.print("start_date (YYYY-MM-DD ou YYYY-M-D): ");
                    im.setStartDate(parseDate(sc.nextLine()));

                    System.out.print("status (ACTIVE/CLOSED): ");
                    im.setStatus(sc.nextLine());

                    service.add(im);
                }

                case 2 -> {
                    List<InvestmentManagement> list = service.getAll();
                    System.out.println("\n--- LIST ---");
                    list.forEach(System.out::println);
                }

                case 3 -> {
                    InvestmentManagement im = new InvestmentManagement();

                    System.out.print("management_id à modifier: ");
                    im.setManagementId(Integer.parseInt(sc.nextLine()));

                    System.out.print("new investment_id: ");
                    im.setInvestmentId(Integer.parseInt(sc.nextLine()));

                    System.out.print("new investment_type: ");
                    im.setInvestmentType(sc.nextLine());

                    System.out.print("new amount_invested: ");
                    im.setAmountInvested(new BigDecimal(sc.nextLine()));

                    System.out.print("new ownership_percentage ou vide: ");
                    String own = sc.nextLine().trim();
                    if (own.isEmpty()) im.setOwnershipPercentage(null);
                    else im.setOwnershipPercentage(new BigDecimal(own));

                    System.out.print("new start_date (YYYY-MM-DD ou YYYY-M-D): ");
                    im.setStartDate(parseDate(sc.nextLine()));

                    System.out.print("new status: ");
                    im.setStatus(sc.nextLine());

                    service.update(im);
                }

                case 4 -> {
                    System.out.print("management_id à supprimer: ");
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
