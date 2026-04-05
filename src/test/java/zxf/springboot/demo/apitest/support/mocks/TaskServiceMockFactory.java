package zxf.springboot.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Factory for creating WireMock stubs for task-service simulation.
 * The task-service acts as an async task processor.
 */
@UtilityClass
public class TaskServiceMockFactory {

    /**
     * Mocks a successful task creation response from task-service.
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
     * Mocks a successful task creation response for a specific task ID.
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
     * Mocks a successful task update response from task-service.
     */
    public static void mockUpdateTaskSuccess(String taskId, String response) {
        WireMock.stubFor(WireMock.put(urlEqualTo("/tasks/" + taskId))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

    /**
     * Mocks a successful task deletion from task-service.
     */
    public static void mockDeleteTaskSuccess(String taskId) {
        WireMock.stubFor(WireMock.delete(urlEqualTo("/tasks/" + taskId))
                .willReturn(WireMock.aResponse()
                        .withStatus(204)));
    }
}