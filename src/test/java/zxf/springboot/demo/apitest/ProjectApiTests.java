package zxf.springboot.demo.apitest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import zxf.springboot.demo.apitest.support.BaseApiTest;
import zxf.springboot.demo.apitest.support.json.JSONComparatorFactory;
import zxf.springboot.demo.apitest.support.json.JsonLoader;

import java.io.IOException;

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
        String url = "/api/projects";
        String requestBody = "{\"id\":\"proj-new\",\"name\":\"New Project\"}";

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.CREATED);

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        String expectedJson = JsonLoader.load("project/post-project-created.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testCreateProjectWithValidationError() throws Exception {
        // Given
        String url = "/api/projects";
        String requestBody = "{\"id\":\"\",\"name\":\"\"}";

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.BAD_REQUEST);

        // Then
        String expectedJson = JsonLoader.load("project/post-project-validation-error.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testCreateProjectConflict() throws Exception {
        // Given - 使用已存在的 proj-001
        String url = "/api/projects";
        String requestBody = "{\"id\":\"proj-001\",\"name\":\"Duplicate Project\"}";

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.CONFLICT);

        // Then
        String expectedJson = JsonLoader.load("project/post-project-conflict.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    // ==================== GET /api/projects/{id} Tests ====================

    @Test
    void testGetProjectById() throws Exception {
        // Given - 使用预置数据 proj-001
        String url = "/api/projects/proj-001";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        String expectedJson = JsonLoader.load("project/get-project-by-id.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testGetProjectByIdNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.NOT_FOUND);

        // Then
        String expectedJson = JsonLoader.load("project/get-project-not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    // ==================== GET /api/projects Tests ====================

    @Test
    void testGetAllProjects() throws Exception {
        // Given - 使用预置数据
        String url = "/api/projects";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        String expectedJson = JsonLoader.load("project/get-all-projects.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    // ==================== PUT /api/projects/{id} Tests ====================

    @Test
    void testUpdateProject() throws Exception {
        // Given - 使用预置数据 proj-001
        String url = "/api/projects/proj-001";
        String requestBody = "{\"name\":\"Updated Project Name\"}";

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, requestBody, HttpStatus.OK);

        // Then
        String expectedJson = JsonLoader.load("project/put-project-updated.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testUpdateProjectNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";
        String requestBody = "{\"name\":\"Updated Name\"}";

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, requestBody, HttpStatus.NOT_FOUND);

        // Then
        String expectedJson = JsonLoader.load("project/put-project-not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    @Test
    void testUpdateProjectValidationError() throws Exception {
        // Given
        String url = "/api/projects/proj-001";
        String requestBody = "{\"name\":\"\"}";

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, requestBody, HttpStatus.BAD_REQUEST);

        // Then
        String expectedJson = JsonLoader.load("project/put-project-validation-error.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), projectApiJsonResponseComparator);
    }

    // ==================== DELETE /api/projects/{id} Tests ====================

    @Test
    void testDeleteProject() throws Exception {
        // Given - 使用预置数据 proj-delete
        String url = "/api/projects/proj-delete";

        // When & Then
        httpDeleteAndAssert(url, HttpStatus.NO_CONTENT);
    }

    @Test
    void testDeleteProjectNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";

        // When & Then
        httpDeleteAndAssert(url, HttpStatus.NOT_FOUND);
    }
}