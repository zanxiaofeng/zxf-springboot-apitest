package zxf.springboot.demo.apitest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import zxf.springboot.demo.apitest.support.BaseApiTest;
import zxf.springboot.demo.apitest.support.json.JsonComparatorFactory;
import zxf.springboot.demo.apitest.support.json.JsonLoader;
import zxf.springboot.demo.apitest.support.mocks.TaskServiceMockFactory;
import zxf.springboot.demo.apitest.support.mocks.TaskServiceMockVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task API Tests.
 *
 * Features:
 * - Uses WireMock to mock task-service (async task processor)
 * - Uses H2 in-memory database
 * - Uses WebTestClient for HTTP calls
 * - Uses JSONAssert for response validation
 * - Each test method tests only ONE endpoint
 */
@EnableWireMock({
    @ConfigureWireMock(name = "task-service", port = 8090, filesUnderClasspath = "mock-data")
})
public class TaskApiTests extends BaseApiTest {

    private JSONComparator taskApiJsonResponseComparator;

    @BeforeEach
    void setupForEach() {
        taskApiJsonResponseComparator = JsonComparatorFactory.buildApiResponseComparator();
    }

    // ==================== POST /api/tasks Tests ====================

    @ParameterizedTest(name = "Create task with name-{0}")
    @CsvSource({"task-one", "task-two", "task-three"})
    void testCreateTask(String taskName) throws Exception {
        // Given
        String url = "/api/tasks";
        String requestBody = JsonLoader.load("task/post/request.json",
                Map.of("name", taskName, "projectId", "null", "priority", "1"));

        TaskServiceMockFactory.mockCreateTaskSuccess(
                "{\"taskId\":\"ext-123\",\"status\":\"ACCEPTED\"}");

        int initialCount = databaseVerifier.countTasks();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("task/post/created.json", Map.of("name", taskName, "priority", "1"));
        JSONAssert.assertEquals(expectedJson, response.getBody(), taskApiJsonResponseComparator);

        // And - verify database state
        assertThat(databaseVerifier.countTasks()).isEqualTo(initialCount + 1);
        String taskId = databaseVerifier.findTaskIdByName(taskName);
        assertThat(taskId).isNotNull();
        assertThat(databaseVerifier.getTaskPriority(taskId)).isEqualTo(1);

        // And - verify downstream async processor was called
        TaskServiceMockVerifier.verifyCreateTaskCalled(1);
    }

    @Test
    void testCreateTaskWithProject() throws Exception {
        // Given
        String taskName = "task-with-project";
        String url = "/api/tasks";
        String requestBody = JsonLoader.load("task/post/request.json",
                Map.of("name", taskName, "projectId", "\"proj-001\"", "priority", "5"));

        TaskServiceMockFactory.mockCreateTaskSuccess(
                "{\"taskId\":\"ext-456\",\"status\":\"ACCEPTED\"}");

        int initialCount = databaseVerifier.countTasks();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("task/post/created.json", Map.of("name", taskName, "priority", "5"));
        JSONAssert.assertEquals(expectedJson, response.getBody(), taskApiJsonResponseComparator);

        // And - verify database state
        assertThat(databaseVerifier.countTasks()).isEqualTo(initialCount + 1);
        String taskId = databaseVerifier.findTaskIdByName(taskName);
        assertThat(taskId).isNotNull();
        assertThat(databaseVerifier.getTaskPriority(taskId)).isEqualTo(5);

        // And - verify downstream async processor was called
        TaskServiceMockVerifier.verifyCreateTaskCalled(1);
    }

    @Test
    void testCreateTaskWithValidationError() throws Exception {
        // Given
        String url = "/api/tasks";
        String requestBody = JsonLoader.load("task/post/request.json",
                Map.of("name", "", "projectId", "null", "priority", "1"));

        int initialCount = databaseVerifier.countTasks();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("task/post/validation-error.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), taskApiJsonResponseComparator);

        // And - verify database state unchanged
        assertThat(databaseVerifier.countTasks()).isEqualTo(initialCount);

        // And - verify downstream service was NOT called
        TaskServiceMockVerifier.verifyCreateTaskCalled(0);
    }

    // ==================== GET /api/tasks/{id} Tests ====================

    @Test
    void testGetTaskById() throws Exception {
        // Given - 使用预置数据 task-001
        String taskId = "task-001";
        String url = "/api/tasks/" + taskId;

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(), String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("task/get-by-id/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), taskApiJsonResponseComparator);
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        // Given
        String url = "/api/tasks/non-existent-task-id";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(), String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("task/get-by-id/not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), taskApiJsonResponseComparator);
    }

    // ==================== GET /api/tasks Tests ====================

    @Test
    void testGetAllTasks() throws Exception {
        // Given - 使用预置数据
        String url = "/api/tasks";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(), String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("task/get-all/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), taskApiJsonResponseComparator);
    }

    // ==================== DELETE /api/tasks/{id} Tests ====================

    @Test
    void testDeleteTask() throws Exception {
        // Given - 使用预置数据 task-002
        String taskId = "task-002";
        String url = "/api/tasks/" + taskId;

        TaskServiceMockFactory.mockDeleteTaskSuccess(taskId);

        int initialCount = databaseVerifier.countTasks();

        // When
        ResponseEntity<String> response = httpDeleteAndAssert(url, commonHeaders(), String.class, HttpStatus.OK, null);

        // Then - verify response has no content
        assertThat(response.getBody()).isNull();

        // And - verify database state
        assertThat(databaseVerifier.countTasks()).isEqualTo(initialCount - 1);

        // And - verify downstream service was called
        TaskServiceMockVerifier.verifyDeleteTaskCalled(1, taskId);
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        // Given
        String taskId = "non-existent-task-id";
        String url = "/api/tasks/" + taskId;

        TaskServiceMockFactory.mockDeleteTaskSuccess(taskId);

        int initialCount = databaseVerifier.countTasks();

        // When
        ResponseEntity<String> response = httpDeleteAndAssert(url, commonHeaders(), String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("task/delete/not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), taskApiJsonResponseComparator);

        // And - verify database state unchanged
        assertThat(databaseVerifier.countTasks()).isEqualTo(initialCount);

        // And - verify downstream service was NOT called
        TaskServiceMockVerifier.verifyDeleteTaskCalled(0, taskId);
    }
}