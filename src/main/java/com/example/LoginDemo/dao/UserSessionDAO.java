package com.example.LoginDemo.dao;

import com.example.LoginDemo.model.UserSession;
import com.example.LoginDemo.exception.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) for managing user session records in the database.
 */
@Repository
public class UserSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserSessionDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDAO userDAO;

    private final RowMapper<UserSession> sessionRowMapper = new RowMapper<>() {
        @Override
        public UserSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserSession session = new UserSession();
            session.setId(rs.getLong("id"));
            session.setBrowserId(rs.getString("browser_id"));
            session.setActive(rs.getBoolean("active"));

            // Get the associated user
            String userEmail = rs.getString("user_email");
            userDAO.findByEmail(userEmail).ifPresent(session::setUser);

            return session;
        }
    };

    /**
     * Retrieves active user sessions for a given email.
     *
     * @param email the user's email
     * @param active session status
     * @return list of active sessions for the user
     */
    public List<UserSession> findByUserEmailAndActive(String email, boolean active) {
        try {
            return jdbcTemplate.query(
                    "SELECT s.*, u.email as user_email FROM user_sessions s " +
                            "JOIN users u ON s.user_id = u.id " +
                            "WHERE u.email = ? AND s.active = ?",
                    sessionRowMapper,
                    email, active
            );
        } catch (Exception ex) {
            logger.error("Error fetching user sessions by email and active status: {}", ex.getMessage());
            throw new RuntimeException("Error fetching user sessions", ex);
        }
    }

    /**
     * Retrieves active user sessions for a given email and browser ID.
     *
     * @param email user's email
     * @param browserId the browser identifier
     * @param active session status
     * @return list of active sessions matching the criteria
     */
    public List<UserSession> findByUserEmailAndBrowserIdAndActive(String email, String browserId, boolean active) {
        try {
            return jdbcTemplate.query(
                    "SELECT s.*, u.email as user_email FROM user_sessions s " +
                            "JOIN users u ON s.user_id = u.id " +
                            "WHERE u.email = ? AND s.browser_id = ? AND s.active = ?",
                    sessionRowMapper,
                    email, browserId, active
            );
        } catch (Exception ex) {
            logger.error("Error fetching user sessions by email, browser ID, and active status: {}", ex.getMessage());
            throw new RuntimeException("Error fetching user sessions", ex);
        }
    }

    /**
     * Checks if a browser session is active.
     *
     * @param browserId the browser identifier
     * @return true if an active session exists, false otherwise
     */
    public boolean browserSessionActive(String browserId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_sessions WHERE browser_id = ? AND active = ?",
                    Integer.class,
                    browserId,
                    true
            );
            return count != null && count > 0;
        } catch (Exception ex) {
            logger.error("Error checking browser session activity: {}", ex.getMessage());
            throw new RuntimeException("Error checking browser session activity", ex);
        }
    }

    /**
     * Saves a user session.
     * If the session ID is null, it inserts a new session; otherwise, it updates an existing session.
     *
     * @param session the user session to save
     */
    public void save(UserSession session) {
        try {
            if (session.getId() == null) {
                jdbcTemplate.update(
                        "INSERT INTO user_sessions (user_id, browser_id, active, created_at, updated_at) " +
                                "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                        session.getUser().getId(),
                        session.getBrowserId(),
                        session.isActive()
                );
            } else {
                jdbcTemplate.update(
                        "UPDATE user_sessions SET browser_id = ?, active = ?, updated_at = CURRENT_TIMESTAMP " +
                                "WHERE id = ?",
                        session.getBrowserId(),
                        session.isActive(),
                        session.getId()
                );
            }
        } catch (Exception ex) {
            logger.error("Error saving user session: {}", ex.getMessage());
            throw new RuntimeException("Error saving user session", ex);
        }
    }

    /**
     * Saves multiple user sessions.
     *
     * @param sessions list of user sessions to save
     */
    public void saveAll(List<UserSession> sessions) {
        try {
            for (UserSession session : sessions) {
                save(session);
            }
        } catch (Exception ex) {
            logger.error("Error saving multiple user sessions: {}", ex.getMessage());
            throw new RuntimeException("Error saving multiple user sessions", ex);
        }
    }
}
