package com.example.LoginDemo.controller;

import com.example.LoginDemo.service.EmailService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.LoginDemo.config.JwtUtil;
import com.example.LoginDemo.service.AuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for handling authentication operations such as login, token validation,
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

    @Autowired
    private EmailService emailService;

    private static final ConcurrentHashMap<String, String> userTokens = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> userCounters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> userBrowserSessions = new ConcurrentHashMap<>();

    /**
     * Handles user login and sends a magic link via email.
     *
     * @param request the login request containing the user's email and browser ID
     * @return a response indicating whether the email was sent successfully
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String browserId = request.getBrowserId();

        logger.info("Login attempt - Email: {}, BrowserId: {}", email, browserId);

        if (authService.isValidEmail(email)) {
            AuthService.UserInfo userInfo = authService.getUserInfo(email);
            String token = jwtUtil.generateToken(email, userInfo.getRole());
            userTokens.put(email, token);
            userCounters.putIfAbsent(email, 0);
            emailService.sendMagicLink(email, token);

            return new LoginResponse(null, email, null, userCounters.get(email),
                    false, "Email sent successfully", userInfo.getRole());
        }
        logger.warn("Invalid email login attempt: {}", email);
        throw new IllegalArgumentException("Invalid email format.");
    }

    /**
     * Adds a new user. Only admin users can perform this action.
     *
     * @param request the user details
     * @param token   the authentication token
     * @return a response indicating whether the user was added successfully
     */
    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody AddUserRequest request, @RequestHeader("Authorization") String token) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new JwtException("Invalid token format");
            }
            String jwtToken = token.substring(7);
            if (!jwtUtil.isValidToken(jwtToken) || !"ADMIN".equals(jwtUtil.extractRole(jwtToken))) {
                throw new JwtException("Unauthorized access");
            }
            authService.addUser(request.getEmail(), request.getUsername(), "USER");
            return ResponseEntity.ok("User added successfully");
        } catch (Exception e) {
            logger.error("Error adding user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Fetches all non-admin users.
     *
     * @param token the authentication token
     * @return a response containing user details
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new JwtException("Invalid token format");
            }
            String jwtToken = token.substring(7);
            if (!jwtUtil.isValidToken(jwtToken) || !"ADMIN".equals(jwtUtil.extractRole(jwtToken))) {
                throw new JwtException("Unauthorized access");
            }

            Map<String, Map<String, Object>> userDetails = new HashMap<>();
            authService.getAllUsers().forEach((email, userInfo) -> {
                if (!"admin".equalsIgnoreCase(userInfo.getRole())) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("username", userInfo.getUsername());
                    details.put("role", userInfo.getRole());
                    details.put("counter", userCounters.getOrDefault(email, 0));
                    userDetails.put(email, details);
                }
            });

            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates a JWT token and establishes a browser session.
     *
     * @param token     the JWT token to validate
     * @param browserId the browser ID from which the request is made
     * @return a response containing user details if validation is successful
     */
    @GetMapping("/validate")
    public LoginResponse validateToken(@RequestParam String token, @RequestParam String browserId) {
        if (token != null && jwtUtil.isValidToken(token)) {
            String email = jwtUtil.extractUsername(token);
            String storedToken = userTokens.get(email);

            if (token.equals(storedToken)) {
                userBrowserSessions.computeIfAbsent(email, k -> new ConcurrentHashMap<>()).put(browserId, true);

                // Get the user's role from the token
                String role = jwtUtil.extractRole(token);

                logger.info("Token validation successful - Email: {}, BrowserId: {}, Role: {}", email, browserId, role);

                return new LoginResponse(
                        token,
                        email,
                        browserId,
                        userCounters.getOrDefault(email, 0),
                        false,
                        "Login successful",
                        role  // Include the actual role from the token
                );
            }
        }
        logger.warn("Token validation failed for token: {}", token);
        throw new IllegalArgumentException("Invalid token.");
    }


    /**
     * Logs out a user from either a single browser session or all sessions.
     *
     * @param request the logout request containing user details
     * @return a response indicating successful logout
     */
    @PostMapping("/logout")
    public LogoutResponse logout(@RequestBody LogoutRequest request) {
        String email = request.getUsername();
        String browserId = request.getBrowserId();
        boolean logoutAll = request.isLogoutAll();

        if (logoutAll) {
            userBrowserSessions.remove(email);
            userTokens.remove(email);
            userCounters.put(email, 0);
            logger.info("User {} logged out from all browsers.", email);
            return new LogoutResponse(true, "Logged out from all browsers.");
        } else {
            ConcurrentHashMap<String, Boolean> userSessions = userBrowserSessions.get(email);
            if (userSessions != null) {
                userSessions.remove(browserId);
                if (userSessions.isEmpty()) {
                    userBrowserSessions.remove(email);
                    userTokens.remove(email);
                    userCounters.put(email, 0);
                }
            }
            logger.info("User {} logged out from browser: {}", email, browserId);
            return new LogoutResponse(true, "Logged out from this browser only.");
        }
    }

    /**
     * Increments the user's counter if the token is valid.
     *
     * @param request the request containing the JWT token
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
                logger.info("Counter incremented - User: {}, New Count: {}", username, newCount);
                return new CounterResponse(newCount, true);
            }
        }
        logger.warn("Counter increment failed for token: {}", token);
        throw new IllegalArgumentException("Invalid token for counter increment.");
    }

    /**
     * Request class for counter increment operation.
     */
    static class IncrementRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    static class AddUserRequest {
        private String email;
        private String username;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    static class LoginResponse {
        private String token;
        private String username;
        private String browserId;
        private int counter;
        private boolean error;
        private String msg;
        private String role;

        public LoginResponse(String token, String username, String browserId,
                             int counter, boolean error, String msg, String role) {
            this.token = token;
            this.username = username;
            this.browserId = browserId;
            this.counter = counter;
            this.error = error;
            this.msg = msg;
            this.role = role;
        }

        // Add getters and setters
        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getBrowserId() { return browserId; }
        public int getCounter() { return counter; }
        public boolean isError() { return error; }
        public String getMsg() { return msg; }
        public String getRole() {
            return role;
        }
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
        private String email;
        private String browserId;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getBrowserId() { return browserId; }
        public void setBrowserId(String browserId) { this.browserId = browserId; }
    }

    /**
     * Response class for login and token validation operations.
     */

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