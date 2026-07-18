package fu.se123456.department.common;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final int apiStatus;
    private final HttpStatus httpStatus;

    public BusinessException(int apiStatus, HttpStatus httpStatus, String message) {
        super(message);
        this.apiStatus = apiStatus;
        this.httpStatus = httpStatus;
    }
    public int getApiStatus() { return apiStatus; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
