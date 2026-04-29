package com.bondworks.analytics;

import com.bondworks.audit.AuditService;
import com.bondworks.common.Numbers;
import com.bondworks.marketdata.MarketDataService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
  private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
  private static final BigDecimal HUNDRED = new BigDecimal("100");
  private final JdbcTemplate jdbc;
  private final MarketDataService marketDataService;
  private final AuditService auditService;

  public AnalyticsService(JdbcTemplate jdbc, MarketDataService marketDataService, AuditService auditService) {
    this.jdbc = jdbc;
    this.marketDataService = marketDataService;
    this.auditService = auditService;
  }

  public Map<String, Object> generate(UUID tradeId, List<Map<String, Object>> rankedQuotes, int selectedRank) {
    Map<String, Object> trade = jdbc.queryForList("""
        SELECT t.id AS trade_id, t.rfq_id, t.quote_id, t.bond_id, t.dealer_id, t.user_id, t.side,
               t.quantity, t.execution_price, t.execution_yield, t.executed_at,
               r.created_at AS rfq_created_at,
               b.mid_price, b.mid_yield,
               q.latency_ms AS selected_latency_ms
        FROM trades t
        JOIN rfqs r ON r.id = t.rfq_id
        JOIN bonds b ON b.id = t.bond_id
        JOIN quotes q ON q.id = t.quote_id
        WHERE t.id = ?
        """, tradeId).getFirst();

    String side = String.valueOf(trade.get("side"));
    BigDecimal quantity = Numbers.bd(trade.get("quantity"));
    BigDecimal executionPrice = Numbers.bd(trade.get("execution_price"));
    BigDecimal midPrice = Numbers.bd(trade.get("mid_price"));
    Map<String, Object> best = rankedQuotes.isEmpty() ? null : rankedQuotes.getFirst();
    Map<String, Object> worst = rankedQuotes.isEmpty() ? null : rankedQuotes.getLast();
    Map<String, Object> cover = rankedQuotes.size() < 2 ? null : (selectedRank == 1 ? rankedQuotes.get(1) : rankedQuotes.getFirst());
    String competitionStatus = rankedQuotes.size() < 2 ? "INSUFFICIENT_COMPETITION" : "COMPETITIVE";

    BigDecimal bestPrice = best == null ? null : Numbers.bd(best.get("price"));
    BigDecimal worstPrice = worst == null ? null : Numbers.bd(worst.get("price"));
    BigDecimal coverPrice = cover == null ? null : Numbers.bd(cover.get("price"));
    BigDecimal slippage = side.equals("BUY")
        ? Numbers.div(executionPrice.subtract(midPrice), midPrice).multiply(TEN_THOUSAND)
        : Numbers.div(midPrice.subtract(executionPrice), midPrice).multiply(TEN_THOUSAND);
    BigDecimal spreadPaid = Numbers.div(executionPrice.subtract(midPrice).abs(), midPrice).multiply(TEN_THOUSAND);
    BigDecimal dispersion = bestPrice == null || worstPrice == null || rankedQuotes.size() < 2
        ? null
        : Numbers.div(bestPrice.subtract(worstPrice).abs(), midPrice).multiply(TEN_THOUSAND);
    BigDecimal missedSavings = bestPrice == null
        ? BigDecimal.ZERO
        : side.equals("BUY")
            ? executionPrice.subtract(bestPrice).max(BigDecimal.ZERO).divide(HUNDRED).multiply(quantity)
            : bestPrice.subtract(executionPrice).max(BigDecimal.ZERO).divide(HUNDRED).multiply(quantity);
    BigDecimal coverDistance = coverPrice == null
        ? null
        : Numbers.div(executionPrice.subtract(coverPrice).abs(), midPrice).multiply(TEN_THOUSAND);
    BigDecimal improvementVsCover = coverPrice == null
        ? null
        : side.equals("BUY")
            ? coverPrice.subtract(executionPrice).max(BigDecimal.ZERO).divide(HUNDRED).multiply(quantity)
            : executionPrice.subtract(coverPrice).max(BigDecimal.ZERO).divide(HUNDRED).multiply(quantity);
    BigDecimal recentPrint = marketDataService.recentPrint((UUID) trade.get("bond_id"));
    BigDecimal tapeVwap = marketDataService.tapeVwap((UUID) trade.get("bond_id"));
    BigDecimal executionVsTape = tapeVwap == null
        ? null
        : side.equals("BUY")
            ? Numbers.div(executionPrice.subtract(tapeVwap), tapeVwap).multiply(TEN_THOUSAND)
            : Numbers.div(tapeVwap.subtract(executionPrice), tapeVwap).multiply(TEN_THOUSAND);
    Integer timeToExecuteMs = jdbc.queryForObject("""
        SELECT ROUND(EXTRACT(EPOCH FROM (t.executed_at - r.created_at)) * 1000)::int
        FROM trades t
        JOIN rfqs r ON r.id = t.rfq_id
        WHERE t.id = ?
        """, Integer.class, tradeId);

    jdbc.update("""
        INSERT INTO execution_analytics (
          trade_id, best_quote_id, cover_quote_id, selected_quote_rank, competition_status,
          slippage_bps, spread_paid_bps, quote_dispersion_bps, response_latency_ms, time_to_execute_ms,
          missed_savings_usd, cover_price, cover_distance_bps, price_improvement_vs_cover_usd,
          recent_print_price, tape_vwap, execution_vs_tape_bps
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        tradeId,
        best == null ? null : best.get("id"),
        cover == null ? null : cover.get("id"),
        selectedRank,
        competitionStatus,
        Numbers.scale(slippage, 6),
        Numbers.scale(spreadPaid, 6),
        Numbers.scale(dispersion, 6),
        trade.get("selected_latency_ms"),
        timeToExecuteMs,
        Numbers.scale(missedSavings, 2),
        Numbers.scale(coverPrice, 6),
        Numbers.scale(coverDistance, 6),
        Numbers.scale(improvementVsCover, 2),
        Numbers.scale(recentPrint, 6),
        Numbers.scale(tapeVwap, 6),
        Numbers.scale(executionVsTape, 6));
    auditService.record("ANALYTICS_GENERATED", "TRADE", tradeId, null, Map.of(
        "selected_quote_rank", selectedRank,
        "competition_status", competitionStatus,
        "cover_price", coverPrice == null ? "N/A" : coverPrice,
        "tape_vwap", tapeVwap == null ? "N/A" : tapeVwap));
    return getForTrade(tradeId);
  }

  public Map<String, Object> getForTrade(UUID tradeId) {
    return jdbc.queryForList("""
        SELECT a.*, bq.price AS best_quote_price, bd.code AS best_quote_dealer,
               cq.price AS cover_quote_price, cd.code AS cover_quote_dealer
        FROM execution_analytics a
        LEFT JOIN quotes bq ON bq.id = a.best_quote_id
        LEFT JOIN dealers bd ON bd.id = bq.dealer_id
        LEFT JOIN quotes cq ON cq.id = a.cover_quote_id
        LEFT JOIN dealers cd ON cd.id = cq.dealer_id
        WHERE a.trade_id = ?
        """, tradeId).getFirst();
  }
}
