package zxf.springboot.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zxf.springboot.demo.observability.TracingDemoService;

import java.util.Map;

@RestController
@RequestMapping("/api/demo/tracing")
@RequiredArgsConstructor
public class TracingDemoController {
    private final TracingDemoService tracingDemoService;

    @GetMapping("/{input}")
    public ResponseEntity<Map<String, String>> tracedOperation(@PathVariable String input) {
        String result = tracingDemoService.tracedOperation(input);
        return ResponseEntity.ok(Map.of("result", result));
    }
}
