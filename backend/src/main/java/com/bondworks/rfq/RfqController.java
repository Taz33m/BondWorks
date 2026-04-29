package com.bondworks.rfq;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rfqs")
public class RfqController {
  private final RfqService rfqService;

  public RfqController(RfqService rfqService) {
    this.rfqService = rfqService;
  }

  @PostMapping
  Map<String, Object> create(@RequestBody Map<String, Object> request) {
    return rfqService.create(request);
  }

  @GetMapping
  List<Map<String, Object>> recent() {
    return rfqService.recent();
  }

  @GetMapping("/{id}")
  Map<String, Object> get(@PathVariable UUID id) {
    return rfqService.get(id);
  }

  @GetMapping("/{id}/quotes")
  List<Map<String, Object>> quotes(@PathVariable UUID id) {
    return rfqService.quotes(id);
  }

  @PostMapping("/{id}/cancel")
  Map<String, Object> cancel(@PathVariable UUID id) {
    return rfqService.cancel(id);
  }
}
