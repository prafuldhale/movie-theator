package com.moviebookingapp.service;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Validator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private static final String TEST_PASSWORD = "testPassword123";
    private static final String TEST_LOGIN_ID = "testuser";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        userService.setPasswordEncoder(passwordEncoder);
        
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email(TEST_EMAIL)
                .loginId(TEST_LOGIN_ID)
                .contactNumber("1234567890")
                .build();
    }

    @Test
    void register_Success() {
        // Arrange
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet());
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.register(testUser, TEST_PASSWORD, TEST_PASSWORD);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_PasswordMismatch() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.register(testUser, TEST_PASSWORD, "differentPassword"));
    }

    @Test
    void register_EmptyPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.register(testUser, "", TEST_PASSWORD));
    }

    @Test
    void register_LoginIdAlreadyExists() {
        // Arrange
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.register(testUser, TEST_PASSWORD, TEST_PASSWORD));
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.register(testUser, TEST_PASSWORD, TEST_PASSWORD));
    }

    @Test
    void register_ValidationFailure() {
        // Arrange
        Set<ConstraintViolation<User>> violations = Collections.singleton(mock(ConstraintViolation.class));
        when(validator.validate(any(User.class))).thenReturn(violations);

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> 
            userService.register(testUser, TEST_PASSWORD, TEST_PASSWORD));
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPasswordHash())).thenReturn(true);

        // Act
        boolean result = userService.login(TEST_LOGIN_ID, TEST_PASSWORD);

        // Assert
        assertTrue(result);
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.login(TEST_LOGIN_ID, TEST_PASSWORD);

        // Assert
        assertFalse(result);
    }

    @Test
    void login_WrongPassword() {
        // Arrange
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPasswordHash())).thenReturn(false);

        // Act
        boolean result = userService.login(TEST_LOGIN_ID, TEST_PASSWORD);

        // Assert
        assertFalse(result);
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        testUser.setPasswordHash("oldEncodedPassword");
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.resetPassword(TEST_LOGIN_ID, "oldPassword", "newPassword", "newPassword");

        // Assert
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_UserNotFound() {
        // Arrange
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.resetPassword(TEST_LOGIN_ID, "oldPassword", "newPassword", "newPassword"));
    }

    @Test
    void resetPassword_WrongOldPassword() {
        // Arrange
        testUser.setPasswordHash("oldEncodedPassword");
        when(userRepository.findByLoginId(TEST_LOGIN_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOldPassword", "oldEncodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.resetPassword(TEST_LOGIN_ID, "wrongOldPassword", "newPassword", "newPassword"));
    }

    @Test
    void resetPassword_PasswordMismatch() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.resetPassword(TEST_LOGIN_ID, "oldPassword", "newPassword", "differentPassword"));
    }
} 