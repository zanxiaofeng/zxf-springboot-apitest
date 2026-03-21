package zxf.springboot.demo.apitest.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base test class for API tests using TestRestTemplate.
 * Provides common GET/POST/PUT/DELETE methods with optional status assertion.
 *
 * Key features:
 * - Uses RANDOM_PORT to avoid port conflicts
 * - Supports SQL initialization/cleanup via @Sql annotations
 * - Provides simplified HTTP methods for test readability
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
@Sql(scripts = {
    "classpath:sql/cleanup/clean-up.sql",
    "classpath:sql/init/schema.sql",
    "classpath:sql/init/data.sql"
})
public abstract class BaseApiTest {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    // ==================== GET Methods ====================

    /**
     * Execute HTTP GET request without assertion.
     *
     * @param url the request URL
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpGet(String url) {
        return testRestTemplate.getForEntity(url, String.class);
    }

    /**
     * Execute HTTP GET request and assert status code.
     *
     * @param url           the request URL
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpGetAndAssert(String url, HttpStatus expectedStatus) {
        ResponseEntity<String> response = httpGet(url);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }

    // ==================== POST Methods ====================

    /**
     * Execute HTTP POST request without assertion.
     *
     * @param url  the request URL
     * @param body the request body as JSON string
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpPost(String url, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<String> requestEntity = new RequestEntity<>(body, headers, HttpMethod.POST, URI.create(url));
        return testRestTemplate.exchange(requestEntity, String.class);
    }

    /**
     * Execute HTTP POST request and assert status code.
     *
     * @param url           the request URL
     * @param body          the request body as JSON string
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpPostAndAssert(String url, String body, HttpStatus expectedStatus) {
        ResponseEntity<String> response = httpPost(url, body);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }

    // ==================== PUT Methods ====================

    /**
     * Execute HTTP PUT request without assertion.
     *
     * @param url  the request URL
     * @param body the request body as JSON string
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpPut(String url, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<String> requestEntity = new RequestEntity<>(body, headers, HttpMethod.PUT, URI.create(url));
        return testRestTemplate.exchange(requestEntity, String.class);
    }

    /**
     * Execute HTTP PUT request and assert status code.
     *
     * @param url           the request URL
     * @param body          the request body as JSON string
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpPutAndAssert(String url, String body, HttpStatus expectedStatus) {
        ResponseEntity<String> response = httpPut(url, body);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }

    // ==================== DELETE Methods ====================

    /**
     * Execute HTTP DELETE request without assertion.
     *
     * @param url the request URL
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpDelete(String url) {
        try {
            testRestTemplate.delete(url);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Execute HTTP DELETE request and assert status code.
     *
     * @param url           the request URL
     * @param expectedStatus expected HTTP status
     * @return ResponseEntity with String body
     */
    protected ResponseEntity<String> httpDeleteAndAssert(String url, HttpStatus expectedStatus) {
        ResponseEntity<String> response = httpDelete(url);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        return response;
    }
}