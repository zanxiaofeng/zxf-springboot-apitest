package zxf.springboot.demo.apitest.support.sql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for verifying database state and connectivity in tests.
 * Provides methods for database validation and state checking.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseVerifier {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Verifies database connectivity by executing a simple query.
     *
     * @return true if the database is reachable and responding
     */
    public boolean isDatabaseConnected() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Collections.emptyMap(), Integer.class);
            log.debug("Database connectivity verified successfully");
            return true;
        } catch (Exception e) {
            log.error("Database connectivity check failed", e);
            return false;
        }
    }

    /**
     * Checks if a table exists in the database.
     *
     * @param tableName the name of the table to check
     * @return true if the table exists
     */
    public boolean tableExists(String tableName) {
        String query = """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_name = :tableName
                """;
        try {
            Integer count = jdbcTemplate.queryForObject(
                    query,
                    Collections.singletonMap("tableName", tableName.toLowerCase()),
                    Integer.class
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Failed to check if table '{}' exists", tableName, e);
            return false;
        }
    }

    /**
     * Counts the number of rows in a table.
     *
     * @param tableName the name of the table
     * @return the row count, or -1 if an error occurs
     */
    public long countRows(String tableName) {
        try {
            String query = "SELECT COUNT(*) FROM " + tableName;
            Long count = jdbcTemplate.queryForObject(query, Collections.emptyMap(), Long.class);
            return count != null ? count : -1;
        } catch (Exception e) {
            log.error("Failed to count rows in table '{}'", tableName, e);
            return -1;
        }
    }

    /**
     * Checks if a record exists in a table by a specific column value.
     *
     * @param tableName  the name of the table
     * @param columnName the name of the column to filter by
     * @param value      the value to search for
     * @return true if a matching record exists
     */
    public boolean recordExists(String tableName, String columnName, Object value) {
        String query = String.format("SELECT COUNT(*) FROM %s WHERE %s = :value", tableName, columnName);
        try {
            Long count = jdbcTemplate.queryForObject(
                    query,
                    Collections.singletonMap("value", value),
                    Long.class
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Failed to check if record exists in '{}' where '{}' = '{}'",
                    tableName, columnName, value, e);
            return false;
        }
    }

    /**
     * Retrieves a single value from a table by column and ID.
     *
     * @param tableName    the name of the table
     * @param idColumnName the name of the ID column
     * @param id           the ID value
     * @param resultColumn the column to retrieve
     * @return the value, or null if not found
     */
    public Object getValueById(String tableName, String idColumnName, Object id, String resultColumn) {
        String query = String.format(
                "SELECT %s FROM %s WHERE %s = :id",
                resultColumn, tableName, idColumnName
        );
        try {
            return jdbcTemplate.queryForObject(
                    query,
                    Collections.singletonMap("id", id),
                    Object.class
            );
        } catch (Exception e) {
            log.error("Failed to get value from table '{}', column '{}' where '{}' = '{}'",
                    tableName, resultColumn, idColumnName, id, e);
            return null;
        }
    }

    /**
     * Executes a custom query and returns the result as a list of maps.
     *
     * @param query      the SQL query to execute
     * @param parameters the named parameters for the query
     * @return a list of rows, where each row is represented as a map
     */
    public List<Map<String, Object>> executeQuery(String query, Map<String, Object> parameters) {
        try {
            return jdbcTemplate.queryForList(query, parameters);
        } catch (Exception e) {
            log.error("Failed to execute query: {}", query, e);
            return Collections.emptyList();
        }
    }

    /**
     * Verifies that the database schema contains all required tables.
     *
     * @param requiredTables the list of table names that must exist
     * @return true if all required tables exist
     */
    public boolean verifySchema(String... requiredTables) {
        for (String table : requiredTables) {
            if (!tableExists(table)) {
                log.warn("Required table '{}' does not exist", table);
                return false;
            }
        }
        log.debug("All required tables verified successfully");
        return true;
    }
}