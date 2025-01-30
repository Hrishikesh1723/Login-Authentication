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
     * Redirects to the index page.
     *
     * @return a redirect string to index.html
     */
    @GetMapping("/")
    public String home() {
        logger.info("Redirecting to index.html");
        return "redirect:/index.html";
    }
}