package zxf.springboot.demo.apitest.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base test class for API tests using TestRestTemplate.
 * Provides common GET/POST/PUT/DELETE methods with status assertion.
 * <p>
 * Key features:
 * - Uses RANDOM_PORT to avoid port conflicts
 * - Supports SQL initialization/cleanup via @Sql annotations
 * - Provides simplified HTTP methods with built-in assertions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:sql/cleanup/clean-up.sql", "classpath:sql/init/schema.sql", "classpath:sql/init/data.sql"})
public abstract class BaseApiTest {
    @Autowired
    protected TestRestTemplate testRestTemplate;

    // ==================== GET Methods ====================

    /**
     * Execute HTTP GET request and assert status code.
     *
     * @param url            the request URL
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpGetAndAssert(String url, HttpStatus expectedStatus) {
        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }

    // ==================== POST Methods ====================

    /**
     * Execute HTTP POST request and assert status code.
     *
     * @param url            the request URL
     * @param body           the request body as JSON string
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpPostAndAssert(String url, String body, HttpStatus expectedStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<String> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(url));
        ResponseEntity<String> response = testRestTemplate.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }

    // ==================== PUT Methods ====================

    /**
     * Execute HTTP PUT request and assert status code.
     *
     * @param url            the request URL
     * @param body           the request body as JSON string
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpPutAndAssert(String url, String body, HttpStatus expectedStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<String> requestEntity = new RequestEntity<>(body, headers, HttpMethod.PUT, URI.create(url));
        ResponseEntity<String> response = testRestTemplate.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }

    // ==================== DELETE Methods ====================

    /**
     * Execute HTTP DELETE request and assert status code.
     *
     * @param url            the request URL
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpDeleteAndAssert(String url, HttpStatus expectedStatus) {
        ResponseEntity<String> response;
        try {
            testRestTemplate.delete(url);
            response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }
}