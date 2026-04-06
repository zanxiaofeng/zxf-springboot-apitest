package zxf.springboot.demo.apitest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import zxf.springboot.demo.apitest.support.BaseApiTest;

import static org.assertj.core.api.Assertions.assertThat;

class DemoApiTests extends BaseApiTest {

    // ==================== Metrics Demo Tests ====================

    @Test
    void testMetricsTimedEndpoint() {
        ResponseEntity<String> response = httpGetAndAssert(
                "/api/demo/metrics/timed", commonHeadersAndJson(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        assertThat(response.getBody()).contains("Timed operation completed");
    }

    @Test
    void testMetricsCountedEndpoint() {
        ResponseEntity<String> response = httpGetAndAssert(
                "/api/demo/metrics/counted", commonHeadersAndJson(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        assertThat(response.getBody()).contains("Counted operation completed");
    }

    @Test
    void testMetricsActiveUsersEndpoint() {
        ResponseEntity<String> response = httpPostAndAssert(
                "/api/demo/metrics/active-users?delta=5", commonHeadersAndJson(), null,
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        assertThat(response.getBody()).contains("\"activeUsers\":5");
    }

    // ==================== Tracing Demo Tests ====================

    @Test
    void testTracingEndpoint() {
        ResponseEntity<String> response = httpGetAndAssert(
                "/api/demo/tracing/hello", commonHeadersAndJson(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        assertThat(response.getBody()).contains("Traced:");
        assertThat(response.getBody()).contains("hello");
    }

    // ==================== Logging Demo Tests ====================

    @Test
    void testLoggingDemoEndpoint() {
        ResponseEntity<String> response = httpGetAndAssert(
                "/api/demo/logging/demo?name=TestUser", commonHeadersAndJson(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        assertThat(response.getBody()).contains("Hello, TestUser!");
    }

    @Test
    void testLoggingLevelsEndpoint() {
        ResponseEntity<String> response = httpGetAndAssert(
                "/api/demo/logging/levels", commonHeadersAndJson(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        assertThat(response.getBody()).contains("All log levels demonstrated");
    }
}
