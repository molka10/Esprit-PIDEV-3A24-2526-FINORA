package com.example.finora;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteUserTest {

    private UserService service;
    private int createdId;

    @BeforeEach
    void setup() throws Exception {
        service = new UserService();

        String uniqueEmail = "del" + System.currentTimeMillis() + "@mail.com";
        String uniqueUsername = "delUser" + System.currentTimeMillis();

        User u = new User(uniqueUsername, uniqueEmail, "Test1234", "USER");
        createdId = service.addUserReturnId(u);

        assertNotNull(service.getUserById(createdId), "User must exist before delete");
    }

    @Test
    void shouldDeleteUser() throws Exception {
        boolean deleted = service.deleteUser(createdId);
        assertTrue(deleted, "Delete should return true");

        User after = service.getUserById(createdId);
        assertNull(after, "User should not exist after delete");
    }
}
