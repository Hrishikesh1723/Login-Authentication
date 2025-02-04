package com.example.LoginDemo.dao;

import com.example.LoginDemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on the User entity.
 * This interface extends JpaRepository, providing built-in methods for database interaction.
 * Additional query methods are defined for user-specific queries such as finding by email.
 */
public interface UserDAO extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user
     * @return an Optional containing the User if found, or empty if no user exists with the given email
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user exists with the given email.
     *
     * @param email the email address to check
     * @return true if a user exists with the given email, false otherwise
     */
    boolean existsByEmail(String email);
}
