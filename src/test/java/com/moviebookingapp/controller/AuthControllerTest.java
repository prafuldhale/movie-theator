package com.moviebookingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebookingapp.domain.User;
import com.moviebookingapp.dto.LoginRequestDTO;
import com.moviebookingapp.dto.PasswordResetDTO;
import com.moviebookingapp.dto.UserRegistrationDTO;
import com.moviebookingapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserRegistrationDTO registrationDTO;
    private LoginRequestDTO loginDTO;
    private PasswordResetDTO passwordResetDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .loginId("johndoe")
                .contactNumber("1234567890")
                .build();

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setFirstName("John");
        registrationDTO.setLastName("Doe");
        registrationDTO.setEmail("john.doe@example.com");
        registrationDTO.setLoginId("johndoe");
        registrationDTO.setContactNumber("1234567890");
        registrationDTO.setPassword("Password123@");
        registrationDTO.setConfirmPassword("Password123@");

        loginDTO = new LoginRequestDTO();
        loginDTO.setLoginId("johndoe");
        loginDTO.setPassword("Password123@");

        passwordResetDTO = new PasswordResetDTO();
        passwordResetDTO.setLoginId("johndoe");
        passwordResetDTO.setPassword("NewPassword123@");
        passwordResetDTO.setConfirmPassword("NewPassword123@");
    }

    @Test
    void register_Success() throws Exception {
        // Arrange
        when(userService.register(any(User.class), anyString(), anyString())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.loginId").value("johndoe"));
    }

    @Test
    void register_ValidationFailure() throws Exception {
        // Arrange
        registrationDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ServiceException() throws Exception {
        // Arrange
        when(userService.register(any(User.class), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Login Id must be unique"));

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        when(userService.login("johndoe", "Password123@")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-token-johndoe"));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        // Arrange
        when(userService.login("johndoe", "wrongpassword")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_ValidationFailure() throws Exception {
        // Arrange
        loginDTO.setLoginId("");

        // Act & Assert
        mockMvc.perform(post("/api/v1.0/moviebooking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1.0/moviebooking/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_ValidationFailure() throws Exception {
        // Arrange
        passwordResetDTO.setLoginId("");

        // Act & Assert
        mockMvc.perform(put("/api/v1.0/moviebooking/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_ServiceException() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).resetPassword(anyString(), anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(put("/api/v1.0/moviebooking/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetDTO)))
                .andExpect(status().isBadRequest());
    }
} 