package zxf.springboot.demo.apitest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import zxf.springboot.demo.apitest.support.BaseApiTest;
import zxf.springboot.demo.apitest.support.json.JSONComparatorFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Project API Tests.
 *
 * Features:
 * - Uses WireMock to mock external services
 * - Uses H2 in-memory database
 * - Uses TestRestTemplate for HTTP calls
 * - Each test method tests only ONE endpoint
 */
@Slf4j
@EnableWireMock({
    @ConfigureWireMock(name = "task-service", port = 8090, filesUnderClasspath = "mock-data")
})
public class ProjectApiTests extends BaseApiTest {

    private JSONComparator jsonComparator;

    @BeforeAll
    static void setupForAll() {
        log.info("========================= Starting Project API Tests =========================");
    }

    @BeforeEach
    void setupForEach() throws IOException {
        jsonComparator = JSONComparatorFactory.buildApiResponseComparator();
        log.info("========================= Setup for each test =========================");
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
        assertThat(response.getBody()).contains("proj-new");
        assertThat(response.getBody()).contains("New Project");
    }

    @Test
    void testCreateProjectWithValidationError() throws Exception {
        // Given
        String url = "/api/projects";
        String requestBody = "{\"id\":\"\",\"name\":\"\"}";

        // When
        ResponseEntity<String> response = httpPost(url, requestBody);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateProjectConflict() throws Exception {
        // Given - 使用已存在的 proj-001
        String url = "/api/projects";
        String requestBody = "{\"id\":\"proj-001\",\"name\":\"Duplicate Project\"}";

        // When
        ResponseEntity<String> response = httpPost(url, requestBody);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
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
        assertThat(response.getBody()).contains("proj-001");
        assertThat(response.getBody()).contains("Demo Project One");
    }

    @Test
    void testGetProjectByIdNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";

        // When
        ResponseEntity<String> response = httpGet(url);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== GET /api/projects Tests ====================

    @Test
    void testGetAllProjects() throws Exception {
        // Given - 使用预置数据
        String url = "/api/projects";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        assertThat(response.getBody()).contains("proj-001");
        assertThat(response.getBody()).contains("proj-002");
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
        assertThat(response.getBody()).contains("proj-001");
        assertThat(response.getBody()).contains("Updated Project Name");
    }

    @Test
    void testUpdateProjectNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";
        String requestBody = "{\"name\":\"Updated Name\"}";

        // When
        ResponseEntity<String> response = httpPut(url, requestBody);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateProjectValidationError() throws Exception {
        // Given
        String url = "/api/projects/proj-001";
        String requestBody = "{\"name\":\"\"}";

        // When
        ResponseEntity<String> response = httpPut(url, requestBody);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== DELETE /api/projects/{id} Tests ====================

    @Test
    void testDeleteProject() throws Exception {
        // Given - 使用预置数据 proj-delete
        String url = "/api/projects/proj-delete";

        // When
        ResponseEntity<String> response = httpDeleteAndAssert(url, HttpStatus.NO_CONTENT);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testDeleteProjectNotFound() throws Exception {
        // Given
        String url = "/api/projects/non-existent-id";

        // When
        ResponseEntity<String> response = httpDelete(url);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}