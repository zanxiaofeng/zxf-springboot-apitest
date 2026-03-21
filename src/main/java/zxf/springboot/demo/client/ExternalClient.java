package zxf.springboot.demo.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class ExternalClient {

    private final RestTemplate restTemplate;

    @Value("${external-service.url:http://localhost:8090}")
    private String externalServiceUrl;

    public ExternalClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> callExternalService(String task) {
        log.info("Calling external service with task: {}", task);

        String url = externalServiceUrl + "/external/api?task=" + task;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            log.info("External service response status: {}", response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call external service", e);
            return Map.of("error", e.getMessage());
        }
    }

    public Map<String, Object> postToExternalService(String task, Map<String, Object> body) {
        log.info("Posting to external service with task: {}", task);

        String url = externalServiceUrl + "/external/api";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            log.info("External service response status: {}", response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to post to external service", e);
            return Map.of("error", e.getMessage());
        }
    }
}