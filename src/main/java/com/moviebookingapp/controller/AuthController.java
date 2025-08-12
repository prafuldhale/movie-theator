package com.moviebookingapp.controller;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.dto.LoginRequestDTO;
import com.moviebookingapp.dto.PasswordResetDTO;
import com.moviebookingapp.dto.UserRegistrationDTO;
import com.moviebookingapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegistrationDTO request) {
        return ResponseEntity.ok(userService.register(
            User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .contactNumber(request.getContactNumber())
                .build(),
            request.getPassword(),
            request.getConfirmPassword()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO request) {
        boolean authenticated = userService.login(request.getLoginId(), request.getPassword());
        if (!authenticated) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid credentials"));
        }
        // Generate and return token
        return ResponseEntity.ok(Collections.singletonMap("token", "dummy-token-" + request.getLoginId()));
    }

    @PutMapping("/forgot")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDTO request) {
        userService.resetPassword(request.getLoginId(), request.getPassword(), request.getPassword(), request.getConfirmPassword());
        return ResponseEntity.ok().build();
    }
}
