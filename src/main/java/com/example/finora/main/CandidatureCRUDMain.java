package com.example.finora.main;

import com.example.finora.entities.Candidature;
import com.example.finora.services.CandidatureService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CandidatureCRUDMain {

    public static void main(String[] args) {

        CandidatureService service = new CandidatureService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== CRUD CANDIDATURE =====");
            System.out.println("1) Ajouter candidature");
            System.out.println("2) Afficher toutes les candidatures");
            System.out.println("3) Afficher candidatures d'un appel d'offre (par appel_offre_id)");
            System.out.println("4) Modifier candidature");
            System.out.println("5) Supprimer candidature");
            System.out.println("0) Quitter");
            System.out.print("Choix: ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> {
                        Candidature c = saisir(sc);
                        service.add(c);
                        System.out.println("✅ Candidature ajoutée");
                    }
                    case "2" -> {
                        List<Candidature> list = service.getAll();
                        System.out.println("\n--- LISTE CANDIDATURES ---");
                        for (Candidature c : list) System.out.println(c);
                    }
                    case "3" -> {
                        System.out.print("appel_offre_id: ");
                        int id = Integer.parseInt(sc.nextLine());
                        List<Candidature> list = service.getByAppelOffreId(id);
                        System.out.println("\n--- CANDIDATURES DE L'APPEL D'OFFRE " + id + " ---");
                        for (Candidature c : list) System.out.println(c);
                    }
                    case "4" -> {
                        System.out.print("candidature_id à modifier: ");
                        int candId = Integer.parseInt(sc.nextLine());
                        Candidature c = saisir(sc);
                        c.setCandidatureId(candId);
                        service.update(c);
                        System.out.println("✅ Candidature modifiée");
                    }
                    case "5" -> {
                        System.out.print("candidature_id à supprimer: ");
                        int candId = Integer.parseInt(sc.nextLine());
                        service.delete(candId);
                        System.out.println("✅ Candidature supprimée");
                    }
                    case "0" -> {
                        System.out.println("Bye 👋");
                        return;
                    }
                    default -> System.out.println("❌ Choix invalide");
                }
            } catch (SQLException e) {
                System.out.println("❌ SQL Error: " + e.getMessage());
                System.out.println("💡 Astuce: appel_offre_id doit exister (FK). Ajoute d'abord un appel d'offre !");
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static Candidature saisir(Scanner sc) {

        System.out.print("appel_offre_id (doit exister): ");
        int appelOffreId = Integer.parseInt(sc.nextLine());

        System.out.print("Nom candidat: ");
        String nom = sc.nextLine();

        System.out.print("Email candidat: ");
        String email = sc.nextLine();

        System.out.print("Montant proposé: ");
        double montant = Double.parseDouble(sc.nextLine());

        System.out.print("Message: ");
        String message = sc.nextLine();

        System.out.print("Statut (submitted/accepted/rejected...): ");
        String statut = sc.nextLine();

        return new Candidature(appelOffreId, nom, email, montant, message, statut);
    }

}
