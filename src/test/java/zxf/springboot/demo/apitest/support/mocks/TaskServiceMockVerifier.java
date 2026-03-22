package zxf.springboot.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Verifier for checking that task-service was called with expected parameters.
 * Used in Server Mode tests to verify HTTP request details.
 */
public class TaskServiceMockVerifier {

    /**
     * Verifies that task-service create API was called expected number of times.
     * Does not verify specific task ID (for tests where ID is dynamically generated).
     *
     * @param calledCount expected number of calls
     */
    public static void verifyCreateTaskCalled(int calledCount) {
        WireMock.verify(calledCount, postRequestedFor(urlEqualTo("/tasks"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(matching(".*\"id\"\\s*:\\s*\"[^\"]+\".*")));
    }

    /**
     * Verifies that task-service create API was called expected number of times.
     *
     * @param calledCount expected number of calls
     * @param taskId      the task ID expected
     */
    public static void verifyCreateTaskCalled(int calledCount, String taskId) {
        WireMock.verify(calledCount, postRequestedFor(urlEqualTo("/tasks"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(containing("\"id\":\"" + taskId + "\"")));
    }

    /**
     * Verifies that task-service get status API was called expected number of times.
     *
     * @param calledCount expected number of calls
     * @param taskId      the task ID expected
     */
    public static void verifyGetTaskStatusCalled(int calledCount, String taskId) {
        WireMock.verify(calledCount, getRequestedFor(urlEqualTo("/tasks/" + taskId + "/status")));
    }
}