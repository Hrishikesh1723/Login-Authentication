package com.example.LoginDemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling user authentication.
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("admin", "admin123");
        USERS.put("user", "password");
        logger.info("Registered users: {}", USERS.keySet());
    }

    /**
     * Authenticates a user based on username and password.
     *
     * @param username the username
     * @param password the password
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticate(String username, String password) {
        logger.info("Authentication attempt for user: {}", username);
        String storedPassword = USERS.get(username);
        logger.debug("Stored password exists: {}", storedPassword != null);
        if (storedPassword != null) {
            logger.debug("Password match: {}", storedPassword.equals(password));
        }
        return storedPassword != null && storedPassword.equals(password);
    }
}