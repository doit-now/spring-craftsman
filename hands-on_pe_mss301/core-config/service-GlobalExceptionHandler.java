package fu.se686868.reservation.common;

import fu.se686868.reservation.dto.ApiResponseDTO;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** GHI NHỚ: lỗi validation theo đề HTTP 406/status=2 (khác thông lệ REST thường là 400). */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> business(BusinessException e) {
        return ResponseEntity.status(e.http())
                .body(ApiResponseDTO.error(e.status(), e.getMessage()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<?> validation(Exception e) {
        return ResponseEntity.status(406)
                .body(ApiResponseDTO.error(2, "Invalid input: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception e) {
        // Không tiết lộ stack trace, SQL, mật khẩu cho client; log ở phía server.
        LoggerFactory.getLogger(getClass()).error("Unexpected error", e);
        return ResponseEntity.status(500).body(ApiResponseDTO.error(0, "Internal server error"));
    }
}
