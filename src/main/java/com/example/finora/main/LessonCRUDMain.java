package com.example.finora.main;

import com.example.finora.entities.Lesson;
import com.example.finora.services.LessonService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class LessonCRUDMain {

    public static void main(String[] args) {

        LessonService service = new LessonService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== GESTION LESSON =====");
            System.out.println("1) Ajouter lesson");
            System.out.println("2) Afficher toutes les lessons");
            System.out.println("3) Afficher lessons d'une formation");
            System.out.println("4) Modifier lesson");
            System.out.println("5) Supprimer lesson");
            System.out.println("0) Quitter");
            System.out.print("Choix: ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> {
                        Lesson l = saisirLesson(sc);
                        service.add(l);
                        System.out.println("✅ Lesson ajoutée");
                    }
                    case "2" -> {
                        List<Lesson> list = service.getAll();
                        System.out.println("\n--- LISTE DES LESSONS ---");
                        for (Lesson l : list) System.out.println(l);
                    }
                    case "3" -> {
                        System.out.print("Formation ID: ");
                        int fid = Integer.parseInt(sc.nextLine());
                        List<Lesson> list = service.getByFormation(fid);
                        System.out.println("\n--- LESSONS DE LA FORMATION " + fid + " ---");
                        for (Lesson l : list) System.out.println(l);
                    }
                    case "4" -> {
                        System.out.print("Lesson ID à modifier: ");
                        int id = Integer.parseInt(sc.nextLine());
                        Lesson l = saisirLesson(sc);
                        l.setId(id);
                        service.update(l);
                        System.out.println("✅ Lesson modifiée");
                    }
                    case "5" -> {
                        System.out.print("Lesson ID à supprimer: ");
                        int id = Integer.parseInt(sc.nextLine());
                        service.delete(id);
                        System.out.println("✅ Lesson supprimée");
                    }
                    case "0" -> {
                        System.out.println("Bye 👋");
                        return;
                    }
                    default -> System.out.println("❌ Choix invalide");
                }
            } catch (SQLException e) {
                System.out.println("❌ SQL Error: " + e.getMessage());
                System.out.println("💡 Astuce: formation_id doit exister (FK). Ajoute d'abord une formation !");
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static Lesson saisirLesson(Scanner sc) {
        System.out.print("Formation ID (doit exister): ");
        int formationId = Integer.parseInt(sc.nextLine());

        System.out.print("Titre: ");
        String titre = sc.nextLine();

        System.out.print("Contenu: ");
        String contenu = sc.nextLine();

        System.out.print("Ordre: ");
        int ordre = Integer.parseInt(sc.nextLine());

        System.out.print("Durée (minutes): ");
        int duree = Integer.parseInt(sc.nextLine());

        return new Lesson(formationId, titre, contenu, ordre, duree);
    }
}
