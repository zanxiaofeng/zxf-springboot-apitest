package zxf.springboot.demo.apitest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import zxf.springboot.demo.apitest.support.BaseApiTest;
import zxf.springboot.demo.apitest.support.json.JSONComparatorFactory;
import zxf.springboot.demo.apitest.support.json.JsonLoader;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Project API Tests.
 *
 * Features:
 * - Uses WireMock to mock external services
 * - Uses H2 in-memory database
 * - Uses TestRestTemplate for HTTP calls
 * - Uses JSONAssert for response validation
 * - Each test method tests only ONE endpoint
 */
@EnableWireMock({
    @ConfigureWireMock(name = "task-service", port = 8090, filesUnderClasspath = "mock-data")
})
public class ProjectApiTests extends BaseApiTest {
    private JSONComparator projectApiJsonResponseComparator;

    @BeforeEach
    void setupForEach() throws IOException {
        projectApiJsonResponseComparator = JSONComparatorFactory.buildApiResponseComparator();
    }

    // ==================== POST /api/projects Tests ====================

    @Test
    void testCreateProject() throws Exception {
        // Given
        String projectId = "proj-new";
        String url = "/api/projects";
        String requestBody = JsonLoader.load("project/post/request.json",
                Map.of("id", projectId, "name", "New Project"));
        int initialCount = databaseVerifier.countProjects();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/post/created.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);

        // And - verify database state
        assertThat(databaseVerifier.countProjects()).isEqualTo(initialCount + 1);
        assertThat(databaseVerifier.projectExists(projectId)).isTrue();
        assertThat(databaseVerifier.getProjectName(projectId)).isEqualTo("New Project");
    }

    @Test
    void testCreateProjectWithValidationError() throws Exception {
        // Given
        String url = "/api/projects";
        String requestBody = JsonLoader.load("project/post/request.json",
                Map.of("id", "", "name", ""));

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/post/validation-error.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testCreateProjectConflict() throws Exception {
        // Given - 使用已存在的 proj-001
        String url = "/api/projects";
        String requestBody = JsonLoader.load("project/post/request.json",
                Map.of("id", "proj-001", "name", "Duplicate Project"));
        int initialCount = databaseVerifier.countProjects();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.CONFLICT, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/post/conflict.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);

        // And - verify database state unchanged
        assertThat(databaseVerifier.countProjects()).isEqualTo(initialCount);
    }

    // ==================== GET /api/projects/{id} Tests ====================

    @Test
    void testGetProjectById() throws Exception {
        // Given - 使用预置数据 proj-001
        String url = "/api/projects/proj-001";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(), String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/get-by-id/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testGetProjectByIdNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(), String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/get-by-id/not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    // ==================== GET /api/projects Tests ====================

    @Test
    void testGetAllProjects() throws Exception {
        // Given - 使用预置数据
        String url = "/api/projects";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(), String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/get-all/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    // ==================== PUT /api/projects/{id} Tests ====================

    @Test
    void testUpdateProject() throws Exception {
        // Given - 使用预置数据 proj-001
        String projectId = "proj-001";
        String url = "/api/projects/" + projectId;
        String newName = "Updated Project Name";
        String requestBody = JsonLoader.load("project/put/request.json",
                Map.of("name", newName));

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/put/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);

        // And - verify database state
        assertThat(databaseVerifier.getProjectName(projectId)).isEqualTo(newName);
    }

    @Test
    void testUpdateProjectNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";
        String requestBody = JsonLoader.load("project/put/request.json",
                Map.of("name", "Updated Name"));

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/put/not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testUpdateProjectValidationError() throws Exception {
        // Given
        String url = "/api/projects/proj-001";
        String requestBody = JsonLoader.load("project/put/request.json",
                Map.of("name", ""));

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, commonHeadersAndJson(), requestBody, String.class, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("project/put/validation-error.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    // ==================== DELETE /api/projects/{id} Tests ====================

    @Test
    void testDeleteProject() throws Exception {
        // Given - 使用预置数据 proj-delete
        String projectId = "proj-delete";
        String url = "/api/projects/" + projectId;
        int initialCount = databaseVerifier.countProjects();
        assertThat(databaseVerifier.projectExists(projectId)).isTrue();

        // When & Then
        httpDeleteAndAssert(url, commonHeaders(), String.class, HttpStatus.NO_CONTENT, null);

        // And - verify database state
        assertThat(databaseVerifier.projectExists(projectId)).isFalse();
        assertThat(databaseVerifier.countProjects()).isEqualTo(initialCount - 1);
    }

    @Test
    void testDeleteProjectNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";

        // When & Then
        httpDeleteAndAssert(url, commonHeaders(), String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);
    }
}