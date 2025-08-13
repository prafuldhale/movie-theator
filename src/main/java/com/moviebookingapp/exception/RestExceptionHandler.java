package com.moviebookingapp.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Validation error occurred - path: {}, method: {}", request.getRequestURI(), request.getMethod());
        
        Map<String, Object> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
            log.debug("Field validation error - field: {}, message: {}", fe.getField(), fe.getDefaultMessage());
        }
        
        log.debug("Validation error details: {}", details);
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Constraint violation occurred - path: {}, method: {}", request.getRequestURI(), request.getMethod());
        
        Map<String, Object> details = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            details.put(v.getPropertyPath().toString(), v.getMessage());
            log.debug("Constraint violation - property: {}, message: {}", v.getPropertyPath(), v.getMessage());
        });
        
        log.debug("Constraint violation details: {}", details);
        return build(HttpStatus.BAD_REQUEST, "Constraint violation", request.getRequestURI(), details);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<ApiError> handleBadArgs(Exception ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Bad argument error - path: {}, method: {}, error: {}", 
                request.getRequestURI(), request.getMethod(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleBadJson(HttpMessageNotReadableException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("JSON parsing error - path: {}, method: {}", request.getRequestURI(), request.getMethod());
        
        String msg = "Malformed JSON request";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String path = ife.getPath().stream().map(JsonMappingException.Reference::getFieldName).reduce((a,b)->a+"."+b).orElse("");
            msg = "Invalid value for field '" + path + "'";
            log.debug("Invalid format error - field: {}", path);
        }
        
        return build(HttpStatus.BAD_REQUEST, msg, request.getRequestURI(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.error("Data integrity violation - path: {}, method: {}, error: {}", 
                request.getRequestURI(), request.getMethod(), ex.getMostSpecificCause().getMessage(), ex);
        return build(HttpStatus.CONFLICT, "Data integrity violation", request.getRequestURI(), 
                Map.of("reason", ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler({EntityNotFoundException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Resource not found - path: {}, method: {}, error: {}", 
                request.getRequestURI(), request.getMethod(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegal(IllegalArgumentException ex, jakarta.servlet.http.HttpServletRequest request) {
        log.warn("Illegal argument error - path: {}, method: {}, error: {}", 
                request.getRequestURI(), request.getMethod(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, jakarta.servlet.http.HttpServletRequest request) {
        log.error("Unexpected error occurred - path: {}, method: {}, error: {}", 
                request.getRequestURI(), request.getMethod(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI(), 
                Map.of("error", ex.getClass().getSimpleName()));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, Map<String, Object> details) {
        ApiError body = ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .details(details)
                .build();
        
        log.debug("Built error response - status: {}, message: {}, path: {}", status, message, path);
        return ResponseEntity.status(status).body(body);
    }
} 