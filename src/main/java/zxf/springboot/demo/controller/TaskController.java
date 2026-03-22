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
     * GET /api/tasks/{id} - Query task status
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String id) {
        log.info("::getTaskStatus - id: {}", id);

        Task task = taskService.getTaskStatus(id);
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
}