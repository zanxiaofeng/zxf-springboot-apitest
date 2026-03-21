package zxf.springboot.demo.trace.sensitive;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Data
@Validated
public class SensitiveProperties {
    @NotNull
    private Set<String> headers;
    @NotNull
    private Set<String> jsonNames;
}