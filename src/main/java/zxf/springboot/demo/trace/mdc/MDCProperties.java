package zxf.springboot.demo.trace.mdc;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MDCProperties {
    @NotNull
    private List<Injection> injections;

    @Data
    public static class Injection {
        private String key;
        private String header;
    }
}