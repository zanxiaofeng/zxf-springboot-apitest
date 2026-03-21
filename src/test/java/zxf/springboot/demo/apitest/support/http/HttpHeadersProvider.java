package zxf.springboot.demo.apitest.support.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public interface HttpHeadersProvider {
    HttpHeaders headers();

    static HttpHeadersProvider commonHeaders() {
        return () -> {
            HttpHeaders headers = new HttpHeaders();
            return headers;
        };
    }

    static HttpHeadersProvider commonHeadersAndJson() {
        return () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return headers;
        };
    }
}
