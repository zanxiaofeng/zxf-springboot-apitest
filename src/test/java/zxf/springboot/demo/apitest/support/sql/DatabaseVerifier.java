package zxf.springboot.demo.apitest.support.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DatabaseVerifier {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public boolean projectExists(String id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM project WHERE id = :id",
                Map.of("id", id),
                Long.class);
        return count != null && count > 0;
    }

    public String getProjectName(String id) {
        return jdbcTemplate.queryForObject(
                "SELECT name FROM project WHERE id = :id",
                Map.of("id", id),
                String.class);
    }

    public int countProjects() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM project",
                Map.of(),
                Integer.class);
    }

    public boolean taskExists(String id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task WHERE id = :id",
                Map.of("id", id),
                Long.class);
        return count != null && count > 0;
    }

    public int countTasks() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task",
                Map.of(),
                Integer.class);
    }

    public String getTaskStatus(String id) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM task WHERE id = :id",
                Map.of("id", id),
                String.class);
    }

    public String findTaskIdByName(String name) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM task WHERE name = :name",
                Map.of("name", name),
                String.class);
    }

    public Integer getTaskPriority(String id) {
        return jdbcTemplate.queryForObject(
                "SELECT priority FROM task WHERE id = :id",
                Map.of("id", id),
                Integer.class);
    }
}