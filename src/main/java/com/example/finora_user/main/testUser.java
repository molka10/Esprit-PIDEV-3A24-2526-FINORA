package com.example.finora_user.main;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;

public class testUser {
    public static void main(String[] args) {
        try {
            UserService service = new UserService();

            User u = new User("houyem", "houyem@mail.com", "Test1234", "USER");
            service.addUser(u);

            service.getAllUsers().forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
