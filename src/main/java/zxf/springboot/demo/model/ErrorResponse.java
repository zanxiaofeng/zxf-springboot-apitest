package zxf.springboot.demo.model;

import lombok.Builder;
import lombok.Data;

/**
 * Standard error response for API errors.
 */
@Data
@Builder
public class ErrorResponse {
    private String error;
    private String id;
}