package com.bondworks.bonds;

import com.bondworks.marketdata.MarketDataService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bonds")
public class BondsController {
  private final BondService bondService;
  private final MarketDataService marketDataService;

  public BondsController(BondService bondService, MarketDataService marketDataService) {
    this.bondService = bondService;
    this.marketDataService = marketDataService;
  }

  @GetMapping
  List<Map<String, Object>> list() {
    return bondService.list();
  }

  @GetMapping("/{id}/market-prints")
  List<Map<String, Object>> marketPrints(@PathVariable UUID id) {
    return marketDataService.prints(id);
  }
}
