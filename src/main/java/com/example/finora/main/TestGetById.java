package com.example.finora.main;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;

import java.util.Scanner;

public class TestGetById {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {

            UserService service = new UserService();

            System.out.println("===== AFFICHER UTILISATEUR PAR ID =====");
            System.out.print("Entrer l'ID de l'utilisateur: ");

            int id = Integer.parseInt(sc.nextLine().trim());

            User u = service.getUserById(id);

            if (u != null) {
                System.out.println("\n✅ Utilisateur trouvé:");
                System.out.println("ID        : " + u.getId());
                System.out.println("Username  : " + u.getUsername());
                System.out.println("Email     : " + u.getEmail());
                System.out.println("Role      : " + u.getRole());
            } else {
                System.out.println("⚠️ Aucun utilisateur trouvé avec l'ID " + id);
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur:");
            e.printStackTrace();
        }
    }
}
