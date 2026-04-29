package com.bondworks.execution;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rfqs/{rfqId}")
public class ExecutionController {
  private final ExecutionService executionService;

  public ExecutionController(ExecutionService executionService) {
    this.executionService = executionService;
  }

  @PostMapping("/execute")
  ResponseEntity<Map<String, Object>> execute(
      @PathVariable UUID rfqId,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody Map<String, Object> request) {
    ExecutionService.ExecutionResult result = executionService.execute(rfqId, idempotencyKey, request);
    return ResponseEntity.status(result.status()).body(result.body());
  }
}
