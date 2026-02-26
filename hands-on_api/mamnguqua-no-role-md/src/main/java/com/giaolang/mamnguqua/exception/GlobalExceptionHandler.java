package com.giaolang.mamnguqua.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler - Xử lý tất cả exceptions toàn ứng dụng
 *
 * Tạo error responses thống nhất cho tất cả endpoints
 * Được apply trên tất cả @RestController
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     * <p>
     * Khi resource không tìm thấy (HTTP 404)
     *
     * @param exception ResourceNotFoundException
     * @param request   WebRequest
     * @return ErrorResponse với status 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("NOT_FOUND")
                .message(exception.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle validation exceptions
     * <p>
     * Khi request body validation thất bại (HTTP 400)
     *
     * @param exception MethodArgumentNotValidException
     * @param request   WebRequest
     * @return ErrorResponse với status 400 và chi tiết validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception, WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Validation failed")
                .details(validationErrors.toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions
     * <p>
     * Catch-all handler cho những exception không được handle riêng
     *
     * @param exception Exception
     * @param request   WebRequest
     * @return ErrorResponse với status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception exception, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .details(exception.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DTO - Error Response
     * <p>
     * Cấu trúc thống nhất cho tất cả error responses
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {

        /**
         * Thời gian error xảy ra
         */
        private LocalDateTime timestamp;

        /**
         * HTTP status code
         */
        private int status;

        /**
         * Error type (NOT_FOUND, VALIDATION_ERROR, v.v.)
         */
        private String error;

        /**
         * Error message
         */
        private String message;

        /**
         * Chi tiết lỗi (thường là validation errors)
         */
        private String details;

        /**
         * Request path
         */
        private String path;
    }
}
