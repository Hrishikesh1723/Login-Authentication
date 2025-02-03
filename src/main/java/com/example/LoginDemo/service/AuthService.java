package com.example.LoginDemo.service;

import com.example.LoginDemo.exception.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for handling authentication and user management.
 */
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

    /**
     * Checks if the given email exists in the user database.
     *
     * @param email the email to validate
     * @return true if the email exists, false otherwise
     */
    public boolean isValidEmail(String email) {
        try {
            boolean isValid = VALID_USERS.containsKey(email);
            logger.info("Email validation check - Email: {}, Valid: {}", email, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error while validating email: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during email validation");
        }
    }

    /**
     * Retrieves user information by email.
     *
     * @param email the user's email
     * @return UserInfo object if found, null otherwise
     */
    public UserInfo getUserInfo(String email) {
        try {
            if (!VALID_USERS.containsKey(email)) {
                logger.warn("User not found for email: {}", email);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
            UserInfo userInfo = VALID_USERS.get(email);
            logger.info("Fetching user info for email: {}, Found: {}", email, (userInfo != null));
            return userInfo;
        } catch (Exception e) {
            logger.error("Error while fetching user info for email: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving user info");
        }
    }

    /**
     * Adds a new user to the database.
     *
     * @param email    the user's email
     * @param username the user's name
     * @param role     the user's role
     */
    public void addUser(String email, String username, String role) {
        try {
            if (VALID_USERS.containsKey(email)) {
                logger.warn("Attempt to add duplicate user: {}", email);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
            }
            VALID_USERS.put(email, new UserInfo(username, role));
            logger.info("Added new user - Email: {}, Username: {}, Role: {}", email, username, role);
        } catch (Exception e) {
            logger.error("Error while adding user: {}", email, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding user");
        }
    }

    /**
     * Retrieves all registered users.
     *
     * @return a map of email addresses and corresponding user information
     */
    public Map<String, UserInfo> getAllUsers() {
        return new HashMap<>(VALID_USERS);
    }

    /**
     * Inner class representing user information.
     */
    public static class UserInfo {
        private final String username;
        private final String role;

        public UserInfo(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
