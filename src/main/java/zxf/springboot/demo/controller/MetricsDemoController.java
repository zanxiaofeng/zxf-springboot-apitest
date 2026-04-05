package zxf.springboot.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.observability.MetricsDemoService;

import java.util.Map;

@RestController
@RequestMapping("/api/demo/metrics")
@RequiredArgsConstructor
public class MetricsDemoController {
    private final MetricsDemoService metricsDemoService;

    @GetMapping("/timed")
    public ResponseEntity<Map<String, String>> timedOperation() {
        String result = metricsDemoService.timedOperation();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @GetMapping("/counted")
    public ResponseEntity<Map<String, String>> countedOperation() {
        String result = metricsDemoService.countedOperation();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/active-users")
    public ResponseEntity<Map<String, Object>> updateActiveUsers(@RequestParam int delta) {
        int active = metricsDemoService.updateActiveUsers(delta);
        return ResponseEntity.ok(Map.of("activeUsers", active));
    }
}
