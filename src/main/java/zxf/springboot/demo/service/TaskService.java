package zxf.springboot.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zxf.springboot.demo.client.TaskServiceClient;
import zxf.springboot.demo.exception.BusinessException;
import zxf.springboot.demo.client.model.ExternalTask;
import zxf.springboot.demo.service.model.Task;

import java.util.*;

/**
 * Service for task management.
 * Tasks are created locally and processed asynchronously by the downstream task-service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TaskServiceClient taskServiceClient;

    /**
     * Create a new task.
     * The task is stored locally first, then submitted to the async processor.
     */
    @Transactional
    public Task createTask(String name, String projectId, Integer priority) {
        String id = UUID.randomUUID().toString();
        String status = "PENDING";

        log.info("Creating task: id={}, name={}, projectId={}", id, name, projectId);

        // Store locally first (PENDING status) — ensures local record exists even if downstream fails
        jdbcTemplate.update(
            "INSERT INTO task (id, name, status, project_id, priority) " +
            "VALUES (:id, :name, :status, :projectId, :priority)",
            Map.of(
                "id", id,
                "name", name,
                "status", status,
                "projectId", StringUtils.defaultString(projectId),
                "priority", Objects.requireNonNullElse(priority, 0)
            )
        );

        // Submit to downstream async processor
        ExternalTask externalTask = taskServiceClient.createTask(id, name, projectId, priority);
        String externalTaskId = externalTask != null ? externalTask.getTaskId() : null;

        // Update with external reference
        if (externalTaskId != null) {
            jdbcTemplate.update(
                "UPDATE task SET external_task_id = :externalTaskId WHERE id = :id",
                Map.of("id", id, "externalTaskId", externalTaskId)
            );
        }

        return Task.builder()
                .id(id)
                .name(name)
                .status(status)
                .projectId(projectId)
                .priority(priority)
                .externalTaskId(externalTaskId)
                .build();
    }

    /**
     * Query task by ID.
     * Returns local task data (does not query downstream to keep response fast).
     * @throws BusinessException if task not found
     */
    public Task getTaskById(String id) {
        log.info("Querying task: id={}", id);

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT id, name, status, project_id, priority, external_task_id FROM task WHERE id = :id",
                Map.of("id", id)
            );

            return Task.builder()
                    .id((String) row.get("id"))
                    .name((String) row.get("name"))
                    .status((String) row.get("status"))
                    .projectId((String) row.get("project_id"))
                    .priority((Integer) row.get("priority"))
                    .externalTaskId((String) row.get("external_task_id"))
                    .build();
        } catch (EmptyResultDataAccessException e) {
            log.warn("Task not found: {}", id);
            throw BusinessException.notFound("Task", id);
        }
    }

    /**
     * Get all tasks.
     */
    public List<Task> getAllTasks() {
        log.info("Querying all tasks");
        return jdbcTemplate.query(
            "SELECT id, name, status, project_id, priority, external_task_id FROM task ORDER BY name",
            (rs, rowNum) -> Task.builder()
                    .id(rs.getString("id"))
                    .name(rs.getString("name"))
                    .status(rs.getString("status"))
                    .projectId(rs.getString("project_id"))
                    .priority(rs.getInt("priority"))
                    .externalTaskId(rs.getString("external_task_id"))
                    .build()
        );
    }

    /**
     * Update a task.
     * Updates both local and downstream.
     * @throws BusinessException if task not found
     */
    @Transactional
    public Task updateTask(String id, String name, Integer priority) {
        log.info("Updating task: id={}, name={}", id, name);

        // Check if exists - will throw BusinessException if not found
        Task existing = getTaskById(id);

        // Update downstream — exception propagates and rolls back local transaction
        taskServiceClient.updateTask(id, name, priority);

        // Update local
        jdbcTemplate.update(
            "UPDATE task SET name = :name, priority = :priority, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            Map.of(
                "id", id,
                "name", StringUtils.defaultString(name),
                "priority", Objects.requireNonNullElse(priority, 0)
            )
        );

        return Task.builder()
                .id(id)
                .name(name)
                .status(existing.getStatus())
                .projectId(existing.getProjectId())
                .priority(priority)
                .externalTaskId(existing.getExternalTaskId())
                .build();
    }

    /**
     * Delete a task.
     * Deletes from both local and downstream.
     * @throws BusinessException if task not found
     */
    @Transactional
    public void deleteTask(String id) {
        log.info("Deleting task: id={}", id);

        // Check if exists - will throw BusinessException if not found
        getTaskById(id);

        // Delete from downstream — exception propagates and rolls back local transaction
        taskServiceClient.deleteTask(id);

        // Delete from local
        jdbcTemplate.update(
            "DELETE FROM task WHERE id = :id",
            Map.of("id", id)
        );
    }
}