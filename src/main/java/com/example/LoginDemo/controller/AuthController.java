package com.example.LoginDemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.LoginDemo.config.JwtUtil;
import com.example.LoginDemo.service.AuthService;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for handling authentication requests such as login, token validation,
 * counter increment, and logout.
 */
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    private static final ConcurrentHashMap<String, String> userTokens = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> userCounters = new ConcurrentHashMap<>();
    // Track active browser sessions for each user
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> userBrowserSessions = new ConcurrentHashMap<>();

    /**
     * Handles user login authentication.
     *
     * @param request the login request containing username, password, and browserId
     * @return a login response with a JWT token if successful
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String browserId = request.getBrowserId();

        logger.info("Login attempt - Username: {}, BrowserId: {}", username, browserId);

        if (authService.authenticate(username, password)) {
            logger.info("Authentication successful for user: {}", username);
            String token = userTokens.get(username);
            if (token == null || !jwtUtil.isValidToken(token)) {
                token = jwtUtil.generateToken(username);
                userTokens.put(username, token);
                userCounters.putIfAbsent(username, 0);
            }

            userBrowserSessions.computeIfAbsent(username, k -> new ConcurrentHashMap<>())
                    .put(browserId, true);

            return new LoginResponse(token, username, browserId,
                    userCounters.get(username), false);
        }
        logger.warn("Authentication failed for user: {}", username);
        return new LoginResponse(null, null, null, 0, true);
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token
     * @param browserId the browser ID
     * @return a response indicating if the token is valid
     */
    @GetMapping("/validate")
    public LoginResponse validateToken(@RequestParam String token, @RequestParam String browserId) {
        if (token != null && jwtUtil.isValidToken(token)) {
            String username = jwtUtil.extractUsername(token);
            String storedToken = userTokens.get(username);

            // Check if this browser session is still active
            ConcurrentHashMap<String, Boolean> userSessions = userBrowserSessions.get(username);
            if (userSessions == null || !userSessions.getOrDefault(browserId, false)) {
                logger.warn("Invalid or inactive browser session for user: {}", username);
                return new LoginResponse(null, null, null, 0, true);
            }

            if (token.equals(storedToken)) {
                logger.info("Token validation successful for user: {}", username);
                return new LoginResponse(token, username, browserId,
                        userCounters.get(username), false);
            }
        }
        logger.warn("Token validation failed");
        return new LoginResponse(null, null, null, 0, true);
    }

    /**
     * Increments the user counter if the provided token is valid.
     *
     * @param request the increment request containing the token
     * @return a response with the updated counter value
     */
    @PostMapping("/increment")
    public CounterResponse incrementCounter(@RequestBody IncrementRequest request) {
        String token = request.getToken();
        if (token != null && jwtUtil.isValidToken(token)) {
            String username = jwtUtil.extractUsername(token);
            String storedToken = userTokens.get(username);
            if (token.equals(storedToken)) {
                int newCount = userCounters.merge(username, 1, Integer::sum);
                logger.info("Counter incremented for user: {}. New count: {}", username, newCount);
                return new CounterResponse(newCount, true);
            }
        }
        logger.warn("Counter increment failed for token: {}", token);
        return new CounterResponse(0, false);
    }

    /**
     * Logs out the user from one or all active sessions.
     *
     * @param request the logout request containing username, browserId, and logoutAll flag
     * @return a response indicating the success of the logout operation
     */
    @PostMapping("/logout")
    public LogoutResponse logout(@RequestBody LogoutRequest request) {
        String username = request.getUsername();
        String browserId = request.getBrowserId();
        boolean logoutAll = request.isLogoutAll();

        if (logoutAll) {
            // Remove all browser sessions for this user
            userBrowserSessions.remove(username);
            // Remove token and reset counter
            userTokens.remove(username);
            userCounters.put(username, 0);
            logger.info("User {} logged out from all browsers", username);
            return new LogoutResponse(true, "Logged out from all browsers");
        } else {
            // Remove only this browser session
            ConcurrentHashMap<String, Boolean> userSessions = userBrowserSessions.get(username);
            if (userSessions != null) {
                userSessions.remove(browserId);
                // If no more active sessions, clean up user data
                if (userSessions.isEmpty()) {
                    userBrowserSessions.remove(username);
                    userTokens.remove(username);
                    userCounters.put(username, 0);
                }
            }
            logger.info("User {} logged out from browser: {}", username, browserId);
            return new LogoutResponse(true, "Logged out from this browser only");
        }
    }

    /**
     * Request class for counter increment operation.
     */
    static class IncrementRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    /**
     * Response class for counter increment operation.
     */
    static class CounterResponse {
        private int count;
        private boolean success;

        public CounterResponse(int count, boolean success) {
            this.count = count;
            this.success = success;
        }

        public int getCount() { return count; }
        public boolean isSuccess() { return success; }
    }

    /**
     * Request class for login operation.
     */
    static class LoginRequest {
        private String username;
        private String password;
        private String browserId;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getBrowserId() { return browserId; }
        public void setBrowserId(String browserId) { this.browserId = browserId; }
    }

    /**
     * Response class for login and token validation operations.
     */
    static class LoginResponse {
        private String token;
        private String username;
        private String browserId;
        private int counter;
        private boolean error;

        public LoginResponse(String token, String username, String browserId,
                             int counter, boolean error) {
            this.token = token;
            this.username = username;
            this.browserId = browserId;
            this.counter = counter;
            this.error = error;
        }

        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getBrowserId() { return browserId; }
        public int getCounter() { return counter; }
        public boolean isError() { return error; }
    }

    /**
     * Request class for logout operation.
     */
    static class LogoutRequest {
        private String username;
        private String browserId;
        private boolean logoutAll;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getBrowserId() { return browserId; }
        public void setBrowserId(String browserId) { this.browserId = browserId; }
        public boolean isLogoutAll() { return logoutAll; }
        public void setLogoutAll(boolean logoutAll) { this.logoutAll = logoutAll; }
    }

    /**
     * Response class for logout operation.
     */
    static class LogoutResponse {
        private boolean success;
        private String message;

        public LogoutResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}