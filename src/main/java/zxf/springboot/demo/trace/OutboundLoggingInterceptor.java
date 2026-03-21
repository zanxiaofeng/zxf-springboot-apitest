package zxf.springboot.demo.trace;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientResponseException;
import zxf.springboot.demo.trace.http.BufferingClientHttpResponseWrapper;
import zxf.springboot.demo.trace.sensitive.SensitiveDataHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@AllArgsConstructor
public class OutboundLoggingInterceptor implements ClientHttpRequestInterceptor {
    private final SensitiveDataHelper sensitiveDataHelper;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ClientHttpResponse response = new BufferingClientHttpResponseWrapper(execution.execute(request, body));
            stopWatch.stop();
            logRequestAndResponse(request, body, response.getStatusCode(), response.getHeaders(), StreamUtils.copyToByteArray(response.getBody()), stopWatch.getTotalTimeMillis());
            return response;
        } catch (RestClientResponseException ex) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logRequestAndResponse(request, body, ex.getStatusCode(), ex.getResponseHeaders(), ex.getResponseBodyAsByteArray(), stopWatch.getTotalTimeMillis());
            throw ex;
        } catch (Exception ex) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logRequestAndResponse(request, body, null, null, null, stopWatch.getTotalTimeMillis());
            log.warn("Exception when send request, exception: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private void logRequestAndResponse(HttpRequest request, byte[] requestBody, HttpStatusCode statusCode, HttpHeaders responseHeaders, byte[] responseBody, long elapsedMillis) throws IOException {
        try {
            boolean isSuccess = statusCode != null && statusCode.is2xxSuccessful();

            boolean loggingEnabled = isSuccess ? log.isDebugEnabled() : log.isErrorEnabled();
            if (!loggingEnabled) {
                return;
            }

            Consumer<String> logger = isSuccess ? log::debug : log::error;
            logger.accept("=================================================Request begin(Outbound)=================================================");
            logger.accept(String.format("URL             : %s", request.getURI()));
            logger.accept(String.format("Method          : %s", request.getMethod()));
            logger.accept(String.format("Headers         : %s", formatHeaders(request.getHeaders())));
            logger.accept(String.format("Request Body    : %s", readAndMaskJsonContent(request.getHeaders().getContentType(), requestBody)));
            logger.accept("=================================================Request end(Outbound)=================================================");

            if (statusCode != null) {
                logger.accept(String.format("=================================================Response begin(Inbound, elapsed: %dms)=================================================", elapsedMillis));
                logger.accept(String.format("Status code     : %d", statusCode.value()));
                logger.accept(String.format("Headers         : %s", formatHeaders(responseHeaders)));
                logger.accept(String.format("Response Body   : %s", readAndMaskJsonContent(responseHeaders.getContentType(), responseBody)));
                logger.accept("=================================================Response end(Inbound)=================================================");
            }
        } catch (Exception ex) {
            log.warn("Exception when log request and response, exception: {}", ex.getMessage(), ex);
        }
    }

    private String formatHeaders(HttpHeaders headers) {
        HttpHeaders clearHttpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            boolean isSensitiveHeader = sensitiveDataHelper.isSensitiveHeader(key);
            clearHttpHeaders.put(key, isSensitiveHeader ? List.of("***") : value);
        });
        return clearHttpHeaders.toString();
    }

    private String readAndMaskJsonContent(MediaType mediaType, byte[] contentBytes) {
        if (contentBytes == null) {
            return "";
        }
        try {
            Charset charset = mediaType != null ? mediaType.getCharset() : null;
            if (charset == null) {
                charset = StandardCharsets.UTF_8;
            }
            String contentString = new String(contentBytes, charset);
            if (StringUtils.isEmpty(contentString) || !MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                return contentString;
            }
            return sensitiveDataHelper.maskSensitiveDataFromJson(contentString);
        } catch (Exception ex) {
            log.warn("Exception when read and mask content, exception: {}", ex.getMessage(), ex);
            return "Content read error";
        }
    }
}
