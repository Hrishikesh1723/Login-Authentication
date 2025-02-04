package com.example.LoginDemo.service;

import com.example.LoginDemo.dao.UserDAO;
import com.example.LoginDemo.model.User;
import com.example.LoginDemo.model.UserSession;
import com.example.LoginDemo.dao.UserSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Authentication Service to handle user authentication and session management.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserSessionDAO sessionDAO;

    /**
     * Checks if an email exists in the database.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isValidEmail(String email) {
        logger.info("Checking if email exists: {}", email);
        return userDAO.existsByEmail(email);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the email to search for
     * @return the User object
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userDAO.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    /**
     * Updates the authentication token of a user.
     *
     * @param email the user's email
     * @param token the new token
     */
    @Transactional
    public void updateUserToken(String email, String token) {
        logger.info("Updating user token for email: {}", email);
        User user = getUserByEmail(email);
        user.setCurrentToken(token);
        userDAO.save(user);
    }

    /**
     * Adds a user session to track login activity.
     *
     * @param email the user's email
     * @param browserId the browser identifier
     */
    @Transactional
    public void addUserSession(String email, String browserId) {
        logger.info("Adding user session for email: {}, browserId: {}", email, browserId);
        User user = getUserByEmail(email);
        UserSession session = new UserSession();
        session.setUser(user);
        session.setBrowserId(browserId);
        session.setActive(true);
        sessionDAO.save(session);
    }

    /**
     * Logs out a user session, either a specific session or all sessions.
     *
     * @param email the user's email
     * @param browserId the browser identifier
     * @param logoutAll flag to determine if all sessions should be logged out
     */
    @Transactional
    public void logout(String email, String browserId, boolean logoutAll) {
        logger.info("Logging out user: {}, logoutAll: {}", email, logoutAll);
        if (logoutAll) {
            List<UserSession> sessions = sessionDAO.findByUserEmailAndActive(email, true);
            sessions.forEach(session -> session.setActive(false));
            sessionDAO.saveAll(sessions);

            User user = getUserByEmail(email);
            user.setCurrentToken(null);
            user.setCounter(0);
            userDAO.save(user);
        } else {
            List<UserSession> sessions_bid = sessionDAO.findByUserEmailAndBrowserIdAndActive(email, browserId, true);
            sessions_bid.forEach(session -> {
                session.setActive(false);
            });
            sessionDAO.saveAll(sessions_bid);
            List<UserSession> sessions = sessionDAO.findByUserEmailAndActive(email, true);
            if(sessions.isEmpty()) {
                User user = getUserByEmail(email);
                user.setCurrentToken(null);
                user.setCounter(0);
                userDAO.save(user);
            }
        }
    }

    /**
     * Increments the login attempt counter for a user.
     * @param email the user's email
     * @return the updated counter value
     */
    @Transactional
    public int incrementCounter(String email) {
        logger.info("Incrementing counter for user: {}", email);
        User user = getUserByEmail(email);
        user.setCounter(user.getCounter() + 1);
        userDAO.save(user);
        return user.getCounter();
    }

    /**
     * Retrieves all users from the database.
     * @return List of all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userDAO.findAll();
    }

    /**
     * Adds a new user to the database.
     *
     * @param email user's email
     * @param username user's name
     * @param role user's role
     */
    @Transactional
    public void addUser(String email, String username, String role) {
        logger.info("Adding new user: email={}, username={}, role={}", email, username, role);
        if (userDAO.existsByEmail(email)) {
            logger.warn("User already exists with email: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setRole(role);
        newUser.setCounter(0);

        userDAO.save(newUser);
    }
}
