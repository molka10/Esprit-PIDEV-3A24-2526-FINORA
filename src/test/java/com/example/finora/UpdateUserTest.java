package com.example.finora;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateUserTest {

    private UserService service;
    private int createdId;

    @BeforeEach
    void setup() throws Exception {
        service = new UserService();

        String uniqueEmail = "upd" + System.currentTimeMillis() + "@mail.com";
        String uniqueUsername = "updUser" + System.currentTimeMillis();

        User u = new User(uniqueUsername, uniqueEmail, "Test1234", "USER");
        createdId = service.addUserReturnId(u);
    }

    @AfterEach
    void cleanup() throws Exception {
        service.deleteUser(createdId);
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User updated = new User("newUsername", "new" + System.currentTimeMillis() + "@mail.com", "", "ADMIN");
        updated.setId(createdId);

        boolean ok = service.updateUser(updated);
        assertTrue(ok, "Update should return true");

        User after = service.getUserById(createdId);
        assertNotNull(after);
        assertEquals("newUsername", after.getUsername());
        assertEquals("ADMIN", after.getRole());
    }
}
