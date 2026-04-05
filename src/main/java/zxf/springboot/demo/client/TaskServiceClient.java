package zxf.springboot.demo.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zxf.springboot.demo.client.model.ExternalTask;
import zxf.springboot.demo.trace.OutboundLoggingInterceptor;

import java.time.Duration;
import java.util.Map;

/**
 * Client for downstream task-service (async task processor).
 * The task-service processes tasks asynchronously and tracks their status.
 */
@Slf4j
@Component
public class TaskServiceClient {
    private final RestTemplate restTemplate;
    private final String taskServiceUrl;

    public TaskServiceClient(RestTemplateBuilder builder, OutboundLoggingInterceptor interceptor,
                             @Value("${task-service.url:http://localhost:8090}") String taskServiceUrl) {
        this.taskServiceUrl = taskServiceUrl;
        this.restTemplate = builder
                .additionalInterceptors(interceptor)
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
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

        ResponseEntity<ExternalTask> response = restTemplate.exchange(url, HttpMethod.POST, request, ExternalTask.class);
        return response.getBody();
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

        String url = taskServiceUrl + "/tasks/{id}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of(
                        "name", StringUtils.defaultString(name),
                        "priority", ObjectUtils.defaultIfNull(priority, 0)
                ),
                headers
        );

        ResponseEntity<ExternalTask> response = restTemplate.exchange(url, HttpMethod.PUT, request, ExternalTask.class, taskId);
        return response.getBody();
    }

    /**
     * Delete task from downstream task-service.
     *
     * @param taskId the task ID
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteTask(String taskId) {
        log.info("Calling task-service to delete task: id={}", taskId);

        String url = taskServiceUrl + "/tasks/{id}";

        restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class, taskId);
        return true;
    }
}