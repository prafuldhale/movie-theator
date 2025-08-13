package com.moviebookingapp.controller;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.dto.LoginRequestDTO;
import com.moviebookingapp.dto.PasswordResetDTO;
import com.moviebookingapp.dto.UserRegistrationDTO;
import com.moviebookingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private UserRegistrationDTO registrationDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        registrationDTO = UserRegistrationDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .loginId("john123")
                .contactNumber("1234567890")
                .password("pass123")
                .confirmPassword("pass123")
                .build();

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .loginId("john123")
                .contactNumber("1234567890")
                .build();
    }

    @Test
    void registerSuccess() {
        when(userService.register(any(User.class), eq("pass123"), eq("pass123")))
                .thenReturn(testUser);

        ResponseEntity<User> response = authController.register(registrationDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testUser.getLoginId(), response.getBody().getLoginId());
    }

    @Test
    void registerThrowsException() {
        when(userService.register(any(User.class), eq("pass123"), eq("pass123")))
                .thenThrow(new RuntimeException("Registration failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authController.register(registrationDTO));

        assertEquals("Registration failed", exception.getMessage());
    }

    @Test
    void loginSuccess() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("john123", "pass123");
        when(userService.login("john123", "pass123")).thenReturn(true);

        ResponseEntity<Map<String, String>> response = authController.login(loginDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("dummy-token-john123", response.getBody().get("token"));
    }

    @Test
    void loginInvalidCredentials() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("john123", "wrongpass");
        when(userService.login("john123", "wrongpass")).thenReturn(false);

        ResponseEntity<Map<String, String>> response = authController.login(loginDTO);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody().get("error"));
    }

    @Test
    void loginThrowsException() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("john123", "pass123");
        when(userService.login("john123", "pass123"))
                .thenThrow(new RuntimeException("Login failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authController.login(loginDTO));

        assertEquals("Login failed", exception.getMessage());
    }

    @Test
    void resetPasswordSuccess() {
        PasswordResetDTO resetDTO = new PasswordResetDTO("john123", "newpass", "newpass");

        ResponseEntity<Void> response = authController.resetPassword(resetDTO);

        assertEquals(200, response.getStatusCodeValue());
        verify(userService).resetPassword("john123", "newpass", "newpass", "newpass");
    }

    @Test
    void resetPasswordThrowsException() {
        PasswordResetDTO resetDTO = new PasswordResetDTO("john123", "newpass", "newpass");

        doThrow(new RuntimeException("Reset failed"))
                .when(userService)
                .resetPassword("john123", "newpass", "newpass", "newpass");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authController.resetPassword(resetDTO));

        assertEquals("Reset failed", exception.getMessage());
    }
}
