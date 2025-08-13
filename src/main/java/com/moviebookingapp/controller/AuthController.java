package com.moviebookingapp.controller;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.dto.LoginRequestDTO;
import com.moviebookingapp.dto.PasswordResetDTO;
import com.moviebookingapp.dto.UserRegistrationDTO;
import com.moviebookingapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegistrationDTO request) {
        log.info("User registration request received for loginId: {}", request.getLoginId());
        log.debug("Registration details - firstName: {}, lastName: {}, email: {}",
                 request.getFirstName(), request.getLastName(), request.getEmail());

        try {
            User user = userService.register(
                User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .loginId(request.getLoginId())
                    .contactNumber(request.getContactNumber())
                    .build(),
                request.getPassword(),
                request.getConfirmPassword()
            );

            log.info("User registration successful for loginId: {}, userId: {}",
                    request.getLoginId(), user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("User registration failed for loginId: {}, error: {}",
                     request.getLoginId(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login attempt for loginId: {}", request.getLoginId());

        try {
            boolean authenticated = userService.login(request.getLoginId(), request.getPassword());
            if (!authenticated) {
                log.warn("Login failed for loginId: {} - Invalid credentials", request.getLoginId());
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid credentials"));
            }

            log.info("Login successful for loginId: {}", request.getLoginId());
            // Generate and return token
            return ResponseEntity.ok(Collections.singletonMap("token", "dummy-token-" + request.getLoginId()));
        } catch (Exception e) {
            log.error("Login error for loginId: {}, error: {}",
                     request.getLoginId(), e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/forgot")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDTO request) {
        log.info("Password reset request received for loginId: {}", request.getLoginId());

        try {
            userService.resetPassword(request.getLoginId(), request.getPassword(), request.getPassword(), request.getConfirmPassword());
            log.info("Password reset successful for loginId: {}", request.getLoginId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Password reset failed for loginId: {}, error: {}",
                     request.getLoginId(), e.getMessage(), e);
            throw e;
        }
    }
}
