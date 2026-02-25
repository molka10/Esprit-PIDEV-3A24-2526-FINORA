package com.example.finora.main;

import com.example.finora.entities.AppelOffre;
import com.example.finora.services.AppelOffreService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class AppelOffreCRUDMain {

    public static void main(String[] args) {

        AppelOffreService service = new AppelOffreService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== CRUD APPEL D'OFFRE =====");
            System.out.println("1) Ajouter appel d'offre");
            System.out.println("2) Afficher appels d'offre");
            System.out.println("3) Modifier appel d'offre");
            System.out.println("4) Supprimer appel d'offre");
            System.out.println("0) Quitter");
            System.out.print("Choix: ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> {
                        AppelOffre a = saisir(sc);
                        service.add(a);
                        System.out.println("✅ Ajout réussi");
                    }
                    case "2" -> {
                        List<AppelOffre> list = service.getAll();
                        System.out.println("\n--- LISTE ---");
                        for (AppelOffre a : list) System.out.println(a);
                    }
                    case "3" -> {
                        System.out.print("ID à modifier (appel_offre_id): ");
                        int id = Integer.parseInt(sc.nextLine());
                        AppelOffre a = saisir(sc);
                        a.setAppelOffreId(id);
                        service.update(a);
                        System.out.println("✅ Modification réussie");
                    }
                    case "4" -> {
                        System.out.print("ID à supprimer (appel_offre_id): ");
                        int id = Integer.parseInt(sc.nextLine());
                        service.delete(id);
                        System.out.println("✅ Suppression réussie");
                    }
                    case "0" -> {
                        System.out.println("Bye 👋");
                        return;
                    }
                    default -> System.out.println("❌ Choix invalide");
                }
            } catch (SQLException e) {
                System.out.println("❌ SQL Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static AppelOffre saisir(Scanner sc) {
        System.out.print("Titre: ");
        String titre = sc.nextLine();

        System.out.print("Description: ");
        String description = sc.nextLine();

        System.out.print("Catégorie: ");
        String categorie = sc.nextLine();

        System.out.print("Type (achat', 'partenariat', 'donnant_donnant', 'don'...): ");
        String type = sc.nextLine();

        System.out.print("Budget min: ");
        double budgetMin = Double.parseDouble(sc.nextLine());

        System.out.print("Budget max: ");
        double budgetMax = Double.parseDouble(sc.nextLine());

        System.out.print("Devise (TND/EUR...): ");
        String devise = sc.nextLine();

        System.out.print("Date limite (YYYY-MM-DD): ");
        LocalDate dateLimite = LocalDate.parse(sc.nextLine());

        System.out.print("Statut (published/closed...): ");
        String statut = sc.nextLine();

        return new AppelOffre(titre, description, categorie, type, budgetMin, budgetMax, devise, dateLimite, statut);
    }
}
