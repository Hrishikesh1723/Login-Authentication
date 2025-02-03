package com.example.LoginDemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final Map<String, UserInfo> VALID_USERS = new HashMap<>();
    private static final ConcurrentHashMap<String, String> pendingLogins = new ConcurrentHashMap<>();

    static {
        // Predefined users with roles
        VALID_USERS.put("hrishikesh.r@ahduni.edu.in", new UserInfo("Admin User", "ADMIN"));
        VALID_USERS.put("hrishikeshrana1723@gmail.com", new UserInfo("Test User", "USER"));
    }

    public boolean isValidEmail(String email) {
        try {
            boolean isValid = VALID_USERS.containsKey(email);
            logger.info("Email validation check - Email: {}, Valid: {}", email, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error while validating email: {}", email, e);
            throw new RuntimeException("Error during email validation");
        }
    }

    public UserInfo getUserInfo(String email) {
        try {
            UserInfo userInfo = VALID_USERS.get(email);
            logger.info("Fetching user info for email: {}, Found: {}", email, (userInfo != null));
            return userInfo;
        } catch (Exception e) {
            logger.error("Error while fetching user info for email: {}", email, e);
            throw new RuntimeException("Error retrieving user info");
        }
    }

    public void addUser(String email, String username, String role) {
        if (VALID_USERS.containsKey(email)) {
            throw new RuntimeException("User already exists");
        }
        VALID_USERS.put(email, new UserInfo(username, role));
        logger.info("Added new user - Email: {}, Username: {}, Role: {}", email, username, role);
    }

    public Map<String, UserInfo> getAllUsers() {
        return new HashMap<>(VALID_USERS);
    }

    public static class UserInfo {
        private final String username;
        private final String role;

        public UserInfo(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}