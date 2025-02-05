package com.example.LoginDemo.dao;

import com.example.LoginDemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for User entity.
 * Provides database operations for User management.
 */
@Repository
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (ResultSet rs, int rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setRole(rs.getString("role"));
        user.setCounter(rs.getInt("counter"));
        user.setCurrentToken(rs.getString("current_token"));
        return user;
    };

    /**
     * Retrieves a user by email.
     *
     * @param email the email of the user
     * @return an Optional containing the User if found, otherwise empty
     */
    public Optional<User> findByEmail(String email) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE email = ?",
                    userRowMapper,
                    email
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            logger.warn("User not found with email: {}", email);
            return Optional.empty();
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving user by email: {}", email, e);
            throw e;
        }
    }

    /**
     * Checks if a user exists by email.
     *
     * @param email the email to check
     * @return true if the user exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE email = ?",
                    Integer.class,
                    email
            );
            return count != null && count > 0;
        } catch (DataAccessException e) {
            logger.error("Database error while checking user existence by email: {}", email, e);
            throw e;
        }
    }

    /**
     * Saves or updates a user in the database.
     *
     * @param user the user to save or update
     */
    public void save(User user) {
        try {
            if (user.getId() == null) {
                jdbcTemplate.update(
                        "INSERT INTO users (email, username, role, counter, current_token) VALUES (?, ?, ?, ?, ?)",
                        user.getEmail(),
                        user.getUsername(),
                        user.getRole(),
                        user.getCounter(),
                        user.getCurrentToken()
                );
                logger.info("New user added: {}", user.getEmail());
            } else {
                jdbcTemplate.update(
                        "UPDATE users SET email = ?, username = ?, role = ?, counter = ?, current_token = ? WHERE id = ?",
                        user.getEmail(),
                        user.getUsername(),
                        user.getRole(),
                        user.getCounter(),
                        user.getCurrentToken(),
                        user.getId()
                );
                logger.info("User updated: {}", user.getEmail());
            }
        } catch (DataAccessException e) {
            logger.error("Database error while saving user: {}", user.getEmail(), e);
            throw e;
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @return a list of users
     */
    public List<User> findAll() {
        try {
            return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving all users", e);
            throw e;
        }
    }
}
