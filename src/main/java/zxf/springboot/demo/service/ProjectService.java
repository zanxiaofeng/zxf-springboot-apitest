package zxf.springboot.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import zxf.springboot.demo.model.Project;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     */
    public Optional<Project> queryProjectById(String projectId) {
        log.info("Querying project by id: {}", projectId);
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT id, name, created_at, updated_at FROM project WHERE id = :id",
                Collections.singletonMap("id", projectId)
            );
            return Optional.of(Project.builder()
                    .id((String) row.get("id"))
                    .name((String) row.get("name"))
                    .createdAt(((java.sql.Timestamp) row.get("created_at")).toLocalDateTime())
                    .updatedAt(((java.sql.Timestamp) row.get("updated_at")).toLocalDateTime())
                    .build());
        } catch (EmptyResultDataAccessException e) {
            log.debug("Project not found: {}", projectId);
            return Optional.empty();
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
        return queryProjectById(id).orElseThrow();
    }

    /**
     * Update project name.
     */
    public Optional<Project> updateProject(String id, String name) {
        log.info("Updating project: {} - {}", id, name);
        int updated = jdbcTemplate.update(
            "UPDATE project SET name = :name, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            Map.of("id", id, "name", name)
        );
        if (updated == 0) {
            return Optional.empty();
        }
        return queryProjectById(id);
    }

    /**
     * Delete project by ID.
     */
    public boolean deleteProject(String id) {
        log.info("Deleting project: {}", id);
        int deleted = jdbcTemplate.update(
            "DELETE FROM project WHERE id = :id",
            Collections.singletonMap("id", id)
        );
        return deleted > 0;
    }
}