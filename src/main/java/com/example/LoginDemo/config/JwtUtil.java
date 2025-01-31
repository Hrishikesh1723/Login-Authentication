package com.example.LoginDemo.config;

import io.jsonwebtoken.*;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generates a JWT token for a given username.
     *
     * @param username the username for which the token is generated
     * @return a signed JWT token
     */
    public String generateToken(String username) {
        try {
            logger.info("Generating JWT token for username: {}", username);
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                    .signWith(SECRET_KEY)
                    .compact();
        } catch (JwtException ex) {
            logger.error("Error generating JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Extracts the username from the provided JWT token.
     *
     * @param token the JWT token
     * @return the username contained in the token
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
     * Validates a JWT token by checking its expiration and username extraction.
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
     * Extracts claims from the given JWT token.
     *
     * @param token the JWT token
     * @return claims extracted from the token
     */
    private Claims getClaims(String token) {
        try {
            logger.debug("Extracting claims from JWT token");
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token expired while extracting claims: {}", ex.getMessage());
            throw ex;
        } catch (JwtException ex) {
            logger.error("Error extracting claims from JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Checks if the JWT token has expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
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
