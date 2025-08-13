package com.moviebookingapp.service;

import com.moviebookingapp.domain.User;
import com.moviebookingapp.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLoginId("john123");
        testUser.setEmail("john@example.com");
        testUser.setPasswordHash("encodedPass");

        userService.setPasswordEncoder(passwordEncoder);
    }

    // ---------- register() tests ----------

    @Test
    void register_Success() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet());
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User saved = userService.register(testUser, "password", "password");

        assertNotNull(saved);
        assertEquals("john123", saved.getLoginId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_PasswordMismatch_ThrowsException() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register(testUser, "pass1", "pass2"));
        assertEquals("Password and Confirm Password must match", ex.getMessage());
    }

    @Test
    void register_ValidationFails_ThrowsException() {
        ConstraintViolation<User> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<User>> violations = Set.of(violation);
        when(validator.validate(any(User.class))).thenReturn(violations);

        assertThrows(ConstraintViolationException.class,
                () -> userService.register(testUser, "password", "password"));
    }

    @Test
    void register_LoginIdExists_ThrowsException() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet());
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> userService.register(testUser, "password", "password"));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet());
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> userService.register(testUser, "password", "password"));
    }

    // ---------- login() tests ----------

    @Test
    void login_Success() {
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPass")).thenReturn(true);

        assertTrue(userService.login("john123", "password"));
    }

    @Test
    void login_UserNotFound_ReturnsFalse() {
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.empty());

        assertFalse(userService.login("john123", "password"));
    }

    @Test
    void login_WrongPassword_ReturnsFalse() {
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertFalse(userService.login("john123", "wrong"));
    }

    // ---------- resetPassword() tests ----------

    @Test
    void resetPassword_Success() {
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        assertDoesNotThrow(() ->
                userService.resetPassword("john123", "oldPass", "newPass", "newPass"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_PasswordMismatch_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword("john123", "oldPass", "newPass", "wrongConfirm"));
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword("john123", "oldPass", "newPass", "newPass"));
    }

    @Test
    void resetPassword_WrongOldPassword_ThrowsException() {
        when(userRepository.findByLoginId("john123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOld", "encodedPass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword("john123", "wrongOld", "newPass", "newPass"));
    }
}
