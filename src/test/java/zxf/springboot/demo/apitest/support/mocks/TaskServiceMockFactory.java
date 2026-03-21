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
     *
     * @param taskName the task name
     * @param response the mock response body
     */
    public static void mockCreateTaskSuccess(String taskName, String response) {
        WireMock.stubFor(WireMock.post(urlEqualTo("/tasks"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(containing("\"name\":\"" + taskName + "\""))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

    /**
     * Mocks a successful task status query response from task-service.
     *
     * @param taskName the task name
     * @param response the mock response body
     */
    public static void mockGetTaskStatusSuccess(String taskName, String response) {
        WireMock.stubFor(WireMock.get(urlEqualTo("/tasks/status?name=" + taskName))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

    /**
     * Mocks a failed response from task-service.
     *
     * @param taskName the task name
     * @param status   the HTTP status code
     * @param response the mock error response body
     */
    public static void mockFailure(String taskName, int status, String response) {
        WireMock.stubFor(WireMock.get(urlEqualTo("/tasks/status?name=" + taskName))
                .willReturn(WireMock.aResponse()
                        .withStatus(status)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

    /**
     * Clears all WireMock stubs.
     */
    public static void reset() {
        WireMock.reset();
    }
}