package com.moviebookingapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class MovieBookingApplication {

    public static void main(String[] args) {
        log.info("Starting Movie Booking Application...");
        
        try {
            SpringApplication.run(MovieBookingApplication.class, args);
            log.info("Movie Booking Application started successfully!");
        } catch (Exception e) {
            log.error("Failed to start Movie Booking Application: {}", e.getMessage(), e);
            throw e;
        }
    }
} 