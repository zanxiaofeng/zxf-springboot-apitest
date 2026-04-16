package zxf.springboot.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zxf.springboot.demo.exception.BusinessException;
import zxf.springboot.demo.service.model.Project;

import java.util.Collections;
import java.util.HashMap;
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
            "SELECT id, name, details, created_at, updated_at FROM project ORDER BY name",
            Collections.emptyMap(),
            (rs, rowNum) -> Project.builder()
                    .id(rs.getString("id"))
                    .name(rs.getString("name"))
                    .details(rs.getString("details"))
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
                "SELECT id, name, details, created_at, updated_at FROM project WHERE id = :id",
                Collections.singletonMap("id", projectId)
            );
            return Project.builder()
                    .id((String) row.get("id"))
                    .name((String) row.get("name"))
                    .details((String) row.get("details"))
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
    public Project createProject(String name, String details) {
        String id = UUID.randomUUID().toString();
        log.info("Creating project: id={}, name={}", id, name);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", name);
        params.put("details", details);
        jdbcTemplate.update(
            "INSERT INTO project (id, name, details, created_at, updated_at) VALUES (:id, :name, :details, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            params
        );
        return queryProjectById(id);
    }

    /**
     * Update project name and details.
     * @throws BusinessException if project not found
     */
    public Project updateProject(String id, String name, String details) {
        log.info("Updating project: {} - {}", id, name);

        // Check if exists - will throw BusinessException if not found
        queryProjectById(id);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", id);
        updateParams.put("name", name);
        updateParams.put("details", details);
        int updated = jdbcTemplate.update(
            "UPDATE project SET name = :name, details = :details, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            updateParams
        );
        if (updated == 0) {
            throw BusinessException.notFound("Project", id);
        }
        return queryProjectById(id);
    }

    /**
     * Delete project by ID.
     * Uses transaction + row-level locking on project to prevent TOCTOU race condition.
     * @throws BusinessException if project not found or has associated tasks
     */
    @Transactional
    public void deleteProject(String id) {
        log.info("Deleting project: {}", id);

        // Lock project row — prevents concurrent task inserts referencing this project
        try {
            jdbcTemplate.queryForMap(
                "SELECT id FROM project WHERE id = :id FOR UPDATE",
                Collections.singletonMap("id", id)
            );
        } catch (EmptyResultDataAccessException e) {
            throw BusinessException.notFound("Project", id);
        }

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
