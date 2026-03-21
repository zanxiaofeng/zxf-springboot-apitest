package zxf.springboot.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.model.ApiResponse;
import zxf.springboot.demo.model.TaskRequest;
import zxf.springboot.demo.service.DatabaseService;
import zxf.springboot.demo.service.DemoService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DemoController {

    @Autowired
    private DemoService demoService;

    @Autowired
    private DatabaseService databaseService;

    public DemoController() {
        log.info("::ctor - DemoController initialized");
    }

    // ==================== Task Endpoints ====================

    @GetMapping("/task")
    public ApiResponse getTask(@RequestParam String task,
                               @RequestParam(required = false) String projectId) {
        log.info("::getTask - task: {}, projectId: {}", task, projectId);
        return demoService.processTask(task, projectId);
    }

    @PostMapping("/task")
    public ApiResponse postTask(@RequestBody TaskRequest request) {
        log.info("::postTask - task: {}, projectId: {}", request.getTask(), request.getProjectId());
        return demoService.postTask(request.getTask(), Map.of("priority", request.getPriority()), request.getProjectId());
    }

    // ==================== Project CRUD Endpoints ====================

    /**
     * GET /api/projects - 查询所有项目
     */
    @GetMapping("/projects")
    public ResponseEntity<List<Map<String, Object>>> getAllProjects() {
        log.info("::getAllProjects");
        List<Map<String, Object>> projects = databaseService.findAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * GET /api/projects/{id} - 根据ID查询项目
     */
    @GetMapping("/projects/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable String id) {
        log.info("::getProjectById - id: {}", id);
        Map<String, Object> project = databaseService.queryProjectById(id);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Project not found", "id", id));
        }
        return ResponseEntity.ok(project);
    }

    /**
     * POST /api/projects - 创建项目
     */
    @PostMapping("/projects")
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String name = request.get("name");
        log.info("::createProject - id: {}, name: {}", id, name);

        if (id == null || name == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "id and name are required"));
        }

        // 检查是否已存在
        if (databaseService.queryProjectById(id) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Project already exists", "id", id));
        }

        databaseService.insertProject(id, name);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(databaseService.queryProjectById(id));
    }

    /**
     * PUT /api/projects/{id} - 更新项目
     */
    @PutMapping("/projects/{id}")
    public ResponseEntity<?> updateProject(@PathVariable String id,
                                          @RequestBody Map<String, String> request) {
        String name = request.get("name");
        log.info("::updateProject - id: {}, name: {}", id, name);

        if (name == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "name is required"));
        }

        int updated = databaseService.updateProject(id, name);
        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Project not found", "id", id));
        }

        return ResponseEntity.ok(databaseService.queryProjectById(id));
    }

    /**
     * DELETE /api/projects/{id} - 删除项目
     */
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        log.info("::deleteProject - id: {}", id);

        Map<String, Object> project = databaseService.queryProjectById(id);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Project not found", "id", id));
        }

        databaseService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}