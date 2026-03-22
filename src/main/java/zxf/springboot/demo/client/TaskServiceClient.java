package zxf.springboot.demo.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zxf.springboot.demo.model.ExternalTask;

import java.util.Map;

/**
 * Client for downstream task-service (async task processor).
 * The task-service processes tasks asynchronously and tracks their status.
 */
@Slf4j
@Component
public class TaskServiceClient {
    private static final ParameterizedTypeReference<ExternalTask> EXTERNAL_TASK_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestTemplate restTemplate;

    @Value("${task-service.url:http://localhost:8090}")
    private String taskServiceUrl;

    public TaskServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Create task in downstream task-service (async processor).
     *
     * @param taskId    local task ID (passed to downstream for correlation)
     * @param name      task name
     * @param projectId project ID
     * @param priority  task priority
     * @return ExternalTask with taskId and status, or null if failed
     */
    public ExternalTask createTask(String taskId, String name, String projectId, Integer priority) {
        log.info("Calling task-service to create task: id={}, name={}", taskId, name);

        String url = taskServiceUrl + "/tasks";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
            Map.of(
                "id", taskId,
                "name", name,
                "projectId", StringUtils.defaultString(projectId),
                "priority", ObjectUtils.defaultIfNull(priority, 0)
            ),
            headers
        );

        try {
            ResponseEntity<ExternalTask> response = restTemplate.exchange(
                url, HttpMethod.POST, request, EXTERNAL_TASK_TYPE);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call task-service to create task: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Update task in downstream task-service.
     *
     * @param taskId   the task ID
     * @param name     new task name
     * @param priority new priority
     * @return ExternalTask with updated info, or null if failed
     */
    public ExternalTask updateTask(String taskId, String name, Integer priority) {
        log.info("Calling task-service to update task: id={}, name={}", taskId, name);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
            Map.of(
                "name", StringUtils.defaultString(name),
                "priority", ObjectUtils.defaultIfNull(priority, 0)
            ),
            headers
        );

        try {
            ResponseEntity<ExternalTask> response = restTemplate.exchange(
                taskServiceUrl + "/tasks/{id}",
                HttpMethod.PUT,
                request,
                EXTERNAL_TASK_TYPE,
                taskId
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call task-service to update task: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete task from downstream task-service.
     *
     * @param taskId the task ID
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteTask(String taskId) {
        log.info("Calling task-service to delete task: id={}", taskId);

        try {
            restTemplate.exchange(
                taskServiceUrl + "/tasks/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                taskId
            );
            return true;
        } catch (Exception e) {
            log.error("Failed to call task-service to delete task: {}", e.getMessage());
            return false;
        }
    }
}