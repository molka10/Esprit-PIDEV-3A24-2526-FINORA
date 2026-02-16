package com.example.finora_user.main;

import com.example.finora_user.services.UserService;

import java.util.Scanner;

public class TestDelete {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {

            UserService service = new UserService();

            System.out.println("===== TEST SUPPRESSION UTILISATEUR =====");

            System.out.print("Entrer l'ID de l'utilisateur à supprimer: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            boolean deleted = service.deleteUser(id);

            if (deleted) {
                System.out.println("✅ Utilisateur avec ID " + id + " supprimé avec succès.");
            } else {
                System.out.println("⚠️ Aucun utilisateur trouvé avec l'ID " + id + ".");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la suppression:");
            e.printStackTrace();
        }
    }
}
