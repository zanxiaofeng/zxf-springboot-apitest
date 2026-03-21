package zxf.springboot.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.model.Task;
import zxf.springboot.demo.model.TaskRequest;
import zxf.springboot.demo.service.DatabaseService;
import zxf.springboot.demo.service.TaskService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DemoController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private DatabaseService databaseService;

    public DemoController() {
        log.info("::ctor - DemoController initialized");
    }

    // ==================== Task Endpoints ====================

    /**
     * POST /api/task - Create a new task
     */
    @PostMapping("/task")
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) {
        log.info("::createTask - name: {}, projectId: {}", request.getName(), request.getProjectId());

        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "name is required"));
        }

        Task task = taskService.createTask(
            request.getName(),
            request.getProjectId(),
            request.getPriority()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    /**
     * GET /api/task/{id} - Query task status
     */
    @GetMapping("/task/{id}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String id) {
        log.info("::getTaskStatus - id: {}", id);

        try {
            Task task = taskService.getTaskStatus(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found", "id", id));
        }
    }

    /**
     * GET /api/tasks - Get all tasks
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        log.info("::getAllTasks");
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // ==================== Project CRUD Endpoints ====================

    @GetMapping("/projects")
    public ResponseEntity<List<Map<String, Object>>> getAllProjects() {
        log.info("::getAllProjects");
        List<Map<String, Object>> projects = databaseService.findAllProjects();
        return ResponseEntity.ok(projects);
    }

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

    @PostMapping("/projects")
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String name = request.get("name");
        log.info("::createProject - id: {}, name: {}", id, name);

        if (id == null || name == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "id and name are required"));
        }

        if (databaseService.queryProjectById(id) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Project already exists", "id", id));
        }

        databaseService.insertProject(id, name);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(databaseService.queryProjectById(id));
    }

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