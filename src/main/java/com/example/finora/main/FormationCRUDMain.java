package com.example.finora.main;

import com.example.finora.entities.Formation;
import com.example.finora.services.FormationService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class FormationCRUDMain {

    public static void main(String[] args) {

        FormationService service = new FormationService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== GESTION FORMATION =====");
            System.out.println("1) Ajouter formation");
            System.out.println("2) Afficher formations");
            System.out.println("3) Modifier formation");
            System.out.println("4) Supprimer formation");
            System.out.println("0) Quitter");
            System.out.print("Choix: ");

            String choice = sc.nextLine();

            try {
                switch (choice) {
                    case "1" -> {
                        Formation f = saisirFormation(sc);
                        service.add(f);
                        System.out.println("✅ Formation ajoutée");
                    }
                    case "2" -> {
                        List<Formation> list = service.getAll();
                        System.out.println("\n--- LISTE DES FORMATIONS ---");
                        for (Formation f : list) {
                            System.out.println(f);
                        }
                    }
                    case "3" -> {
                        System.out.print("ID à modifier: ");
                        int id = Integer.parseInt(sc.nextLine());
                        Formation f = saisirFormation(sc);
                        f.setId(id);
                        service.update(f);
                        System.out.println("✅ Formation modifiée");
                    }
                    case "4" -> {
                        System.out.print("ID à supprimer: ");
                        int id = Integer.parseInt(sc.nextLine());
                        service.delete(id);
                        System.out.println("✅ Formation supprimée");
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

    private static Formation saisirFormation(Scanner sc) {
        System.out.print("Titre: ");
        String titre = sc.nextLine();

        System.out.print("Description: ");
        String description = sc.nextLine();

        System.out.print("Catégorie: ");
        String categorie = sc.nextLine();

        System.out.print("Niveau (debutant/intermediaire/avance): ");
        String niveau = sc.nextLine();

        System.out.print("Publié? (true/false): ");
        boolean published = Boolean.parseBoolean(sc.nextLine());

        System.out.print("Image URL: ");
        String imageUrl = sc.nextLine();

        return new Formation(titre, description, categorie, niveau, published, imageUrl);
    }
}
