package zxf.springboot.demo.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import zxf.springboot.demo.client.TaskServiceClient;
import zxf.springboot.demo.model.Task;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TaskServiceClient taskServiceClient;

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
                "projectId", StringUtils.defaultString(projectId),
                "priority", ObjectUtils.defaultIfNull(priority, 0)
            )
        );

        // Call downstream task-service
        Map<String, Object> downstreamResponse = taskServiceClient.createTask(name, projectId, priority);

        return Task.builder()
                .id(id)
                .name(name)
                .status(status)
                .projectId(projectId)
                .priority(priority)
                .downstreamResponse(downstreamResponse)
                .build();
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

        return Task.builder()
                .id((String) row.get("id"))
                .name((String) row.get("name"))
                .status((String) row.get("status"))
                .projectId((String) row.get("project_id"))
                .priority((Integer) row.get("priority"))
                .downstreamResponse(downstreamResponse)
                .build();
    }

    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        log.info("Querying all tasks");
        return jdbcTemplate.query(
            "SELECT id, name, status, project_id, priority FROM task ORDER BY name",
            (rs, rowNum) -> Task.builder()
                    .id(rs.getString("id"))
                    .name(rs.getString("name"))
                    .status(rs.getString("status"))
                    .projectId(rs.getString("project_id"))
                    .priority(rs.getInt("priority"))
                    .build()
        );
    }
}