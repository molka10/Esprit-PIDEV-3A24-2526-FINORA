package com.example.finora.main;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;

import java.util.Scanner;

public class TestUpdate {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {

            UserService service = new UserService();

            System.out.println("===== TEST MODIFICATION UTILISATEUR =====");

            System.out.print("Entrer l'ID de l'utilisateur à modifier: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            // Optional: check if user exists before updating
            User existing = service.getUserById(id);
            if (existing == null) {
                System.out.println("⚠️ Aucun utilisateur trouvé avec l'ID " + id);
                return;
            }

            System.out.println("\nUtilisateur actuel:");
            System.out.println("Username: " + existing.getUsername());
            System.out.println("Email   : " + existing.getEmail());
            System.out.println("Role    : " + existing.getRole());

            System.out.println("\n--- Nouveaux champs ---");
            System.out.print("Nouveau username: ");
            String username = sc.nextLine().trim();

            System.out.print("Nouveau email: ");
            String email = sc.nextLine().trim();

            System.out.print("Nouveau role (ADMIN/ENTREPRISE/USER): ");
            String role = sc.nextLine().trim().toUpperCase();

            User u = new User(username, email, "", role);
            u.setId(id);

            boolean updated = service.updateUser(u);

            if (updated) {
                System.out.println("\n✅ Modification réussie.");
                User after = service.getUserById(id);
                System.out.println("Après modification: " + after);
            } else {
                System.out.println("\n⚠️ Aucune modification (ID non trouvé).");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la modification:");
            e.printStackTrace();
        }
    }
}
