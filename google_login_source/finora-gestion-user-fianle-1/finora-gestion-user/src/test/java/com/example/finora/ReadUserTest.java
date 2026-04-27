package com.example.finora;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReadUserTest {

    private UserService service;
    private int createdId;

    @BeforeEach
    void setup() throws Exception {
        service = new UserService();

        String uniqueEmail = "read" + System.currentTimeMillis() + "@mail.com";
        String uniqueUsername = "readUser" + System.currentTimeMillis();

        User u = new User(uniqueUsername, uniqueEmail, "Test1234", "ENTREPRISE");
        createdId = service.addUserReturnId(u);
    }

    @AfterEach
    void cleanup() throws Exception {
        service.deleteUser(createdId);
    }

    @Test
    void shouldReadUserById() throws Exception {
        User found = service.getUserById(createdId);

        assertNotNull(found);
        assertEquals(createdId, found.getId());
        assertEquals("ENTREPRISE", found.getRole());
    }
}
