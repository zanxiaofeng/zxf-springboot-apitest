package zxf.springboot.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findAllProjects() {
        log.info("Finding all projects");
        return jdbcTemplate.queryForList(
            "SELECT id, name, created_at, updated_at FROM project ORDER BY name",
            Collections.emptyMap()
        );
    }

    public Optional<Map<String, Object>> queryProjectById(String projectId) {
        log.info("Querying project by id: {}", projectId);
        try {
            return Optional.of(jdbcTemplate.queryForMap(
                "SELECT id, name, created_at, updated_at FROM project WHERE id = :id",
                Collections.singletonMap("id", projectId)
            ));
        } catch (EmptyResultDataAccessException e) {
            log.debug("Project not found: {}", projectId);
            return Optional.empty();
        }
    }

    public void insertProject(String id, String name) {
        log.info("Inserting project: {} - {}", id, name);
        jdbcTemplate.update(
            "INSERT INTO project (id, name, created_at, updated_at) VALUES (:id, :name, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            Map.of("id", id, "name", name)
        );
    }

    public int updateProject(String id, String name) {
        log.info("Updating project: {} - {}", id, name);
        return jdbcTemplate.update(
            "UPDATE project SET name = :name, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            Map.of("id", id, "name", name)
        );
    }

    public void deleteProject(String id) {
        log.info("Deleting project: {}", id);
        jdbcTemplate.update(
            "DELETE FROM project WHERE id = :id",
            Collections.singletonMap("id", id)
        );
    }
}