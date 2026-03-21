package zxf.springboot.demo.apitest;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import zxf.springboot.demo.apitest.support.BaseServerModeTest;
import zxf.springboot.demo.apitest.support.json.JSONComparatorFactory;
import zxf.springboot.demo.apitest.support.mocks.TaskServiceMockFactory;
import zxf.springboot.demo.apitest.support.mocks.TaskServiceMockVerifier;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Server Mode API Tests for Demo Application.
 *
 * Features:
 * - Uses WireMock to mock task-service
 * - Uses H2 in-memory database
 * - Uses TestRestTemplate for HTTP calls
 * - Uses JSONAssert for response validation
 */
@Slf4j
@EnableWireMock({
    @ConfigureWireMock(name = "task-service", port = 8090, filesUnderClasspath = "mock-data")
})
public class ApiServerModeTests extends BaseServerModeTest {

    private JSONComparator jsonComparator;

    @BeforeAll
    static void setupForAll() {
        log.info("========================= Starting Server Mode Tests =========================");
    }

    @BeforeEach
    void setupForEach() throws IOException {
        jsonComparator = JSONComparatorFactory.buildApiResponseComparator();
        log.info("========================= Setup for each test =========================");
    }

    // ==================== POST Task Tests ====================

    @ParameterizedTest(name = "Create task with name-{0}")
    @CsvSource({"task-one", "task-two", "task-three"})
    void testCreateTask(String taskName) throws Exception {
        // Given
        String url = "/api/task";
        String requestBody = String.format("{\"name\":\"%s\",\"projectId\":null,\"priority\":1}", taskName);

        TaskServiceMockFactory.mockCreateTaskSuccess(taskName,
                "{\"taskId\":\"ext-123\",\"status\":\"CREATED\"}");

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.CREATED);

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(response.getBody()).contains("\"name\":\"" + taskName + "\"");
        assertThat(response.getBody()).contains("\"status\":\"PENDING\"");

        // Verify task-service was called
        TaskServiceMockVerifier.verifyCreateTaskCalled(1, taskName);
    }

    @Test
    void testCreateTaskWithProject() throws Exception {
        // Given
        String url = "/api/task";
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
        String url = "/api/task";
        String requestBody = "{\"name\":\"\",\"projectId\":null,\"priority\":1}";

        // When
        ResponseEntity<String> response = httpPost(url, requestBody);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== GET Task Tests ====================

    @Test
    void testGetTaskStatus() throws Exception {
        // Given - First create a task
        String createUrl = "/api/task";
        String createBody = "{\"name\":\"test-task-query\",\"projectId\":null,\"priority\":1}";
        TaskServiceMockFactory.mockCreateTaskSuccess("test-task-query",
                "{\"taskId\":\"ext-789\",\"status\":\"CREATED\"}");
        ResponseEntity<String> createResponse = httpPost(createUrl, createBody);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Extract task ID from response
        String responseBody = createResponse.getBody();
        String taskId = responseBody.substring(responseBody.indexOf("\"id\":\"") + 6, responseBody.indexOf("\"", responseBody.indexOf("\"id\":\"") + 6));

        // Mock the status query
        TaskServiceMockFactory.mockGetTaskStatusSuccess("test-task-query",
                "{\"taskId\":\"ext-789\",\"status\":\"COMPLETED\"}");

        // When - Get task status
        String getUrl = "/api/task/" + taskId;
        ResponseEntity<String> response = httpGetAndAssert(getUrl, HttpStatus.OK);

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(response.getBody()).contains("PENDING");
    }

    @Test
    void testGetTaskNotFound() throws Exception {
        // Given
        String url = "/api/task/" + UUID.randomUUID();

        // When
        ResponseEntity<String> response = httpGet(url);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== GET All Tasks Test ====================

    @Test
    void testGetAllTasks() throws Exception {
        // Given
        String createUrl = "/api/task";
        String createBody = "{\"name\":\"list-task\",\"projectId\":null,\"priority\":1}";
        TaskServiceMockFactory.mockCreateTaskSuccess("list-task",
                "{\"taskId\":\"ext-list\",\"status\":\"CREATED\"}");
        httpPost(createUrl, createBody);

        // When
        String url = "/api/tasks";
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        assertThat(response.getBody()).contains("list-task");
    }
}