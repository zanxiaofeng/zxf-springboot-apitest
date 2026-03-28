package zxf.springboot.demo.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * External task information returned from downstream task-service.
 * The downstream service acts as an async task processor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalTask {
    private String taskId;
}