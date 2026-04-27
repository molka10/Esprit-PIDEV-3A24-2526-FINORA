package com.example.finora.main;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;

import java.time.LocalDate;

public class testUser {

    public static void main(String[] args) {

        try {
            UserService service = new UserService();

            // Create user with new fields
            User u = new User();
            u.setUsername("testUser");
            u.setEmail("test@mail.com");
            u.setMotDePasse("123456");
            u.setRole("USER");
            u.setPhone("12345678");
            u.setAddress("Tunis");
            u.setDateOfBirth(LocalDate.of(2000, 1, 1));

            int id = service.addUserReturnId(u);

            if (id != -1) {
                System.out.println("✅ User added with ID: " + id);
            } else {
                System.out.println("❌ Insert failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
