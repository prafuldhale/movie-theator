package com.moviebookingapp.config;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.*;

class CommonConfigTest {

    @Test
    void testJavaxValidatorBean() {
        // Arrange
        CommonConfig config = new CommonConfig();

        // Act
        Validator validator = config.javaxValidator();

        // Assert
        assertNotNull(validator);
        assertTrue(validator instanceof LocalValidatorFactoryBean);
    }

    @Test
    void testPasswordEncoderBean() {
        // Arrange
        CommonConfig config = new CommonConfig();

        // Act
        BCryptPasswordEncoder passwordEncoder = (BCryptPasswordEncoder) config.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void testPasswordEncoderFunctionality() {
        // Arrange
        CommonConfig config = new CommonConfig();
        BCryptPasswordEncoder passwordEncoder = (BCryptPasswordEncoder) config.passwordEncoder();

        // Act
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        boolean wrongPassword = passwordEncoder.matches("wrongPassword", encodedPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(matches);
        assertFalse(wrongPassword);
    }

    @Test
    void testMultiplePasswordEncodings() {
        // Arrange
        CommonConfig config = new CommonConfig();
        BCryptPasswordEncoder passwordEncoder = (BCryptPasswordEncoder) config.passwordEncoder();

        // Act
        String password = "testPassword123";
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        // Assert
        // BCrypt generates different hashes for the same password due to salt
        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(password, encoded1));
        assertTrue(passwordEncoder.matches(password, encoded2));
    }
} 