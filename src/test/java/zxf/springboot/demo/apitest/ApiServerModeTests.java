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
import zxf.springboot.demo.apitest.support.mocks.ExternalServiceMockFactory;
import zxf.springboot.demo.apitest.support.mocks.ExternalServiceMockVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Server Mode API Tests for Demo Application.
 *
 * Features:
 * - Uses WireMock to mock external service
 * - Uses H2 in-memory database
 * - Uses TestRestTemplate for HTTP calls
 * - Uses JSONAssert for response validation
 */
@Slf4j
@EnableWireMock({
    @ConfigureWireMock(name = "external-service", port = 8090, filesUnderClasspath = "mock-data")
})
public class ApiServerModeTests extends BaseServerModeTest {

    private String requestTemplate;
    private JSONComparator jsonComparator;

    @BeforeAll
    static void setupForAll() {
        log.info("========================= Starting Server Mode Tests =========================");
    }

    @BeforeEach
    void setupForEach() throws IOException {
        requestTemplate = IOUtils.resourceToString("/test-data/post-request.json", Charsets.UTF_8);
        jsonComparator = JSONComparatorFactory.buildApiResponseComparator();
        log.info("========================= Setup for each test =========================");
    }

    // ==================== GET Tests ====================

    @Test
    void testGetTaskSuccess() throws Exception {
        // Given
        String url = "/api/task?task=task-200";
        ExternalServiceMockFactory.mockExternalServiceSuccess("task-200",
                "{\"task\":\"EXT.task-200\",\"value\":\"1234567890\"}");

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        String expected = IOUtils.resourceToString("/test-data/response-success.json", Charsets.UTF_8);
        JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

        // Verify external service was called
        ExternalServiceMockVerifier.verifyExternalServiceCalled(1, "task-200");
    }

    @Test
    void testGetTaskWithProject() throws Exception {
        // Given
        String url = "/api/task?task=task-200&projectId=proj-001";
        ExternalServiceMockFactory.mockExternalServiceSuccess("task-200",
                "{\"task\":\"EXT.task-200\",\"value\":\"1234567890\"}");

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, HttpStatus.OK);

        // Then
        String expected = IOUtils.resourceToString("/test-data/response-with-project.json", Charsets.UTF_8);
        JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);
    }

    // ==================== POST Tests ====================

    @ParameterizedTest(name = "Test POST with task-{0}")
    @CsvSource({"task-200,200", "task-201,200", "task-400,200"})
    void testPostTask(String task, Integer expectedStatus) throws Exception {
        // Given
        String url = "/api/task";
        String requestBody = String.format("{\"task\":\"%s\",\"projectId\":null,\"priority\":1}", task);

        ExternalServiceMockFactory.mockExternalServicePostSuccess(
                "{\"task\":\"EXT." + task + "\",\"value\":\"1234567890\"}");

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.valueOf(expectedStatus));

        // Then
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(response.getBody()).contains("DEMO." + task);

        // Verify external service was called
        ExternalServiceMockVerifier.verifyExternalServicePostCalled(1);
    }

    @Test
    void testPostTaskWithProject() throws Exception {
        // Given
        String url = "/api/task";
        String requestBody = "{\"task\":\"task-200\",\"projectId\":\"proj-001\",\"priority\":1}";

        ExternalServiceMockFactory.mockExternalServicePostSuccess(
                "{\"task\":\"EXT.task-200\",\"value\":\"1234567890\"}");

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, requestBody, HttpStatus.OK);

        // Then
        assertThat(response.getBody()).contains("proj-001");
        assertThat(response.getBody()).contains("Demo Project One");
    }
}