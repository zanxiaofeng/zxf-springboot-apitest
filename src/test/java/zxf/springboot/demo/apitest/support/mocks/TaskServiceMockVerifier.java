package zxf.springboot.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Verifier for checking that task-service was called with expected parameters.
 */
public class TaskServiceMockVerifier {

    /**
     * Verifies that task-service create API was called expected number of times.
     */
    public static void verifyCreateTaskCalled(int calledCount) {
        WireMock.verify(calledCount, postRequestedFor(urlEqualTo("/tasks"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(matching(".*\"id\"\\s*:\\s*\"[^\"]+\".*")));
    }

    /**
     * Verifies that task-service update API was called expected number of times.
     */
    public static void verifyUpdateTaskCalled(int calledCount, String taskId) {
        WireMock.verify(calledCount, putRequestedFor(urlEqualTo("/tasks/" + taskId))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    /**
     * Verifies that task-service delete API was called expected number of times.
     */
    public static void verifyDeleteTaskCalled(int calledCount, String taskId) {
        WireMock.verify(calledCount, deleteRequestedFor(urlEqualTo("/tasks/" + taskId)));
    }
}