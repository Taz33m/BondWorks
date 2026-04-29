package com.bondworks.dealers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DealersController {
  private final DealerService dealerService;

  public DealersController(DealerService dealerService) {
    this.dealerService = dealerService;
  }

  @GetMapping("/api/dealers")
  List<Map<String, Object>> list() {
    return dealerService.list();
  }

  @GetMapping("/api/dealers/performance")
  Map<String, Object> performance() {
    return Map.of("summary", dealerService.performanceSummary(), "dealers", dealerService.performance());
  }

  @GetMapping("/api/rfqs/recommendations")
  List<Map<String, Object>> recommendations(
      @RequestParam("bond_id") UUID bondId,
      @RequestParam String side,
      @RequestParam BigDecimal quantity) {
    return dealerService.recommendations(bondId, side, quantity);
  }
}
