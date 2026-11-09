package fu.se686868.reservation.common;

import org.springframework.http.HttpStatus;

/** Tách HTTP code khỏi business status của ApiResponseDTO. */
public class BusinessException extends RuntimeException {
    private final HttpStatus http;
    private final int status;

    public BusinessException(HttpStatus http, int status, String message) {
        super(message);
        this.http = http;
        this.status = status;
    }

    public HttpStatus http() {
        return http;
    }

    public int status() {
        return status;
    }
}
