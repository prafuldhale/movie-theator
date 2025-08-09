package com.moviebookingapp.controller;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegistrationRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .contactNumber(request.getContactNumber())
                .build();
        return ResponseEntity.ok(userService.register(user, request.getPassword(), request.getConfirmPassword()));
    }

    @GetMapping("/login")
    public ResponseEntity<Boolean> login(@RequestParam String loginId, @RequestParam String password) {
        return ResponseEntity.ok(userService.login(loginId, password));
    }

    @GetMapping("/{username}/forgot")
    public ResponseEntity<String> forgot(@PathVariable("username") String username) {
        // In a real app: trigger email with reset token
        return ResponseEntity.ok("Password reset link sent if user exists");
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@RequestParam String loginId,
                                      @RequestParam String oldPassword,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword) {
        userService.resetPassword(loginId, oldPassword, newPassword, confirmPassword);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class RegistrationRequest {
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String loginId;
        @NotBlank
        @Size(min = 8)
        private String password;
        @NotBlank
        private String confirmPassword;
        @NotBlank
        private String contactNumber;
    }
} 