package com.example.LoginDemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class for managing authentication and authorization.
 */
@Configuration
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Configures security settings such as disabling CSRF, defining authorization rules,
     * and setting session management to stateless for JWT-based authentication.
     *
     * @param http the HttpSecurity configuration object
     * @return a SecurityFilterChain instance
     * @throws Exception in case of security configuration errors
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        try {
            logger.info("Configuring security filter chain");
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            logger.info("Security filter chain configured successfully");
            return http.build();
        } catch (Exception ex) {
            logger.error("Error configuring security filter chain: {}", ex.getMessage(), ex);
            throw new IllegalArgumentException("Error configuring security filter"); // Rethrow the exception to let Spring handle it
        }
    }
}
