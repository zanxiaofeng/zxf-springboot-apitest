package zxf.springboot.demo.apitest.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import zxf.springboot.demo.apitest.support.sql.DatabaseVerifier;

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
@AutoConfigureTestRestTemplate
@Sql(scripts = {"classpath:sql/cleanup/clean-up.sql", "classpath:sql/init/schema.sql", "classpath:sql/init/data.sql"})
public abstract class BaseApiTest {
    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected DatabaseVerifier databaseVerifier;

    // ==================== GET Methods ====================

    /**
     * Execute HTTP GET request and assert status code and content type.
     *
     * @param url                  the request URL
     * @param requestHeaders       the request headers
     * @param tClass               the response body type
     * @param expectedStatus       expected HTTP status
     * @param expectedContentType  expected content type (null to skip check)
     * @return ResponseEntity with typed body
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
     * Execute HTTP POST request and assert status code and content type.
     *
     * @param url                  the request URL
     * @param requestHeaders       the request headers
     * @param requestBody          the request body as JSON string
     * @param tClass               the response body type
     * @param expectedStatus       expected HTTP status
     * @param expectedContentType  expected content type (null to skip check)
     * @return ResponseEntity with typed body
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
     * Execute HTTP PUT request and assert status code and content type.
     *
     * @param url                  the request URL
     * @param requestHeaders       the request headers
     * @param requestBody          the request body as JSON string
     * @param tClass               the response body type
     * @param expectedStatus       expected HTTP status
     * @param expectedContentType  expected content type (null to skip check)
     * @return ResponseEntity with typed body
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
     * Execute HTTP DELETE request and assert status code and content type.
     *
     * @param url                  the request URL
     * @param requestHeaders       the request headers
     * @param tClass               the response body type
     * @param expectedStatus       expected HTTP status
     * @param expectedContentType  expected content type (null to skip check)
     * @return ResponseEntity with typed body
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

    /**
     * Creates common HTTP headers for requests.
     *
     * @return HttpHeaders with default settings
     */
    protected HttpHeaders commonHeaders() {
        return new HttpHeaders();
    }

    /**
     * Creates HTTP headers with JSON content type.
     *
     * @return HttpHeaders with Content-Type: application/json
     */
    protected HttpHeaders commonHeadersAndJson() {
        HttpHeaders headers = commonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}