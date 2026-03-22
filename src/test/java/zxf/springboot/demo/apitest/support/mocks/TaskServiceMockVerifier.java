package zxf.springboot.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Verifier for checking that task-service was called with expected parameters.
 * Used in Server Mode tests to verify HTTP request details.
 */
public class TaskServiceMockVerifier {

    /**
     * Verifies that task-service create API was called expected number of times.
     *
     * @param calledCount expected number of calls
     * @param taskName    the task name expected
     */
    public static void verifyCreateTaskCalled(int calledCount, String taskName) {
        WireMock.verify(calledCount, postRequestedFor(urlEqualTo("/tasks"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(containing("\"name\":\"" + taskName + "\"")));
    }

    /**
     * Verifies that task-service get status API was called expected number of times.
     *
     * @param calledCount expected number of calls
     * @param taskName    the task name expected (will be URL-encoded for matching)
     */
    public static void verifyGetTaskStatusCalled(int calledCount, String taskName) {
        String encodedName = URLEncoder.encode(taskName, StandardCharsets.UTF_8).replace("+", "%20");
        WireMock.verify(calledCount, getRequestedFor(urlEqualTo("/tasks/status?name=" + encodedName)));
    }
}