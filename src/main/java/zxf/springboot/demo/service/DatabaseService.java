package zxf.springboot.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DatabaseService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DatabaseService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findAllProjects() {
        log.info("Finding all projects");
        try {
            return jdbcTemplate.queryForList(
                "SELECT id, name, created_at, updated_at FROM project ORDER BY name",
                Collections.emptyMap()
            );
        } catch (Exception e) {
            return jdbcTemplate.queryForList(
                "SELECT id, name FROM project ORDER BY name",
                Collections.emptyMap()
            );
        }
    }

    public Map<String, Object> queryProjectById(String projectId) {
        log.info("Querying project by id: {}", projectId);
        try {
            // 尝试查询完整字段，如果失败则查询基本字段
            try {
                return jdbcTemplate.queryForMap(
                    "SELECT id, name, created_at, updated_at FROM project WHERE id = :id",
                    Collections.singletonMap("id", projectId)
                );
            } catch (Exception e) {
                return jdbcTemplate.queryForMap(
                    "SELECT id, name FROM project WHERE id = :id",
                    Collections.singletonMap("id", projectId)
                );
            }
        } catch (Exception e) {
            log.warn("Project not found: {}", projectId);
            return null;
        }
    }

    public void insertProject(String id, String name) {
        log.info("Inserting project: {} - {}", id, name);
        jdbcTemplate.update(
            "INSERT INTO project (id, name) VALUES (:id, :name)",
            Map.of("id", id, "name", name)
        );
    }

    public int updateProject(String id, String name) {
        log.info("Updating project: {} - {}", id, name);
        return jdbcTemplate.update(
            "UPDATE project SET name = :name WHERE id = :id",
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

    public int countProjects() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM project",
            Collections.emptyMap(),
            Integer.class
        );
        return count != null ? count : 0;
    }
}