package zxf.springboot.demo.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class TaskServiceClient {
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestTemplate restTemplate;

    @Value("${task-service.url:http://localhost:8090}")
    private String taskServiceUrl;

    public TaskServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Create task in downstream task-service
     */
    public Map<String, Object> createTask(String taskId, String name, String projectId, Integer priority) {
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
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, MAP_TYPE);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call task-service", e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Get task status from downstream task-service
     */
    public Map<String, Object> getTaskStatus(String taskId) {
        log.info("Calling task-service to get status: id={}", taskId);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                taskServiceUrl + "/tasks/{id}/status",
                HttpMethod.GET,
                null,
                MAP_TYPE,
                taskId
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call task-service", e);
            return Map.of("error", e.getMessage());
        }
    }
}