package zxf.springboot.demo.trace.sensitive;

import dev.blaauwendraad.masker.json.JsonMasker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Slf4j
public class SensitiveDataHelper {
    private final SensitiveProperties sensitiveProperties;
    private final JsonMasker jsonMasker;

    public SensitiveDataHelper(SensitiveProperties sensitiveProperties) {
        this.sensitiveProperties = sensitiveProperties;
        this.jsonMasker = CollectionUtils.isEmpty(sensitiveProperties.getJsonNames())
                ? null
                : JsonMasker.getMasker(sensitiveProperties.getJsonNames());
    }

    public String maskSensitiveDataFromJson(String content) {
        if (StringUtils.isEmpty(content) || jsonMasker == null) {
            return content;
        }
        if (log.isDebugEnabled()) {
            log.debug("Masking sensitive data from JSON, input length: {}", content.length());
        }
        String masked = jsonMasker.mask(content);
        if (log.isTraceEnabled()) {
            log.trace("Masked JSON output: {}", masked);
        }
        return masked;
    }

    public boolean isSensitiveHeader(String headerName) {
        if (StringUtils.isEmpty(headerName) || CollectionUtils.isEmpty(sensitiveProperties.getHeaders())) {
            return false;
        }

        boolean isSensitive = false;
        for (String sensitiveHeader : sensitiveProperties.getHeaders()) {
            if (headerName.equalsIgnoreCase(sensitiveHeader)) {
                isSensitive = true;
                break;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Checking if header '{}' is sensitive: {}", headerName, isSensitive);
        }
        return isSensitive;
    }
}
