package com.example.LoginDemo.dao;

import com.example.LoginDemo.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for handling user session-related database operations.
 * This interface extends JpaRepository, enabling built-in CRUD operations and
 * custom query methods for session management.\
 */
public interface UserSessionDAO extends JpaRepository<UserSession, Long> {

    /**
     * Retrieves all active user sessions for a given email.
     *
     * @param email  the email address associated with the user
     * @param active session status (true for active, false for inactive)
     * @return a list of active {@link UserSession} instances
     */
    List<UserSession> findByUserEmailAndActive(String email, boolean active);

    /**
     * Finds an active user session for a given email and browser ID.
     *
     * @param email     the email address associated with the user
     * @param browserId the browser identifier for the session
     * @param active    session status (true for active, false for inactive)
     * @return an Optional containing the {@link UserSession} if found, or empty if not found
     */
    List<UserSession> findByUserEmailAndBrowserIdAndActive(String email, String browserId, boolean active);
}

