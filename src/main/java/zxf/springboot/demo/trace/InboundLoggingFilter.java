package zxf.springboot.demo.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import zxf.springboot.demo.trace.mdc.MDCHelper;
import zxf.springboot.demo.trace.sensitive.SensitiveDataHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class InboundLoggingFilter extends OncePerRequestFilter {
    private final Boolean logging;
    private final MDCHelper mdcHelper;
    private final SensitiveDataHelper sensitiveDataHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 4096);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            mdcHelper.inject(request);
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            stopWatch.stop();
            if (logging) {
                // 重新注入MDC，避免过滤器链中其他过滤器修改MDC
                mdcHelper.inject(request);
                logRequestAndResponse(requestWrapper, responseWrapper, stopWatch.getTotalTimeMillis());
            }
            responseWrapper.copyBodyToResponse();
            mdcHelper.clean();
        }
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long elapsedMillis) {
        try {
            boolean isSuccess = HttpStatus.valueOf(response.getStatus()).is2xxSuccessful();

            boolean loggingEnabled = isSuccess ? log.isDebugEnabled() : log.isErrorEnabled();
            if (!loggingEnabled) {
                return;
            }

            Consumer<String> logger = isSuccess ? log::debug : log::error;
            logger.accept("=================================================Request begin(Inbound)=================================================");
            logger.accept(String.format("URL             : %s%s", request.getRequestURI(), StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString()));
            logger.accept(String.format("Method          : %s", request.getMethod()));
            logger.accept(String.format("Headers         : %s", formatHeaders(Collections.list(request.getHeaderNames()), request::getHeader)));
            logger.accept(String.format("Request Body    : %s", readAndMaskJsonContent(request.getContentType(), request.getContentAsByteArray(), request.getCharacterEncoding())));
            logger.accept("=================================================Request end(Inbound)=================================================");

            logger.accept(String.format("=================================================Response begin(Outbound, elapsed: %dms)=================================================", elapsedMillis));
            logger.accept(String.format("Status code     : %d", response.getStatus()));
            logger.accept(String.format("Headers         : %s", formatHeaders(response.getHeaderNames(), response::getHeader)));
            logger.accept(String.format("Response Body   : %s", readAndMaskJsonContent(response.getContentType(), response.getContentAsByteArray(), response.getCharacterEncoding())));
            logger.accept("=================================================Response end(Outbound)=================================================");
        } catch (Exception ex) {
            log.warn("Exception when log request and response, exception: {}", ex.getMessage(), ex);
        }
    }

    private String formatHeaders(Collection<String> headerNames, Function<String, String> headerValueProvider) {
        Function<String, String> sensitiveHeaderFormatProviderWrapper = headerName -> {
            String headerValue = sensitiveDataHelper.isSensitiveHeader(headerName) ? "***" : headerValueProvider.apply(headerName);
            return String.format("%s: %s", headerName, headerValue);
        };

        return headerNames.stream().map(sensitiveHeaderFormatProviderWrapper).collect(Collectors.joining(", ", "[", "]"));
    }

    private String readAndMaskJsonContent(String contentType, byte[] contentBytes, String encoding) {
        try {
            Charset charset = encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8;
            String contentString = new String(contentBytes, charset);
            if (StringUtils.isEmpty(contentString) || contentType == null || !contentType.toLowerCase().contains("json")) {
                return contentString;
            }
            return sensitiveDataHelper.maskSensitiveDataFromJson(contentString);
        } catch (Exception ex) {
            log.warn("Failed to read and mask content, exception: {}", ex.getMessage(), ex);
            return "Content read error";
        }
    }
}