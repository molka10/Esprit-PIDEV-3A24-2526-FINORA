package com.example.finora_user;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CreateUserTest {

    private UserService service;
    private int createdId;

    @BeforeEach
    void setup() throws Exception {
        service = new UserService();
        createdId = -1;
    }

    @AfterEach
    void cleanup() throws Exception {
        // cleanup inserted user if test created it
        if (createdId != -1) {
            service.deleteUser(createdId);
        }
    }

    @Test
    void shouldCreateUser() throws Exception {
        String uniqueEmail = "test" + System.currentTimeMillis() + "@mail.com";
        String uniqueUsername = "user" + System.currentTimeMillis();

        User u = new User(uniqueUsername, uniqueEmail, "Test1234", "USER");

        createdId = service.addUserReturnId(u);

        assertTrue(createdId > 0, "ID should be generated");
        assertNotNull(service.getUserById(createdId), "User should exist after insert");
    }
}
