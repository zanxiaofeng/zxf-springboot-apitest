package zxf.springboot.demo.apitest.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import zxf.springboot.demo.apitest.support.sql.DatabaseVerifier;

/**
 * Base test class for API tests using WebTestClient.
 * Provides common GET/POST/PUT/DELETE methods with status assertion.
 * <p>
 * Key features:
 * - Uses RANDOM_PORT to avoid port conflicts
 * - Supports SQL initialization/cleanup via @Sql annotations
 * - Provides simplified HTTP methods with built-in assertions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = {"classpath:sql/cleanup/clean-up.sql", "classpath:sql/init/schema.sql", "classpath:sql/init/data.sql"})
public abstract class BaseApiTest {
    @Autowired
    protected WebTestClient webTestClient;
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
        var responseSpec = webTestClient.get()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
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
        var requestSpec = webTestClient.post()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders));

        var responseSpec = (requestBody != null ? requestSpec.bodyValue(requestBody) : requestSpec)
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
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
        var responseSpec = webTestClient.put()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .bodyValue(requestBody)
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
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
        var responseSpec = webTestClient.delete()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== Common Methods ====================

    private void assertContentType(WebTestClient.ResponseSpec responseSpec, MediaType expectedContentType) {
        if (expectedContentType != null) {
            responseSpec.expectHeader().contentType(expectedContentType);
        }
    }

    private <T> ResponseEntity<T> toResponseEntity(EntityExchangeResult<T> result) {
        return ResponseEntity.status(result.getStatus())
                .headers(result.getResponseHeaders())
                .body(result.getResponseBody());
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
