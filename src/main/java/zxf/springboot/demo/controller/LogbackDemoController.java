package zxf.springboot.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/demo/logging")
public class LogbackDemoController {

    @GetMapping("/demo")
    public ResponseEntity<Map<String, String>> loggingDemo(@RequestParam(defaultValue = "world") String name) {
        MDC.put("demo.user", name);
        try {
            log.info("Processing logging demo request");
            log.debug("Debug level message with parameter: name={}", name);
            log.warn("Warning: this is a structured log demo");

            String result = "Hello, %s! Check logs for structured output.".formatted(name);
            return ResponseEntity.ok(Map.of("result", result));
        } finally {
            MDC.remove("demo.user");
        }
    }

    @GetMapping("/levels")
    public ResponseEntity<Map<String, String>> logLevels() {
        log.trace("TRACE level message");
        log.debug("DEBUG level message");
        log.info("INFO level message");
        log.warn("WARN level message");
        log.error("ERROR level message");
        return ResponseEntity.ok(Map.of("result", "All log levels demonstrated. Check console output."));
    }
}
