package zxf.springboot.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Verifier for checking that external service was called with expected parameters.
 * Used in Server Mode tests to verify HTTP request details.
 */
public class ExternalServiceMockVerifier {

    /**
     * Verifies that external GET API was called expected number of times with task parameter.
     *
     * @param calledCount expected number of calls
     * @param task         the task parameter expected
     */
    public static void verifyExternalServiceCalled(int calledCount, String task) {
        WireMock.verify(calledCount, getRequestedFor(urlEqualTo("/external/api?task=" + task)));
    }

    /**
     * Verifies that external POST API was called expected number of times.
     *
     * @param calledCount expected number of calls
     */
    public static void verifyExternalServicePostCalled(int calledCount) {
        WireMock.verify(calledCount, postRequestedFor(urlEqualTo("/external/api"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    /**
     * Verifies that external GET API was never called.
     *
     * @param task the task parameter
     */
    public static void verifyExternalServiceNeverCalled(String task) {
        WireMock.verify(0, getRequestedFor(urlEqualTo("/external/api?task=" + task)));
    }
}