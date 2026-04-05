package zxf.springboot.demo.observability;

import io.micrometer.tracing.SpanName;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TracingDemoService {
    private final Tracer tracer;

    @SpanName("demo.tracing.parent")
    public String tracedOperation(String input) {
        var currentSpan = tracer.currentSpan();
        String traceId = currentSpan != null ? currentSpan.context().traceId() : "no-trace";
        String spanId = currentSpan != null ? currentSpan.context().spanId() : "no-span";

        log.info("Traced operation - traceId: {}, spanId: {}, input: {}", traceId, spanId, input);

        childOperation(input);

        return "Traced: traceId=%s, spanId=%s, input=%s".formatted(traceId, spanId, input);
    }

    private void childOperation(String input) {
        var newSpan = tracer.nextSpan().name("demo.tracing.child").start();
        try (var ignored = tracer.withSpan(newSpan)) {
            log.info("Child span processing: {}", input.toUpperCase());
        } finally {
            newSpan.end();
        }
    }
}
