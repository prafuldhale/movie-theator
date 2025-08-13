package com.moviebookingapp.service;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final Validator validator;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User register(@Valid User user, String rawPassword, String confirmPassword) {
        log.debug("Starting user registration for loginId: {}", user.getLoginId());
        
        if (!StringUtils.hasText(rawPassword) || !rawPassword.equals(confirmPassword)) {
            log.warn("Password mismatch during registration for loginId: {}", user.getLoginId());
            throw new IllegalArgumentException("Password and Confirm Password must match");
        }
        
        log.debug("Encoding password for user: {}", user.getLoginId());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        
        log.debug("Validating user data for loginId: {}", user.getLoginId());
        var violations = validator.validate(user);
        if (!violations.isEmpty()) {
            log.warn("Validation failed for user registration - loginId: {}, violations: {}", 
                    user.getLoginId(), violations);
            throw new jakarta.validation.ConstraintViolationException(violations);
        }
        
        log.debug("Checking if loginId already exists: {}", user.getLoginId());
        if (userRepository.findByLoginId(user.getLoginId()).isPresent()) {
            log.warn("LoginId already exists: {}", user.getLoginId());
            throw new IllegalArgumentException("Login Id must be unique");
        }
        
        log.debug("Checking if email already exists: {}", user.getEmail());
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email must be unique");
        }
        
        log.debug("Saving user to database: {}", user.getLoginId());
        User savedUser = userRepository.save(user);
        log.info("User registered successfully - loginId: {}, userId: {}", user.getLoginId(), savedUser.getId());
        
        return savedUser;
    }

    public boolean login(String loginId, String rawPassword) {
        log.debug("Attempting login for loginId: {}", loginId);
        
        Optional<User> userOpt = userRepository.findByLoginId(loginId);
        if (userOpt.isEmpty()) {
            log.warn("Login failed - user not found: {}", loginId);
            return false;
        }
        
        User user = userOpt.get();
        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        
        if (passwordMatches) {
            log.info("Login successful for loginId: {}", loginId);
        } else {
            log.warn("Login failed - invalid password for loginId: {}", loginId);
        }
        
        return passwordMatches;
    }

    public void resetPassword(String loginId, String oldPassword, String newPassword, String confirmPassword) {
        log.debug("Starting password reset for loginId: {}", loginId);
        
        if (!newPassword.equals(confirmPassword)) {
            log.warn("Password mismatch during reset for loginId: {}", loginId);
            throw new IllegalArgumentException("Password and Confirm Password must match");
        }
        
        log.debug("Finding user by loginId: {}", loginId);
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.warn("User not found for password reset: {}", loginId);
                    return new IllegalArgumentException("User not found");
                });
        
        log.debug("Verifying old password for loginId: {}", loginId);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            log.warn("Invalid old password for loginId: {}", loginId);
            throw new IllegalArgumentException("Old password is incorrect");
        }
        
        log.debug("Encoding new password for loginId: {}", loginId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        
        log.debug("Saving updated user: {}", loginId);
        userRepository.save(user);
        log.info("Password reset successful for loginId: {}", loginId);
    }
} 