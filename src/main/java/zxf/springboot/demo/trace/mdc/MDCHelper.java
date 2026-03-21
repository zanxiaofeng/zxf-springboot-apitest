package zxf.springboot.demo.trace.mdc;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

@Slf4j
@AllArgsConstructor
public class MDCHelper {
    private final MDCProperties mdcProperties;

    public void inject(HttpServletRequest request) {
        if (!CollectionUtils.isEmpty(mdcProperties.getInjections())) {
            if (log.isDebugEnabled()) {
                log.debug("Injecting MDC properties from HTTP headers");
            }
            for (MDCProperties.Injection injection : mdcProperties.getInjections()) {
                String headerValue = request.getHeader(injection.getHeader());
                if (headerValue != null) {
                    MDC.put(injection.getKey(), headerValue);
                    if (log.isTraceEnabled()) {
                        log.trace("Injected MDC: {} -> {}", injection.getKey(), headerValue);
                    }
                }
            }
        }
    }

    public void clean() {
        if (!CollectionUtils.isEmpty(mdcProperties.getInjections())) {
            if (log.isDebugEnabled()) {
                log.debug("Cleaning MDC properties");
            }
            for (MDCProperties.Injection injection : mdcProperties.getInjections()) {
                MDC.remove(injection.getKey());
            }
        }
    }
}
