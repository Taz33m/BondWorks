package com.bondworks.execution;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/execution-quality")
public class ExecutionQualityController {
  private final JdbcTemplate jdbc;

  public ExecutionQualityController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping
  Map<String, Object> quality() {
    Map<String, Object> summary = jdbc.queryForMap("""
        SELECT
          COUNT(t.id) AS total_trades,
          COALESCE(ROUND(AVG(a.slippage_bps), 2), 0) AS avg_slippage_bps,
          COALESCE(ROUND(AVG(a.cover_distance_bps), 2), 0) AS avg_cover_distance_bps,
          COALESCE(ROUND(AVG(a.quote_dispersion_bps), 2), 0) AS avg_quote_dispersion_bps,
          COALESCE(ROUND(SUM(a.missed_savings_usd), 2), 0) AS total_missed_savings_usd
        FROM trades t
        LEFT JOIN execution_analytics a ON a.trade_id = t.id
        """);

    return Map.of(
        "summary", summary,
        "slippage_over_time", jdbc.queryForList("""
            SELECT t.executed_at AS bucket, b.code AS bond_code, d.code AS dealer,
                   ROUND(a.slippage_bps, 2) AS slippage_bps
            FROM trades t
            JOIN bonds b ON b.id = t.bond_id
            JOIN dealers d ON d.id = t.dealer_id
            LEFT JOIN execution_analytics a ON a.trade_id = t.id
            ORDER BY t.executed_at ASC
            LIMIT 100
            """),
        "dealer_win_rate", jdbc.queryForList("""
            SELECT d.code AS dealer,
                   COUNT(DISTINCT q.rfq_id) AS rfqs,
                   COUNT(DISTINCT t.id) AS wins,
                   COALESCE(ROUND((COUNT(DISTINCT t.id)::numeric / NULLIF(COUNT(DISTINCT q.rfq_id), 0)::numeric) * 100, 1), 0) AS win_rate
            FROM dealers d
            LEFT JOIN quotes q ON q.dealer_id = d.id
            LEFT JOIN trades t ON t.dealer_id = d.id AND t.rfq_id = q.rfq_id
            GROUP BY d.code
            ORDER BY win_rate DESC, d.code ASC
            """),
        "quote_dispersion_by_instrument_type", jdbc.queryForList("""
            SELECT b.sector AS instrument_type,
                   COUNT(t.id) AS trades,
                   COALESCE(ROUND(AVG(a.quote_dispersion_bps), 2), 0) AS avg_quote_dispersion_bps
            FROM trades t
            JOIN bonds b ON b.id = t.bond_id
            LEFT JOIN execution_analytics a ON a.trade_id = t.id
            GROUP BY b.sector
            ORDER BY avg_quote_dispersion_bps DESC, b.sector ASC
            """),
        "dealer_latency_distribution", jdbc.queryForList("""
            SELECT d.code AS dealer,
                   COUNT(q.id) AS quotes,
                   COALESCE(ROUND(AVG(q.latency_ms), 0), 0) AS avg_latency_ms,
                   COALESCE(MIN(q.latency_ms), 0) AS min_latency_ms,
                   COALESCE(MAX(q.latency_ms), 0) AS max_latency_ms
            FROM dealers d
            LEFT JOIN quotes q ON q.dealer_id = d.id
            GROUP BY d.code
            ORDER BY avg_latency_ms ASC, d.code ASC
            """),
        "best_vs_executed_quote_history", jdbc.queryForList("""
            SELECT t.id AS trade_id, t.executed_at, b.code AS bond_code, t.side,
                   d.code AS executed_dealer, t.execution_price,
                   bd.code AS best_dealer, bq.price AS best_price,
                   cd.code AS cover_dealer, cq.price AS cover_price,
                   a.selected_quote_rank
            FROM trades t
            JOIN bonds b ON b.id = t.bond_id
            JOIN dealers d ON d.id = t.dealer_id
            LEFT JOIN execution_analytics a ON a.trade_id = t.id
            LEFT JOIN quotes bq ON bq.id = a.best_quote_id
            LEFT JOIN dealers bd ON bd.id = bq.dealer_id
            LEFT JOIN quotes cq ON cq.id = a.cover_quote_id
            LEFT JOIN dealers cd ON cd.id = cq.dealer_id
            ORDER BY t.executed_at DESC
            LIMIT 50
            """),
        "missed_savings_leaderboard", jdbc.queryForList("""
            SELECT d.code AS dealer,
                   COUNT(t.id) AS trades,
                   COALESCE(ROUND(SUM(a.missed_savings_usd), 2), 0) AS missed_savings_usd,
                   COALESCE(ROUND(AVG(a.slippage_bps), 2), 0) AS avg_slippage_bps
            FROM trades t
            JOIN dealers d ON d.id = t.dealer_id
            LEFT JOIN execution_analytics a ON a.trade_id = t.id
            GROUP BY d.code
            ORDER BY missed_savings_usd DESC, avg_slippage_bps DESC, d.code ASC
            LIMIT 10
            """));
  }
}
