package com.example.LoginDemo.controller;

import com.example.LoginDemo.config.JwtUtil;
import com.example.LoginDemo.model.User;
import com.example.LoginDemo.service.AuthService;
import com.example.LoginDemo.service.EmailService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling authentication operations using database storage.
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

    /**
     * Handles user login and sends a magic link via email.
     *
     * @param request The login request containing email and browser ID.
     * @return LoginResponse containing status and message.
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String email = request.getEmail();

        logger.info("Login attempt - Email: {}", email);

        if (authService.isValidEmail(email)) {
            User user = authService.getUserByEmail(email);
            String token = jwtUtil.generateToken(email, user.getRole());

            // Update user's token in database
            authService.updateUserToken(email, token);
            emailService.sendMagicLink(email, token);

            return new LoginResponse(null, email, null, user.getCounter(),
                    false, "Email sent successfully", user.getRole());
        }

        logger.warn("Invalid email login attempt: {}", email);
        throw new IllegalArgumentException("Invalid email format.");
    }

    /**
     * Validates a JWT token and establishes a browser session.
     *
     * @param token The JWT token.
     * @param browserId The browser ID.
     * @return LoginResponse containing user details if valid.
     */
    @GetMapping("/validate")
    public LoginResponse validateToken(@RequestParam String token, @RequestParam String browserId) {
        if (token != null && jwtUtil.isValidToken(token)) {
            String email = jwtUtil.extractUsername(token);
            User user = authService.getUserByEmail(email);

            if (token.equals(user.getCurrentToken())) {
                // Add browser session to database
                if(!authService.isActiveBrowserSession(browserId)){
                    authService.addUserSession(email, browserId);
                }

                String role = jwtUtil.extractRole(token);
                logger.info("Token validation successful - Email: {}, BrowserId: {}, Role: {}",
                        email, browserId, role);

                return new LoginResponse(
                        token,
                        email,
                        browserId,
                        user.getCounter(),
                        false,
                        "Login successful",
                        role
                );
            }
        }

        logger.warn("Token validation failed for token: {}", token);
        throw new IllegalArgumentException("Invalid token.");
    }

    /**
     * Adds a new user to the system. Only administrators are allowed to perform this action.
     *
     * @param request The request object containing the new user's email and username.
     * @param token The JWT token for authentication.
     * @return ResponseEntity indicating success or failure.
     * @throws JwtException if the token is invalid or unauthorized.
     */
    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody AddUserRequest request,
                                     @RequestHeader("Authorization") String token) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new JwtException("Invalid token format");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isValidToken(jwtToken) ||
                    !"ADMIN".equals(jwtUtil.extractRole(jwtToken))) {
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
     * Retrieves a list of all non-admin users in the system.
     *
     * @param token The JWT token for authentication.
     * @return ResponseEntity containing a list of non-admin users.
     * @throws JwtException if the token is invalid or unauthorized.
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new JwtException("Invalid token format");
            }

            String jwtToken = token.substring(7);
            if (!jwtUtil.isValidToken(jwtToken) ||
                    !"ADMIN".equals(jwtUtil.extractRole(jwtToken))) {
                throw new JwtException("Unauthorized access");
            }

            Map<String, Map<String, Object>> userDetails = new HashMap<>();
            authService.getAllUsers().forEach(user -> {
                if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("username", user.getUsername());
                    details.put("role", user.getRole());
                    details.put("counter", user.getCounter());
                    userDetails.put(user.getEmail(), details);
                }
            });

            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Logs out a user from either a single browser session or all sessions.
     *
     * @param request The request object containing the username, browser ID, and logout preference.
     * @return ResponseEntity containing logout success message.
     */
    @PostMapping("/logout")
    public LogoutResponse logout(@RequestBody LogoutRequest request) {
        String email = request.getUsername();
        String browserId = request.getBrowserId();
        boolean logoutAll = request.isLogoutAll();

        authService.logout(email, browserId, logoutAll);

        String message = logoutAll ?
                "Logged out from all browsers." :
                "Logged out from this browser only.";

        logger.info("User {} logged out. All sessions: {}", email, logoutAll);
        return new LogoutResponse(true, message);
    }


    /**
     * Increments the user's counter if the provided JWT token is valid.
     *
     * @param request The request object containing the user's JWT token.
     * @return CounterResponse containing the updated counter value and success status.
     * @throws IllegalArgumentException if the token is invalid.
     */
    @PostMapping("/increment")
    public CounterResponse incrementCounter(@RequestBody IncrementRequest request) {
        String token = request.getToken();
        if (token != null && jwtUtil.isValidToken(token)) {
            String email = jwtUtil.extractUsername(token);
            User user = authService.getUserByEmail(email);

            if (token.equals(user.getCurrentToken())) {
                int newCount = authService.incrementCounter(email);
                logger.info("Counter incremented - User: {}, New Count: {}", email, newCount);
                return new CounterResponse(newCount, true);
            }
        }

        logger.warn("Counter increment failed for token: {}", token);
        throw new IllegalArgumentException("Invalid token for counter increment.");
    }

    /**
     * Request object for incrementing a user's counter.
     */
    static class IncrementRequest {
        private String token;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    /**
     * Request object for adding a new user.
     */
    static class AddUserRequest {
        private String email;
        private String username;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    /**
     * Response object for login requests.
     */
    static class LoginResponse {
        private String token;
        private String username;
        private String browserId;
        private int counter;
        private boolean error;
        private String msg;
        private String role;

        /**
         * Constructs a LoginResponse.
         *
         * @param token The JWT token.
         * @param username The username.
         * @param browserId The browser ID.
         * @param counter The user's counter.
         * @param error Whether an error occurred.
         * @param msg The response message.
         * @param role The user's role.
         */
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

        // Getters and setters remain the same
        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getBrowserId() { return browserId; }
        public int getCounter() { return counter; }
        public boolean isError() { return error; }
        public String getMsg() { return msg; }
        public String getRole() { return role; }
    }

    /**
     * Response object for counter increment operations.
     */
    static class CounterResponse {
        private int count;
        private boolean success;

        /**
         * Constructs a CounterResponse.
         *
         * @param count The new counter value.
         * @param success Whether the operation was successful.
         */
        public CounterResponse(int count, boolean success) {
            this.count = count;
            this.success = success;
        }

        public int getCount() { return count; }
        public boolean isSuccess() { return success; }
    }

    /**
     * Request object for login operations.
     */
    static class LoginRequest {
        private String email;
        private String browserId;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getBrowserId() { return browserId; }
        public void setBrowserId(String browserId) { this.browserId = browserId; }
    }

    /**
     * Request object for user logout.
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
     * Response object for logout operations.
     */
    static class LogoutResponse {
        private boolean success;
        private String message;

        /**
         * Constructs a LogoutResponse.
         *
         * @param success Whether the operation was successful.
         * @param message A message describing the outcome.
         */
        public LogoutResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}