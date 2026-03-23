package zxf.springboot.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.model.Task;
import zxf.springboot.demo.model.TaskRequest;
import zxf.springboot.demo.service.TaskService;

import java.util.List;

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
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("::createTask - name: {}, projectId: {}", request.getName(), request.getProjectId());

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
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        log.info("::getTaskById - id: {}", id);
        Task task = taskService.getTaskById(id);
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
    public ResponseEntity<Task> updateTask(@PathVariable String id, @Valid @RequestBody TaskRequest request) {
        log.info("::updateTask - id: {}, name: {}", id, request.getName());
        Task task = taskService.updateTask(id, request.getName(), request.getPriority());
        return ResponseEntity.ok(task);
    }

    /**
     * DELETE /api/tasks/{id} - Delete a task
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        log.info("::deleteTask - id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}