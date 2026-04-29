package com.bondworks.common;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping({"/health", "/actuator/health"})
  Map<String, Object> health() {
    return Map.of(
        "status", "UP",
        "service", "bondworks-lite",
        "timestamp", Instant.now());
  }
}
