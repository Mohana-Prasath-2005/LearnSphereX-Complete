package com.learnspherex.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.learnspherex.common.ApiException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe ->
                        fe.getField()
                        + ": "
                        + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Failed",
                        message,
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND
        );
    }


    @ExceptionHandler({
        DuplicateResourceException.class,
        InvalidOperationException.class,
        BadRequestException.class
    })
    public ResponseEntity<ErrorResponse>
    handleBadRequestExceptions(
            RuntimeException ex,
            HttpServletRequest request) {

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorResponse>
    handleUnauthorizedOperationException(
            UnauthorizedOperationException ex,
            HttpServletRequest request) {

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.FORBIDDEN
        );
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse>
    handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.FORBIDDEN
        );
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse>
    handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        ex.getStatusCode().value(),
                        ex.getStatusCode().toString(),
                        ex.getReason(),
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.valueOf(ex.getStatusCode().value())
        );
    }


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse>
    handleApiException(
            ApiException ex,
            HttpServletRequest request) {

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        ex.status().value(),
                        "Application Error",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                ex.status()
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}