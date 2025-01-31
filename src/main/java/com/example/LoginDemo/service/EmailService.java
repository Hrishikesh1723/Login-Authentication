package com.example.LoginDemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails, such as magic login links.
 */
@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a magic login link to the given email.
     *
     * @param email the recipient's email address
     * @param token the authentication token to be included in the link
     */
    public void sendMagicLink(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Login Link");
            // Constructing the login link with the token
            String loginUrl = "http://localhost:8083/index.html?token=" + token + "&email=" + email;
            message.setText("Click here to login: " + loginUrl);

            mailSender.send(message);
            logger.info("Magic link sent successfully to {}", email);
        } catch (MailException e) {
            logger.error("Failed to send magic link to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Email sending failed. Please try again later.");
        }
    }
}
