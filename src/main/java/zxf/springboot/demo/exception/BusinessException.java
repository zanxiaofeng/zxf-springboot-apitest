package zxf.springboot.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom business exception for application-specific errors.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public BusinessException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public static BusinessException notFound(String resource, String id) {
        return new BusinessException(
            resource + " not found",
            "NOT_FOUND",
            HttpStatus.NOT_FOUND
        );
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(
            message,
            "CONFLICT",
            HttpStatus.CONFLICT
        );
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(
            message,
            "BAD_REQUEST",
            HttpStatus.BAD_REQUEST
        );
    }
}