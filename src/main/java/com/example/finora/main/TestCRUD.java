package com.example.finora.main;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;

public class TestCRUD {

    public static void main(String[] args) {
        try {
            UserService service = new UserService();

            System.out.println("------ TEST CRUD FINORA ------");

            // 1) ADD
            System.out.println("\n1) ADD USERS");
            User u = new User("testuser", "testuser@mail.com", "Test1234", "USER");
            int newId = service.addUserReturnId(u);
            System.out.println("✅ Added user, id = " + newId);

            // 2) UPDATE
            System.out.println("\n2) UPDATE USERS");
            User updated = new User("testuser_updated", "testuser_updated@mail.com", "Test1234", "ENTREPRISE");
            updated.setId(newId); // IMPORTANT: set the id of the user to update
            service.updateUser(updated);
            System.out.println("✅ Updated user id = " + newId);

            // 3) DELETE
            System.out.println("\n3) DELETE USERS");
            service.deleteUser(newId);
            System.out.println("✅ Deleted user id = " + newId);

            System.out.println("\n------ END TEST ------");

        } catch (Exception e) {
            System.out.println("❌ CRUD test failed:");
            e.printStackTrace();
        }
    }
}
