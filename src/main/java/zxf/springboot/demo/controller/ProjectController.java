package zxf.springboot.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.service.DatabaseService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final DatabaseService databaseService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProjects() {
        log.info("::getAllProjects");
        List<Map<String, Object>> projects = databaseService.findAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable String id) {
        log.info("::getProjectById - id: {}", id);
        Map<String, Object> project = databaseService.queryProjectById(id);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Project not found", "id", id));
        }
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String name = request.get("name");
        log.info("::createProject - id: {}, name: {}", id, name);

        if (StringUtils.isBlank(id) || StringUtils.isBlank(name)) {
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable String id,
                                          @RequestBody Map<String, String> request) {
        String name = request.get("name");
        log.info("::updateProject - id: {}, name: {}", id, name);

        if (StringUtils.isBlank(name)) {
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

    @DeleteMapping("/{id}")
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