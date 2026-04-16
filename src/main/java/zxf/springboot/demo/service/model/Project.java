package zxf.springboot.demo.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Project entity representing a project in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    private String id;
    private String name;
    private String details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}