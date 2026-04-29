package com.bondworks.quotes;

import com.bondworks.audit.AuditService;
import com.bondworks.cache.QuoteBoardCacheService;
import com.bondworks.common.Numbers;
import com.bondworks.events.OutboxService;
import com.bondworks.websocket.RfqMessagingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealerSimulator {
  private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
  private final JdbcTemplate jdbc;
  private final ScheduledExecutorService quoteScheduler;
  private final AuditService auditService;
  private final OutboxService outboxService;
  private final RfqMessagingService messagingService;
  private final QuoteBoardCacheService quoteBoardCacheService;

  public DealerSimulator(
      JdbcTemplate jdbc,
      ScheduledExecutorService quoteScheduler,
      AuditService auditService,
      OutboxService outboxService,
      RfqMessagingService messagingService,
      QuoteBoardCacheService quoteBoardCacheService) {
    this.jdbc = jdbc;
    this.quoteScheduler = quoteScheduler;
    this.auditService = auditService;
    this.outboxService = outboxService;
    this.messagingService = messagingService;
    this.quoteBoardCacheService = quoteBoardCacheService;
  }

  public void scheduleQuotes(UUID rfqId) {
    List<Map<String, Object>> dealers = jdbc.queryForList("""
        SELECT d.id AS dealer_id, d.code, d.personality, d.avg_latency_ms, d.base_spread_bps,
               d.response_probability, d.aggressiveness, d.size_capacity,
               r.bond_id, r.side, r.quantity, r.time_in_force_seconds,
               b.code AS bond_code, b.mid_price, b.mid_yield,
               COALESCE(a.inventory_adjustment_bps, 0) AS inventory_adjustment_bps
        FROM rfq_dealers rd
        JOIN rfqs r ON r.id = rd.rfq_id
        JOIN bonds b ON b.id = r.bond_id
        JOIN dealers d ON d.id = rd.dealer_id
        LEFT JOIN dealer_axes a ON a.dealer_id = d.id AND a.bond_id = b.id
        WHERE rd.rfq_id = ?
        ORDER BY d.code
        """, rfqId);

    for (Map<String, Object> dealer : dealers) {
      String code = String.valueOf(dealer.get("code"));
      long latency = demoLatency(dealer);
      if (!shouldRespond(dealer)) {
        continue;
      }
      quoteScheduler.schedule(() -> createQuote(rfqId, dealer), latency, TimeUnit.MILLISECONDS);
    }
  }

  @Transactional
  public void createQuote(UUID rfqId, Map<String, Object> dealer) {
    List<Map<String, Object>> rfqs = jdbc.queryForList("""
        SELECT r.id, r.status, r.expires_at, r.expires_at <= now() AS rfq_expired,
               r.side, r.quantity, r.time_in_force_seconds,
               b.mid_price, b.mid_yield
        FROM rfqs r
        JOIN bonds b ON b.id = r.bond_id
        WHERE r.id = ?
        """, rfqId);
    if (rfqs.isEmpty()) {
      return;
    }
    Map<String, Object> rfq = rfqs.getFirst();
    if (!List.of("OPEN", "QUOTING").contains(String.valueOf(rfq.get("status")))) {
      return;
    }
    if (Boolean.TRUE.equals(rfq.get("rfq_expired"))) {
      return;
    }

    String rfqSide = String.valueOf(rfq.get("side"));
    BigDecimal mid = Numbers.bd(rfq.get("mid_price"));
    BigDecimal midYield = Numbers.bd(rfq.get("mid_yield"));
    BigDecimal spreadBps = spread(dealer, Numbers.bd(rfq.get("quantity")));
    BigDecimal delta = mid.multiply(spreadBps).divide(TEN_THOUSAND, 8, RoundingMode.HALF_UP);
    BigDecimal price = rfqSide.equals("BUY") ? mid.add(delta) : mid.subtract(delta);
    BigDecimal yield = rfqSide.equals("BUY")
        ? midYield.subtract(delta.multiply(new BigDecimal("0.45")))
        : midYield.add(delta.multiply(new BigDecimal("0.45")));
    int latency = (int) demoLatency(dealer);
    int ttl = quoteTtlSeconds(dealer, ((Number) rfq.get("time_in_force_seconds")).intValue());
    UUID dealerId = (UUID) dealer.get("dealer_id");
    UUID quoteId;
    try {
      quoteId = jdbc.queryForObject("""
          INSERT INTO quotes (rfq_id, dealer_id, side, price, yield_value, spread_bps, quantity, status, expires_at, latency_ms, response_latency_ms, quote_reason)
          VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', LEAST((SELECT expires_at FROM rfqs WHERE id = ?), now() + (? * interval '1 second')), ?, ?, ?)
          RETURNING id
          """, UUID.class, rfqId, dealerId, rfqSide.equals("BUY") ? "OFFER" : "BID",
          Numbers.scale(price, 6), Numbers.scale(yield, 6), Numbers.scale(spreadBps, 6),
          rfq.get("quantity"), rfqId, ttl, latency, latency, quoteReason(dealer, rfqSide));
    } catch (DuplicateKeyException ex) {
      return;
    }

    jdbc.update("UPDATE rfqs SET status = 'QUOTING' WHERE id = ? AND status = 'OPEN'", rfqId);
    auditService.record("QUOTE_RECEIVED", "QUOTE", quoteId, null, Map.of(
        "rfq_id", rfqId,
        "dealer", dealer.get("code"),
        "price", Numbers.scale(price, 6),
        "spread_bps", Numbers.scale(spreadBps, 6),
        "latency_ms", latency));
    outboxService.enqueue("quote.received", quoteId, Map.of("quote_id", quoteId, "rfq_id", rfqId, "dealer", dealer.get("code")));
    Map<String, Object> quotePayload = new LinkedHashMap<>();
    quotePayload.put("event_type", "QUOTE_RECEIVED");
    quotePayload.put("rfq_id", rfqId);
    quotePayload.put("quote_id", quoteId);
    quotePayload.put("dealer_id", dealerId);
    quotePayload.put("dealer", dealer.get("code"));
    quotePayload.put("side", rfqSide.equals("BUY") ? "OFFER" : "BID");
    quotePayload.put("price", Numbers.scale(price, 6));
    quotePayload.put("yield_value", Numbers.scale(yield, 6));
    quotePayload.put("spread_bps", Numbers.scale(spreadBps, 6));
    quotePayload.put("quantity", rfq.get("quantity"));
    quotePayload.put("status", "ACTIVE");
    quotePayload.put("latency_ms", latency);
    quotePayload.put("response_latency_ms", latency);
    quotePayload.put("quote_reason", quoteReason(dealer, rfqSide));
    quotePayload.put("received_at", Instant.now().toString());
    quoteBoardCacheService.cacheQuote(rfqId, quoteId, quotePayload, ttl);
    messagingService.quote(rfqId, quotePayload);
    messagingService.status(rfqId, Map.of("event_type", "RFQ_STATUS", "rfq_id", rfqId, "status", "QUOTING"));
  }

  private boolean shouldRespond(Map<String, Object> dealer) {
    String bondCode = String.valueOf(dealer.get("bond_code"));
    String code = String.valueOf(dealer.get("code"));
    if (bondCode.equals("UST-10Y-2036") && code.equals("DB")) {
      return false;
    }
    if (bondCode.equals("UST-10Y-2036")) {
      return true;
    }
    BigDecimal probability = Numbers.bd(dealer.get("response_probability"));
    int deterministic = Math.abs((bondCode + code).hashCode() % 100);
    return deterministic < probability.multiply(new BigDecimal("100")).intValue();
  }

  private long demoLatency(Map<String, Object> dealer) {
    String bondCode = String.valueOf(dealer.get("bond_code"));
    String code = String.valueOf(dealer.get("code"));
    if (bondCode.equals("UST-10Y-2036")) {
      return switch (code) {
        case "JPM" -> 286L;
        case "CITI" -> 412L;
        case "GS" -> 830L;
        case "MS" -> 1200L;
        case "BOFA" -> 1900L;
        case "BARC" -> 4800L;
        default -> 2600L;
      };
    }
    long base = ((Number) dealer.get("avg_latency_ms")).longValue();
    return Math.max(100L, base + Math.abs(String.valueOf(dealer.get("code")).hashCode() % 350));
  }

  private int quoteTtlSeconds(Map<String, Object> dealer, int tif) {
    if (String.valueOf(dealer.get("bond_code")).equals("UST-10Y-2036") && String.valueOf(dealer.get("code")).equals("BARC")) {
      return 3;
    }
    return tif;
  }

  private BigDecimal spread(Map<String, Object> dealer, BigDecimal quantity) {
    BigDecimal base = Numbers.bd(dealer.get("base_spread_bps"));
    BigDecimal sizePenalty = quantity.compareTo(new BigDecimal("5000000")) > 0 ? new BigDecimal("1.2") : new BigDecimal("0.4");
    BigDecimal volatilityPenalty = new BigDecimal("0.6");
    BigDecimal inventoryAdjustment = Numbers.bd(dealer.get("inventory_adjustment_bps"));
    BigDecimal noise = BigDecimal.valueOf(Math.abs(String.valueOf(dealer.get("code")).hashCode() % 7)).divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
    if (String.valueOf(dealer.get("bond_code")).equals("UST-10Y-2036")) {
      return switch (String.valueOf(dealer.get("code"))) {
        case "CITI" -> new BigDecimal("5.7");
        case "GS" -> new BigDecimal("6.5");
        case "JPM" -> new BigDecimal("7.8");
        case "MS" -> new BigDecimal("9.3");
        case "BOFA" -> new BigDecimal("10.2");
        case "BARC" -> new BigDecimal("12.0");
        default -> base.add(sizePenalty).add(volatilityPenalty).add(inventoryAdjustment).add(noise);
      };
    }
    return base.add(sizePenalty).add(volatilityPenalty).add(inventoryAdjustment).add(noise).max(new BigDecimal("1.0"));
  }

  private String quoteReason(Map<String, Object> dealer, String rfqSide) {
    String code = String.valueOf(dealer.get("code"));
    String bondCode = String.valueOf(dealer.get("bond_code"));
    String sideLabel = rfqSide.equals("BUY") ? "offer" : "bid";
    String axe = String.valueOf(dealer.getOrDefault("inventory_adjustment_bps", "0"));
    return switch (code) {
      case "CITI" -> "CITI quote tightened because its simulated axe is to reduce " + bondCode + " inventory; inventory adjustment " + axe + " bps improves the " + sideLabel + ".";
      case "JPM" -> "JPM responded quickly with neutral inventory fit; price is wider than the best quote but strong on latency.";
      case "GS" -> "GS quote reflects good depth but a weaker inventory fit for this RFQ direction.";
      case "MS" -> "MS quote is wider due to simulated balance-sheet cost and slower response profile.";
      case "BARC" -> "BARC is included for price discovery; outlier profile creates slower and wider liquidity.";
      default -> code + " quote generated from seeded dealer profile, size penalty, volatility penalty, and inventory fit.";
    };
  }
}
