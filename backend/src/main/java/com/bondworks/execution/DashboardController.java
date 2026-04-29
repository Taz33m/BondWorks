package com.bondworks.execution;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final JdbcTemplate jdbc;

  public DashboardController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping
  Map<String, Object> dashboard() {
    Map<String, Object> metrics = jdbc.queryForMap("""
        WITH dealer_coverage AS (
          SELECT
            COUNT(rd.dealer_id) AS requested_dealers,
            COUNT(q.id) FILTER (WHERE q.status IN ('ACTIVE', 'EXECUTED', 'INACTIVE')) AS dealer_responses
          FROM rfq_dealers rd
          LEFT JOIN quotes q ON q.rfq_id = rd.rfq_id AND q.dealer_id = rd.dealer_id
        )
        SELECT
          (SELECT COUNT(*) FROM rfqs WHERE status IN ('OPEN', 'QUOTING')) AS active_rfqs,
          (SELECT COALESCE(ROUND(AVG(slippage_bps), 2), 0) FROM execution_analytics) AS avg_slippage_bps,
          (SELECT COUNT(*) FROM trades WHERE executed_at::date = current_date) AS trades_today,
          COALESCE(ROUND((dealer_responses::numeric / NULLIF(requested_dealers, 0)::numeric) * 100, 1), 0) AS dealer_hit_rate
        FROM dealer_coverage
        """);
    return Map.of(
        "metrics", metrics,
        "active_rfqs", jdbc.queryForList("""
            SELECT r.id, b.ticker AS security, r.side, r.quantity, r.status,
                   (SELECT q.price FROM quotes q WHERE q.rfq_id = r.id ORDER BY
                     CASE WHEN r.side = 'BUY' THEN q.price END ASC,
                     CASE WHEN r.side = 'SELL' THEN q.price END DESC LIMIT 1) AS best_price
            FROM rfqs r
            JOIN bonds b ON b.id = r.bond_id
            WHERE r.status IN ('OPEN', 'QUOTING')
            ORDER BY r.created_at DESC
            LIMIT 10
            """),
        "recent_trades", jdbc.queryForList("""
            SELECT t.id, t.executed_at, b.ticker AS security, d.code AS dealer, t.side,
                   t.quantity, t.execution_price, a.slippage_bps
            FROM trades t
            JOIN bonds b ON b.id = t.bond_id
            JOIN dealers d ON d.id = t.dealer_id
            LEFT JOIN execution_analytics a ON a.trade_id = t.id
            ORDER BY t.executed_at DESC
            LIMIT 10
            """));
  }
}
