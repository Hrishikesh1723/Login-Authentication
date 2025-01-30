package com.example.LoginDemo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for handling JWT (JSON Web Token) operations such as
 * token generation, extraction, and validation.
 */
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class); // Logger instance
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generates a JWT token for a given username.
     *
     * @param username the username for which the token is generated
     * @return a signed JWT token
     */
    public String generateToken(String username) {
        logger.info("Generating JWT token for username: {}", username); // Log token generation
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                .signWith(SECRET_KEY)
                .compact();
        logger.debug("Generated JWT token: {}", token); // Log the generated token (debug level)
        return token;
    }

    /**
     * Extracts the username from the provided JWT token.
     *
     * @param token the JWT token
     * @return the username contained in the token
     */
    public String extractUsername(String token) {
        logger.debug("Extracting username from JWT token: {}", token); // Log token extraction
        String username = getClaims(token).getSubject();
        logger.info("Extracted username: {}", username); // Log the extracted username
        return username;
    }

    /**
     * Validates a JWT token by checking its expiration and username extraction.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean isValidToken(String token) {
        logger.debug("Validating JWT token: {}", token); // Log token validation
        boolean isValid = extractUsername(token) != null && !isTokenExpired(token);
        logger.info("Token validation result: {}", isValid); // Log the validation result
        return isValid;
    }

    /**
     * Extracts claims from the given JWT token.
     *
     * @param token the JWT token
     * @return claims extracted from the token
     */
    private Claims getClaims(String token) {
        logger.debug("Extracting claims from JWT token: {}", token); // Log claims extraction
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the JWT token has expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        logger.debug("Checking if JWT token is expired: {}", token); // Log expiration check
        boolean isExpired = getClaims(token).getExpiration().before(new Date());
        logger.info("Token expiration check result: {}", isExpired); // Log the expiration result
        return isExpired;
    }
}