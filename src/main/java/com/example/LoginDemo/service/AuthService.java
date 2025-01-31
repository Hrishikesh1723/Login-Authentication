package com.example.LoginDemo.service;

import com.example.LoginDemo.exception.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for handling authentication-related operations.
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // Stores a list of valid email addresses mapped to their usernames.
    private static final Map<String, String> VALID_EMAILS = new HashMap<>();

    // Stores pending login requests mapped to unique identifiers.
    private static final ConcurrentHashMap<String, String> pendingLogins = new ConcurrentHashMap<>();

    static {
        // Predefined valid emails for authentication.
        VALID_EMAILS.put("hrishikesh.r@ahduni.edu.in", "User One");
        VALID_EMAILS.put("admin@example.com", "Admin User");
        VALID_EMAILS.put("test@example.com", "Test User");
    }

    /**
     * Validates whether the provided email is registered in the system.
     *
     * @param email the email to validate
     * @return {@code true} if the email exists in the valid email list, {@code false} otherwise
     */
    public boolean isValidEmail(String email) {
        try {
            boolean isValid = VALID_EMAILS.containsKey(email);
            logger.info("Email validation check - Email: {}, Valid: {}", email, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error while validating email: {}", email, e);
            throw new RuntimeException("Error during email validation");
        }
    }

    /**
     * Retrieves the username associated with the given email.
     *
     * @param email the email to look up
     * @return the username associated with the email, or {@code null} if not found
     */
    public String getUsernameFromEmail(String email) {
        try {
            String username = VALID_EMAILS.get(email);
            logger.info("Fetching username for email: {}, Found: {}", email, (username != null));
            return username;
        } catch (Exception e) {
            logger.error("Error while fetching username for email: {}", email, e);
            throw new RuntimeException("Error retrieving username");
        }
    }
}
