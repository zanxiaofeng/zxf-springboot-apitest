package zxf.springboot.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Factory for creating WireMock stubs for task-service simulation.
 * Used in Server Mode tests to mock downstream task-service responses.
 */
public class TaskServiceMockFactory {

    /**
     * Mocks a successful task creation response from task-service.
     * Matches any task ID (for tests where ID is dynamically generated).
     *
     * @param response the mock response body
     */
    public static void mockCreateTaskSuccess(String response) {
        WireMock.stubFor(WireMock.post(urlEqualTo("/tasks"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(matching(".*\"id\"\\s*:\\s*\"[^\"]+\".*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

    /**
     * Mocks a successful task creation response from task-service.
     *
     * @param taskId the task ID
     * @param response the mock response body
     */
    public static void mockCreateTaskSuccess(String taskId, String response) {
        WireMock.stubFor(WireMock.post(urlEqualTo("/tasks"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(containing("\"id\":\"" + taskId + "\""))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

    /**
     * Mocks a successful task status query response from task-service.
     *
     * @param taskId the task ID
     * @param response the mock response body
     */
    public static void mockGetTaskStatusSuccess(String taskId, String response) {
        WireMock.stubFor(WireMock.get(urlEqualTo("/tasks/" + taskId + "/status"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }
}