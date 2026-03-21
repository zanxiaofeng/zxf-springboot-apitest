package zxf.springboot.demo.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class TaskServiceClient {

    private final RestTemplate restTemplate;

    @Value("${task-service.url:http://localhost:8090}")
    private String taskServiceUrl;

    public TaskServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Create task in downstream task-service
     */
    public Map<String, Object> createTask(String name, String projectId, Integer priority) {
        log.info("Calling task-service to create task: {}", name);

        String url = taskServiceUrl + "/tasks";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
            Map.of(
                "name", name,
                "projectId", StringUtils.defaultString(projectId),
                "priority", ObjectUtils.defaultIfNull(priority, 0)
            ),
            headers
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call task-service", e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Get task status from downstream task-service
     */
    public Map<String, Object> getTaskStatus(String taskName) {
        log.info("Calling task-service to get status: {}", taskName);

        String url = taskServiceUrl + "/tasks/status?name=" + taskName;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call task-service", e);
            return Map.of("error", e.getMessage());
        }
    }
}