package com.example.LoginDemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle home page redirection.
 */
@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * Handles the root URL ("/") and redirects to the index page.
     *
     * @return a redirect string to index.html
     */
    @GetMapping("/")
    public String home() {
        try {
            logger.debug("Processing home page redirection.");
            logger.info("Redirecting to index.html");
            return "redirect:/index.html";
        } catch (Exception e) {
            logger.error("Error while redirecting to index.html: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to redirect to the home page.");
        }
    }
}
