package zxf.springboot.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import zxf.springboot.demo.exception.BusinessException;
import zxf.springboot.demo.service.model.Project;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for project database operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Find all projects.
     */
    public List<Project> findAllProjects() {
        log.info("Finding all projects");
        return jdbcTemplate.query(
            "SELECT id, name, created_at, updated_at FROM project ORDER BY name",
            Collections.emptyMap(),
            (rs, rowNum) -> Project.builder()
                    .id(rs.getString("id"))
                    .name(rs.getString("name"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build()
        );
    }

    /**
     * Query project by ID.
     * @throws BusinessException if project not found
     */
    public Project queryProjectById(String projectId) {
        log.info("Querying project by id: {}", projectId);
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT id, name, created_at, updated_at FROM project WHERE id = :id",
                Collections.singletonMap("id", projectId)
            );
            return Project.builder()
                    .id((String) row.get("id"))
                    .name((String) row.get("name"))
                    .createdAt(((java.sql.Timestamp) row.get("created_at")).toLocalDateTime())
                    .updatedAt(((java.sql.Timestamp) row.get("updated_at")).toLocalDateTime())
                    .build();
        } catch (EmptyResultDataAccessException e) {
            log.debug("Project not found: {}", projectId);
            throw BusinessException.notFound("Project", projectId);
        }
    }

    /**
     * Create a new project with auto-generated ID.
     */
    public Project createProject(String name) {
        String id = UUID.randomUUID().toString();
        log.info("Creating project: id={}, name={}", id, name);
        jdbcTemplate.update(
            "INSERT INTO project (id, name, created_at, updated_at) VALUES (:id, :name, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            Map.of("id", id, "name", name)
        );
        return queryProjectById(id);
    }

    /**
     * Update project name.
     * @throws BusinessException if project not found
     */
    public Project updateProject(String id, String name) {
        log.info("Updating project: {} - {}", id, name);

        // Check if exists - will throw BusinessException if not found
        queryProjectById(id);

        int updated = jdbcTemplate.update(
            "UPDATE project SET name = :name, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            Map.of("id", id, "name", name)
        );
        if (updated == 0) {
            throw BusinessException.notFound("Project", id);
        }
        return queryProjectById(id);
    }

    /**
     * Delete project by ID.
     * Checks for related tasks before deletion.
     * @throws BusinessException if project not found or has associated tasks
     */
    public void deleteProject(String id) {
        log.info("Deleting project: {}", id);

        // Check if project exists - will throw BusinessException if not found
        queryProjectById(id);

        // Check if there are related tasks
        int taskCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM task WHERE project_id = :id",
            Collections.singletonMap("id", id),
            Integer.class
        );

        if (taskCount > 0) {
            log.warn("Cannot delete project {} - {} tasks still associated", id, taskCount);
            throw BusinessException.conflict("Cannot delete project with " + taskCount + " associated tasks");
        }

        jdbcTemplate.update(
            "DELETE FROM project WHERE id = :id",
            Collections.singletonMap("id", id)
        );
    }
}