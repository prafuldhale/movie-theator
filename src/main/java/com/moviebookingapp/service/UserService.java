package com.moviebookingapp.service;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final Validator validator;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User register(@Valid User user, String rawPassword, String confirmPassword) {
        if (!StringUtils.hasText(rawPassword) || !rawPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and Confirm Password must match");
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        var violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new jakarta.validation.ConstraintViolationException(violations);
        }
        if (userRepository.findByLoginId(user.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("Login Id must be unique");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email must be unique");
        }
        return userRepository.save(user);
    }

    public boolean login(String loginId, String rawPassword) {
        Optional<User> userOpt = userRepository.findByLoginId(loginId);
        return userOpt.filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash())).isPresent();
    }

    public void resetPassword(String loginId, String oldPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and Confirm Password must match");
        }
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
} 