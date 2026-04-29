package com.bondworks.dealers;

import com.bondworks.common.Numbers;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DealerService {
  private final JdbcTemplate jdbc;

  public DealerService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> list() {
    return jdbc.queryForList("""
        SELECT id, code, name, personality, avg_latency_ms, base_spread_bps,
               response_probability, aggressiveness, size_capacity
        FROM dealers
        ORDER BY code
        """);
  }

  public List<Map<String, Object>> recommendations(UUID bondId, String side, BigDecimal quantity) {
    return jdbc.queryForList("""
        SELECT d.id, d.code, d.name, d.personality, d.avg_latency_ms,
               COALESCE(a.stance, 'NEUTRAL') AS stance,
               COALESCE(a.description, 'No specific axe; neutral inventory fit') AS rationale,
               ROUND(100 * (
                 0.30 * s.historical_price_quality
               + 0.20 * s.response_rate
               + 0.15 * s.latency_score
               + 0.15 * s.sector_win_rate
               + 0.10 * s.size_capacity
               + 0.10 * COALESCE(a.inventory_fit_score, 0.55)
               ), 0) AS score,
               s.historical_price_quality, s.response_rate, s.latency_score,
               s.sector_win_rate, s.size_capacity, COALESCE(a.inventory_fit_score, 0.55) AS inventory_fit
        FROM dealers d
        JOIN bonds b ON b.id = ?
        JOIN dealer_statistics_snapshot s ON s.dealer_id = d.id AND s.sector = b.sector
        LEFT JOIN dealer_axes a ON a.dealer_id = d.id AND a.bond_id = b.id
        ORDER BY score DESC, d.code
        """, bondId);
  }

  public List<Map<String, Object>> performance() {
    return jdbc.queryForList("""
        WITH stats AS (
          SELECT d.id, d.code,
                 COUNT(DISTINCT rd.rfq_id) AS rfqs,
                 COUNT(q.id) AS quotes,
                 COUNT(t.id) AS wins,
                 COALESCE(AVG(q.spread_bps), d.base_spread_bps) AS avg_spread,
                 COALESCE(AVG(q.latency_ms), d.avg_latency_ms) AS avg_latency
          FROM dealers d
          LEFT JOIN rfq_dealers rd ON rd.dealer_id = d.id
          LEFT JOIN quotes q ON q.dealer_id = d.id
          LEFT JOIN trades t ON t.dealer_id = d.id
          GROUP BY d.id, d.code, d.base_spread_bps, d.avg_latency_ms
        )
        SELECT code AS dealer, rfqs, quotes,
               CASE WHEN quotes = 0 THEN 0 ELSE ROUND((wins::numeric / quotes::numeric) * 100, 1) END AS win_rate,
               ROUND(avg_spread, 1) AS avg_spread_bps,
               ROUND(avg_latency, 0) AS avg_latency_ms
        FROM stats
        ORDER BY win_rate DESC, avg_spread_bps ASC
        """);
  }

  public Map<String, Object> performanceSummary() {
    List<Map<String, Object>> rows = performance();
    BigDecimal totalRfqs = BigDecimal.ZERO;
    BigDecimal totalWins = BigDecimal.ZERO;
    BigDecimal totalQuotes = BigDecimal.ZERO;
    BigDecimal weightedLatency = BigDecimal.ZERO;
    String primary = rows.isEmpty() ? "N/A" : String.valueOf(rows.getFirst().get("dealer"));
    for (Map<String, Object> row : rows) {
      BigDecimal rfqs = Numbers.bd(row.get("rfqs"));
      BigDecimal quotes = Numbers.bd(row.get("quotes"));
      totalRfqs = totalRfqs.add(rfqs);
      totalQuotes = totalQuotes.add(quotes);
      totalWins = totalWins.add(Numbers.bd(row.get("win_rate")).multiply(quotes));
      weightedLatency = weightedLatency.add(Numbers.bd(row.get("avg_latency_ms")).multiply(quotes));
    }
    BigDecimal winRate = totalQuotes.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : Numbers.div(totalWins, totalQuotes);
    BigDecimal latency = totalQuotes.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : Numbers.div(weightedLatency, totalQuotes);
    return Map.of(
        "total_rfqs_sent", totalRfqs,
        "aggregate_win_rate", Numbers.scale(winRate, 1),
        "avg_execution_latency_ms", Numbers.scale(latency, 0),
        "primary_counterparty", primary);
  }
}
