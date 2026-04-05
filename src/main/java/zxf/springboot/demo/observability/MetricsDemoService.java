package zxf.springboot.demo.observability;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class MetricsDemoService {
    private final Counter demoCounter;
    private final AtomicInteger activeUsers;

    public MetricsDemoService(MeterRegistry meterRegistry) {
        this.demoCounter = meterRegistry.counter("demo.counter", "type", "example");
        this.activeUsers = meterRegistry.gauge("demo.active.users", new AtomicInteger(0));
    }

    @Timed(value = "demo.operation.time", description = "Time spent on demo operation", percentiles = {0.5, 0.95, 0.99})
    public String timedOperation() {
        log.info("Executing timed operation");
        try {
            Thread.sleep((long) (Math.random() * 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Timed operation completed";
    }

    @Counted(value = "demo.operation.count", description = "Number of times counted operation was invoked")
    public String countedOperation() {
        log.info("Executing counted operation");
        demoCounter.increment();
        return "Counted operation completed (count: " + demoCounter.count() + ")";
    }

    public int updateActiveUsers(int delta) {
        int newValue = activeUsers.updateAndGet(v -> Math.max(0, v + delta));
        log.info("Active users updated: {}", newValue);
        return newValue;
    }
}
