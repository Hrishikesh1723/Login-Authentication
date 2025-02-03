package com.example.LoginDemo.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling JWT operations such as generation, validation, and extraction of claims.
 */
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generates a JWT token for a given username and role.
     *
     * @param username the username for which the token is generated
     * @param role the role associated with the user
     * @return the generated JWT token
     */
    public String generateToken(String username, String role) {
        try {
            logger.info("Generating JWT token for username: {} with role: {}", username, role);
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                    .signWith(SECRET_KEY)
                    .compact();
        } catch (JwtException ex) {
            logger.error("Error generating JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Extracts the username from a given JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        try {
            logger.debug("Extracting username from JWT token");
            return getClaims(token).getSubject();
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token has expired: {}", ex.getMessage());
            throw ex;
        } catch (JwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Extracts the role from a given JWT token.
     *
     * @param token the JWT token
     * @return the role extracted from the token
     */
    public String extractRole(String token) {
        try {
            logger.debug("Extracting role from JWT token");
            return getClaims(token).get("role", String.class);
        } catch (JwtException ex) {
            logger.error("Error extracting role from JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Validates a JWT token by checking its username and expiration.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean isValidToken(String token) {
        try {
            logger.debug("Validating JWT token");
            return extractUsername(token) != null && !isTokenExpired(token);
        } catch (JwtException ex) {
            logger.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extracts claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    private Claims getClaims(String token) {
        try {
            logger.debug("Extracting claims from JWT token");
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException ex) {
            logger.error("Error extracting claims from JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Checks whether a JWT token has expired.
     *
     * @param token the JWT token to check
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        try {
            logger.debug("Checking if JWT token is expired");
            boolean isExpired = getClaims(token).getExpiration().before(new Date());
            logger.info("Token expiration check result: {}", isExpired);
            return isExpired;
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token is expired: {}", ex.getMessage());
            throw ex;
        }
    }
}
