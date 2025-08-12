package com.moviebookingapp.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    private RestExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new RestExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleValidation_Success() {
        // Arrange
        FieldError fieldError = new FieldError("object", "field", "default message");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleValidation(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    void handleConstraintViolation_Success() {
        // Arrange
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("field");
        when(violation.getMessage()).thenReturn("constraint message");
        
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        
        ConstraintViolationException ex = new ConstraintViolationException("message", violations);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleConstraint(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Constraint violation", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentTypeMismatch_Success() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getMessage()).thenReturn("Type mismatch error");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleBadArgs(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Type mismatch error", response.getBody().getMessage());
    }

    @Test
    void handleMissingServletRequestParameter_Success() {
        // Arrange
        MissingServletRequestParameterException ex = mock(MissingServletRequestParameterException.class);
        when(ex.getMessage()).thenReturn("Missing parameter error");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleBadArgs(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Missing parameter error", response.getBody().getMessage());
    }

    @Test
    void handleHttpMessageNotReadable_Success() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(null);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleBadJson(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Malformed JSON request", response.getBody().getMessage());
    }

    @Test
    void handleHttpMessageNotReadable_WithInvalidFormatException() {
        // Arrange
        InvalidFormatException ife = mock(InvalidFormatException.class);
        JsonMappingException.Reference ref = mock(JsonMappingException.Reference.class);
        when(ref.getFieldName()).thenReturn("field");
        when(ife.getPath()).thenReturn(Collections.singletonList(ref));
        
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(ife);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleBadJson(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid value for field 'field'", response.getBody().getMessage());
    }

    @Test
    void handleDataIntegrityViolation_Success() {
        // Arrange
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Throwable cause = mock(Throwable.class);
        when(cause.getMessage()).thenReturn("Database constraint error");
        when(ex.getMostSpecificCause()).thenReturn(cause);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleDataIntegrity(ex, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Data integrity violation", response.getBody().getMessage());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    void handleEntityNotFound_Success() {
        // Arrange
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleNotFound(ex, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Entity not found", response.getBody().getMessage());
    }

    @Test
    void handleIllegalStateException_Success() {
        // Arrange
        IllegalStateException ex = new IllegalStateException("Illegal state");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleNotFound(ex, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Illegal state", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgumentException_Success() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleIllegal(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_Success() {
        // Arrange
        RuntimeException ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleGeneric(ex, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    void buildApiError_WithDetails() {
        // Arrange
        String message = "Test message";
        String path = "/api/test";
        String detailsKey = "testKey";
        String detailsValue = "testValue";

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleGeneric(
            new RuntimeException("Test"), request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }
} 