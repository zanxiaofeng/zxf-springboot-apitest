package zxf.springboot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a project.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    private String name;
}