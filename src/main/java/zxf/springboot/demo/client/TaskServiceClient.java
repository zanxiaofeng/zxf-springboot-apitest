package zxf.springboot.demo.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
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
    private final RestClient restClient;
    private final String taskServiceUrl;

    public TaskServiceClient(RestClient.Builder builder, OutboundLoggingInterceptor interceptor,
                             @Value("${task-service.url:http://localhost:8090}") String taskServiceUrl) {
        this.taskServiceUrl = taskServiceUrl;
        this.restClient = builder
                .requestInterceptor(interceptor)
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }

    /**
     * Create task in downstream task-service (async processor).
     */
    public ExternalTask createTask(String taskId, String name, String projectId, Integer priority) {
        log.info("Calling task-service to create task: id={}, name={}", taskId, name);

        return restClient.post()
                .uri(taskServiceUrl + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "id", taskId,
                        "name", name,
                        "projectId", StringUtils.defaultString(projectId),
                        "priority", ObjectUtils.defaultIfNull(priority, 0)
                ))
                .retrieve()
                .body(ExternalTask.class);
    }

    /**
     * Update task in downstream task-service.
     */
    public ExternalTask updateTask(String taskId, String name, Integer priority) {
        log.info("Calling task-service to update task: id={}, name={}", taskId, name);

        return restClient.put()
                .uri(taskServiceUrl + "/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "name", StringUtils.defaultString(name),
                        "priority", ObjectUtils.defaultIfNull(priority, 0)
                ))
                .retrieve()
                .body(ExternalTask.class);
    }

    /**
     * Delete task from downstream task-service.
     */
    public boolean deleteTask(String taskId) {
        log.info("Calling task-service to delete task: id={}", taskId);

        restClient.delete()
                .uri(taskServiceUrl + "/tasks/{id}", taskId)
                .retrieve()
                .toBodilessEntity();
        return true;
    }
}
