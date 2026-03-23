package zxf.springboot.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zxf.springboot.demo.client.TaskServiceClient;
import zxf.springboot.demo.exception.BusinessException;
import zxf.springboot.demo.model.ExternalTask;
import zxf.springboot.demo.model.Task;

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
     * The task is stored locally and submitted to the async processor.
     */
    public Task createTask(String name, String projectId, Integer priority) {
        String id = UUID.randomUUID().toString();
        String status = "PENDING";

        log.info("Creating task: id={}, name={}, projectId={}", id, name, projectId);

        // Call downstream async processor first
        ExternalTask externalTask = taskServiceClient.createTask(id, name, projectId, priority);
        String externalTaskId = externalTask != null ? externalTask.getTaskId() : null;

        // Store locally with external reference
        jdbcTemplate.update(
            "INSERT INTO task (id, name, status, project_id, priority, external_task_id) " +
            "VALUES (:id, :name, :status, :projectId, :priority, :externalTaskId)",
            Map.of(
                "id", id,
                "name", name,
                "status", status,
                "projectId", StringUtils.defaultString(projectId),
                "priority", ObjectUtils.defaultIfNull(priority, 0),
                "externalTaskId", StringUtils.defaultString(externalTaskId)
            )
        );

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

        // Update downstream
        taskServiceClient.updateTask(id, name, priority);

        // Update local
        jdbcTemplate.update(
            "UPDATE task SET name = :name, priority = :priority, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            Map.of(
                "id", id,
                "name", StringUtils.defaultString(name),
                "priority", ObjectUtils.defaultIfNull(priority, 0)
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
    public boolean deleteTask(String id) {
        log.info("Deleting task: id={}", id);

        // Check if exists - will throw BusinessException if not found
        getTaskById(id);

        // Delete from downstream
        taskServiceClient.deleteTask(id);

        // Delete from local
        jdbcTemplate.update(
            "DELETE FROM task WHERE id = :id",
            Map.of("id", id)
        );

        return true;
    }
}