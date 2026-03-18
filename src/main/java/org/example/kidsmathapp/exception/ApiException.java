package org.example.kidsmathapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = "INTERNAL_ERROR";
    }

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = status.name();
    }

    public ApiException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public ApiException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
        this.code = status.name();
    }

    public ApiException(String message, Throwable cause, HttpStatus status, String code) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public static ApiException notFound(String message) {
        return new ApiException(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public static ApiException badRequest(String message) {
        return new ApiException(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public static ApiException forbidden(String message) {
        return new ApiException(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public static ApiException conflict(String message) {
        return new ApiException(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}
