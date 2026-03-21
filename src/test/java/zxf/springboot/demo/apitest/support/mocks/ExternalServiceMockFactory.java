package zxf.springboot.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Factory for creating WireMock stubs for external service simulation.
 * Used in Server Mode tests to mock external API responses.
 */
public class ExternalServiceMockFactory {

    /**
     * Mocks a successful GET response from external service.
     *
     * @param task      the task parameter
     * @param response  the mock response body
     */
    public static void mockExternalServiceSuccess(String task, String response) {
        WireMock.stubFor(WireMock.get(urlEqualTo("/external/api?task=" + task))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

    /**
     * Mocks a successful POST response from external service.
     *
     * @param response  the mock response body
     */
    public static void mockExternalServicePostSuccess(String response) {
        WireMock.stubFor(WireMock.post(urlEqualTo("/external/api"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
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