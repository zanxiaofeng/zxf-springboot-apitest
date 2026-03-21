package zxf.springboot.demo.apitest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import zxf.springboot.demo.apitest.support.BaseApiTest;
import zxf.springboot.demo.apitest.support.json.JSONComparatorFactory;
import zxf.springboot.demo.apitest.support.mocks.TaskServiceMockFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task API Tests.
 *
 * Features:
 * - Uses WireMock to mock task-service
 * - Uses H2 in-memory database
 * - Uses TestRestTemplate for HTTP calls
 * - Uses JSONAssert for response validation
 * - Each test method tests only ONE endpoint
 */
@Slf4j
@EnableWireMock({
    @ConfigureWireMock(name = "task-service", port = 8090, filesUnderClasspath = "mock-data")
})
public class TaskApiTests extends BaseApiTest {

    private JSONComparator jsonComparator;

    @BeforeAll
    static void setupForAll() {
        log.info("========================= Starting Task API Tests =========================");
    }

    @BeforeEach
    void setupForEach() throws IOException {
        jsonComparator = JSONComparatorFactory.buildApiResponseComparator();
        log.info("========================= Setup for each test =========================");
    }

    // ==================== POST /api/tasks Tests ====================

    @ParameterizedTest(name = "Create task with name-{0}")
    @CsvSource({"task-one", "task-two", "task-three"})
    void testCreateTask(String taskName) throws Exception {
        // Given
        String url = "/api/tasks";
        String requestBody = String.format("{\"name\":\"%s\",\"projectId\":null,\"priority\":1}", taskName);

        TaskServiceMockFactory.mockCreateTaskSuccess(taskName,
                "{\"taskId\":\"ext-123\",\"status\":\"CREATED\"}");

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.CREATED);

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(response.getBody()).contains("\"name\":\"" + taskName + "\"");
        assertThat(response.getBody()).contains("\"status\":\"PENDING\"");
    }

    @Test
    void testCreateTaskWithProject() throws Exception {
        // Given
        String url = "/api/tasks";
        String requestBody = "{\"name\":\"task-with-project\",\"projectId\":\"proj-001\",\"priority\":5}";

        TaskServiceMockFactory.mockCreateTaskSuccess("task-with-project",
                "{\"taskId\":\"ext-456\",\"status\":\"CREATED\"}");

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.CREATED);

        // Then
        assertThat(response.getBody()).contains("task-with-project");
        assertThat(response.getBody()).contains("proj-001");
    }

    @Test
    void testCreateTaskWithValidationError() throws Exception {
        // Given
        String url = "/api/tasks";
        String requestBody = "{\"name\":\"\",\"projectId\":null,\"priority\":1}";

        // When
        ResponseEntity<String> response = httpPost(url, requestBody);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== GET /api/tasks/{id} Tests ====================

    @Test
    void testGetTaskById() throws Exception {
        // Given - 使用预置数据 task-001
        String taskId = "task-001";
        String url = "/api/tasks/" + taskId;

        TaskServiceMockFactory.mockGetTaskStatusSuccess("Test Task One",
                "{\"taskId\":\"ext-789\",\"status\":\"COMPLETED\"}");

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(response.getBody()).contains("task-001");
        assertThat(response.getBody()).contains("Test Task One");
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        // Given
        String url = "/api/tasks/non-existent-task-id";

        // When
        ResponseEntity<String> response = httpGet(url);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== GET /api/tasks Tests ====================

    @Test
    void testGetAllTasks() throws Exception {
        // Given - 使用预置数据
        String url = "/api/tasks";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        assertThat(response.getBody()).contains("task-001");
        assertThat(response.getBody()).contains("task-002");
    }
}