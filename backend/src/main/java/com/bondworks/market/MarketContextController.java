package com.bondworks.market;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketContextController {
  private final MarketContextService marketContextService;

  public MarketContextController(MarketContextService marketContextService) {
    this.marketContextService = marketContextService;
  }

  @GetMapping("/yield-curve")
  List<Map<String, Object>> yieldCurve() {
    return marketContextService.latestYieldCurve();
  }

  @GetMapping("/reference-rates")
  List<Map<String, Object>> referenceRates() {
    return marketContextService.latestReferenceRates();
  }

  @GetMapping("/context")
  Map<String, Object> context() {
    return marketContextService.context();
  }
}
