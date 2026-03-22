package zxf.springboot.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.model.Task;
import zxf.springboot.demo.model.TaskRequest;
import zxf.springboot.demo.service.TaskService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for task management.
 * Tasks are processed asynchronously by the downstream task-service.
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    /**
     * POST /api/tasks - Create a new task
     */
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) {
        log.info("::createTask - name: {}, projectId: {}", request.getName(), request.getProjectId());

        if (StringUtils.isBlank(request.getName())) {
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
     * GET /api/tasks/{id} - Get task by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable String id) {
        log.info("::getTaskById - id: {}", id);

        Task task = taskService.getTaskById(id);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found", "id", id));
        }
        return ResponseEntity.ok(task);
    }

    /**
     * GET /api/tasks - Get all tasks
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        log.info("::getAllTasks");
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * PUT /api/tasks/{id} - Update a task
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id, @RequestBody TaskRequest request) {
        log.info("::updateTask - id: {}, name: {}", id, request.getName());

        if (StringUtils.isBlank(request.getName())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "name is required"));
        }

        Task task = taskService.updateTask(id, request.getName(), request.getPriority());
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found", "id", id));
        }
        return ResponseEntity.ok(task);
    }

    /**
     * DELETE /api/tasks/{id} - Delete a task
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        log.info("::deleteTask - id: {}", id);

        boolean deleted = taskService.deleteTask(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found", "id", id));
        }
        return ResponseEntity.noContent().build();
    }
}