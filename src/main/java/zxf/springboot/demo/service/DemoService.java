package zxf.springboot.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zxf.springboot.demo.client.ExternalClient;
import zxf.springboot.demo.model.ApiResponse;

import java.util.Map;

@Slf4j
@Service
public class DemoService {

    private final ExternalClient externalClient;
    private final DatabaseService databaseService;

    public DemoService(ExternalClient externalClient, DatabaseService databaseService) {
        this.externalClient = externalClient;
        this.databaseService = databaseService;
    }

    public ApiResponse processTask(String task, String projectId) {
        log.info("Processing task: {}, projectId: {}", task, projectId);

        // 调用外部服务
        Map<String, Object> downstream = externalClient.callExternalService(task);

        // 如果有 projectId，查询项目信息
        if (projectId != null && !projectId.isEmpty()) {
            Map<String, Object> project = databaseService.queryProjectById(projectId);
            ApiResponse response = ApiResponse.success(task, downstream);
            response.setProject(project);
            return response;
        }

        return ApiResponse.success(task, downstream);
    }

    public ApiResponse postTask(String task, Map<String, Object> body, String projectId) {
        log.info("Posting task: {}, projectId: {}", task, projectId);

        // 调用外部服务
        Map<String, Object> downstream = externalClient.postToExternalService(task, body);

        // 如果有 projectId，查询项目信息
        if (projectId != null && !projectId.isEmpty()) {
            Map<String, Object> project = databaseService.queryProjectById(projectId);
            ApiResponse response = ApiResponse.success(task, downstream);
            response.setProject(project);
            return response;
        }

        return ApiResponse.success(task, downstream);
    }

    public ApiResponse handleError(String task, int errorCode) {
        log.info("Handling error for task: {}, code: {}", task, errorCode);
        Map<String, Object> error = Map.of("code", errorCode, "message", "Error occurred");
        return ApiResponse.error(task, error);
    }
}