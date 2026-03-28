package zxf.springboot.demo.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task entity representing a task in the system.
 * Tasks are processed asynchronously by the downstream task-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    private String id;
    private String name;
    private String status;
    private String projectId;
    private Integer priority;
    /**
     * External task ID returned from downstream task-service.
     * Used to correlate with the async processor.
     */
    private String externalTaskId;
}