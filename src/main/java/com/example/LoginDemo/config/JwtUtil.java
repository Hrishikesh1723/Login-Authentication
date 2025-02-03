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

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

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

    public String extractRole(String token) {
        try {
            logger.debug("Extracting role from JWT token");
            return getClaims(token).get("role", String.class);
        } catch (JwtException ex) {
            logger.error("Error extracting role from JWT token: {}", ex.getMessage());
            throw ex;
        }
    }

    public boolean isValidToken(String token) {
        try {
            logger.debug("Validating JWT token");
            return extractUsername(token) != null && !isTokenExpired(token);
        } catch (JwtException ex) {
            logger.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

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