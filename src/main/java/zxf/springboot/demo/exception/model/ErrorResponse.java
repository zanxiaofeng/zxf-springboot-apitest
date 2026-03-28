package zxf.springboot.demo.exception.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Standard error response for API errors.
 */
@Data
@Builder
public class ErrorResponse {
    private String error;
    private String errorCode;
    private String message;
    private String id;
    private Instant timestamp;
}