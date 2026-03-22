package zxf.springboot.demo.apitest.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    protected <T> ResponseEntity<T> httpGetAndAssert(String url, HttpHeaders requestHeaders, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(null, requestHeaders, HttpMethod.GET, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    // ==================== POST Methods ====================

    /**
     * Execute HTTP POST request and assert status code.
     *
     * @param url            the request URL
     * @param requestBody    the request body as JSON string
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected <T> ResponseEntity<T> httpPostAndAssert(String url, HttpHeaders requestHeaders, String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, requestHeaders, HttpMethod.POST, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    // ==================== PUT Methods ====================

    /**
     * Execute HTTP PUT request and assert status code.
     *
     * @param url            the request URL
     * @param requestBody    the request body as JSON string
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected <T> ResponseEntity<T> httpPutAndAssert(String url, HttpHeaders requestHeaders, String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, requestHeaders, HttpMethod.PUT, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    // ==================== DELETE Methods ====================

    /**
     * Execute HTTP DELETE request and assert status code.
     *
     * @param url            the request URL
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected <T> ResponseEntity<T> httpDeleteAndAssert(String url, HttpHeaders requestHeaders, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        RequestEntity<String> requestEntity = new RequestEntity<>(null, requestHeaders, HttpMethod.DELETE, URI.create(url));
        ResponseEntity<T> responseEntity = testRestTemplate.exchange(requestEntity, tClass);
        assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
        if (expectedContentType != null && responseEntity.getHeaders().getContentType() != null) {
            assertTrue(expectedContentType.isCompatibleWith(responseEntity.getHeaders().getContentType()));
        }
        return responseEntity;
    }

    protected HttpHeaders commonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        return headers;
    }

    protected HttpHeaders commonHeadersAndJson() {
        HttpHeaders headers = commonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}