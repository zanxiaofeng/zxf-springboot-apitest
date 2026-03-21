package zxf.springboot.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import zxf.springboot.demo.client.TaskServiceClient;
import zxf.springboot.demo.model.Task;

import java.util.*;

@Slf4j
@Service
public class TaskService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TaskServiceClient taskServiceClient;

    public TaskService(NamedParameterJdbcTemplate jdbcTemplate, TaskServiceClient taskServiceClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskServiceClient = taskServiceClient;
    }

    /**
     * Create a new task
     */
    public Task createTask(String name, String projectId, Integer priority) {
        String id = UUID.randomUUID().toString();
        String status = "PENDING";

        log.info("Creating task: id={}, name={}, projectId={}", id, name, projectId);

        jdbcTemplate.update(
            "INSERT INTO task (id, name, status, project_id, priority) " +
            "VALUES (:id, :name, :status, :projectId, :priority)",
            Map.of(
                "id", id,
                "name", name,
                "status", status,
                "projectId", projectId != null ? projectId : "",
                "priority", priority != null ? priority : 0
            )
        );

        // Call downstream task-service
        Map<String, Object> downstreamResponse = taskServiceClient.createTask(name, projectId, priority);

        Task task = new Task();
        task.setId(id);
        task.setName(name);
        task.setStatus(status);
        task.setProjectId(projectId);
        task.setPriority(priority);
        task.setDownstreamResponse(downstreamResponse);

        return task;
    }

    /**
     * Query task status by ID
     */
    public Task getTaskStatus(String id) {
        log.info("Querying task status: id={}", id);

        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT id, name, status, project_id, priority FROM task WHERE id = :id",
            Map.of("id", id)
        );

        String taskName = (String) row.get("name");

        // Call downstream task-service to get status
        Map<String, Object> downstreamResponse = taskServiceClient.getTaskStatus(taskName);

        Task task = new Task();
        task.setId((String) row.get("id"));
        task.setName((String) row.get("name"));
        task.setStatus((String) row.get("status"));
        task.setProjectId((String) row.get("project_id"));
        task.setPriority((Integer) row.get("priority"));
        task.setDownstreamResponse(downstreamResponse);

        return task;
    }

    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        log.info("Querying all tasks");
        return jdbcTemplate.query(
            "SELECT id, name, status, project_id, priority FROM task ORDER BY name",
            (rs, rowNum) -> {
                Task task = new Task();
                task.setId(rs.getString("id"));
                task.setName(rs.getString("name"));
                task.setStatus(rs.getString("status"));
                task.setProjectId(rs.getString("project_id"));
                task.setPriority(rs.getInt("priority"));
                return task;
            }
        );
    }
}