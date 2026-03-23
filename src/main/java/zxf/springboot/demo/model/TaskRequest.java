package zxf.springboot.demo.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be less than 100 characters")
    private String name;

    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "projectId contains invalid characters")
    private String projectId;

    @Min(value = 0, message = "priority must be >= 0")
    @Max(value = 100, message = "priority must be <= 100")
    private Integer priority;
}