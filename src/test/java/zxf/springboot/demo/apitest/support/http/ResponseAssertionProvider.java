package zxf.springboot.demo.apitest.support.http;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public interface ResponseAssertionProvider {
    void assertResponse(ResponseEntity response);

    static ResponseAssertionProvider status(HttpStatus expectedStatus){
        return (response -> {
            Assertions.assertEquals(expectedStatus, response.getStatusCode());
        });
    }

    static ResponseAssertionProvider statusAndJson(HttpStatus expectedStatus){
        return (response -> {
            Assertions.assertEquals(expectedStatus, response.getStatusCode());
            Assertions.assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        });
    }
}
