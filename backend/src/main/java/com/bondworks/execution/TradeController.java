package com.bondworks.execution;

import com.bondworks.analytics.AnalyticsService;
import com.bondworks.common.ApiException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {
  private final JdbcTemplate jdbc;
  private final AnalyticsService analyticsService;

  public TradeController(JdbcTemplate jdbc, AnalyticsService analyticsService) {
    this.jdbc = jdbc;
    this.analyticsService = analyticsService;
  }

  @GetMapping
  List<Map<String, Object>> list() {
    return jdbc.queryForList("""
        SELECT t.id, t.rfq_id, t.side, t.quantity, t.execution_price, t.execution_yield, t.executed_at,
               b.code AS bond_code, b.ticker, d.code AS dealer,
               a.slippage_bps, a.selected_quote_rank
        FROM trades t
        JOIN bonds b ON b.id = t.bond_id
        JOIN dealers d ON d.id = t.dealer_id
        LEFT JOIN execution_analytics a ON a.trade_id = t.id
        ORDER BY t.executed_at DESC
        LIMIT 50
        """);
  }

  @GetMapping("/{id}")
  Map<String, Object> get(@PathVariable UUID id) {
    List<Map<String, Object>> rows = jdbc.queryForList("""
        SELECT t.id, t.rfq_id, t.quote_id, t.side, t.quantity, t.execution_price, t.execution_yield,
               t.settlement_date, t.executed_at,
               b.code AS bond_code, b.ticker, b.issuer, b.mid_price, b.mid_yield,
               d.code AS dealer, d.name AS dealer_name
        FROM trades t
        JOIN bonds b ON b.id = t.bond_id
        JOIN dealers d ON d.id = t.dealer_id
        WHERE t.id = ?
        """, id);
    if (rows.isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "TRADE_NOT_FOUND", "Trade not found");
    }
    Map<String, Object> trade = rows.getFirst();
    trade.put("analytics", analyticsService.getForTrade(id));
    return trade;
  }

  @GetMapping("/{id}/analytics")
  Map<String, Object> analytics(@PathVariable UUID id) {
    return analyticsService.getForTrade(id);
  }
}
