package com.bondworks.rfq;

import com.bondworks.audit.AuditService;
import com.bondworks.auth.AuthContext;
import com.bondworks.auth.CurrentUser;
import com.bondworks.bonds.BondService;
import com.bondworks.cache.QuoteBoardCacheService;
import com.bondworks.common.ApiException;
import com.bondworks.events.OutboxService;
import com.bondworks.quotes.DealerSimulator;
import com.bondworks.websocket.RfqMessagingService;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RfqService {
  private final JdbcTemplate jdbc;
  private final BondService bondService;
  private final AuditService auditService;
  private final OutboxService outboxService;
  private final DealerSimulator dealerSimulator;
  private final RfqMessagingService messagingService;
  private final QuoteBoardCacheService quoteBoardCacheService;

  public RfqService(
      JdbcTemplate jdbc,
      BondService bondService,
      AuditService auditService,
      OutboxService outboxService,
      DealerSimulator dealerSimulator,
      RfqMessagingService messagingService,
      QuoteBoardCacheService quoteBoardCacheService) {
    this.jdbc = jdbc;
    this.bondService = bondService;
    this.auditService = auditService;
    this.outboxService = outboxService;
    this.dealerSimulator = dealerSimulator;
    this.messagingService = messagingService;
    this.quoteBoardCacheService = quoteBoardCacheService;
  }

  @Transactional
  public Map<String, Object> create(Map<String, Object> request) {
    CurrentUser user = AuthContext.require();
    Object bondIdentifier = request.get("bond_id");
    if (isBlankish(bondIdentifier)) {
      bondIdentifier = request.get("bond_code");
    }
    if (isBlankish(bondIdentifier)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "BOND_REQUIRED", "RFQ requires bond_id or bond_code");
    }
    UUID bondId = bondService.resolveId(String.valueOf(bondIdentifier));
    String side = String.valueOf(request.getOrDefault("side", "BUY")).toUpperCase();
    if (!side.equals("BUY") && !side.equals("SELL")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SIDE", "RFQ side must be BUY or SELL");
    }
    Object quantityValue = request.get("quantity");
    if (isBlankish(quantityValue)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY", "RFQ quantity is required");
    }
    BigDecimal quantity;
    try {
      quantity = new BigDecimal(String.valueOf(quantityValue));
    } catch (NumberFormatException ex) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY", "RFQ quantity must be numeric");
    }
    if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY", "RFQ quantity must be positive");
    }
    int tif = Integer.parseInt(String.valueOf(request.getOrDefault("time_in_force_seconds", 30)));
    if (tif <= 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TIF", "Time in force must be positive");
    }
    LocalDate settlementDate = LocalDate.parse(String.valueOf(request.getOrDefault("settlement_date", LocalDate.now().plusDays(1).toString())));
    List<UUID> dealerIds = resolveDealerIds(request.get("dealer_ids"), request.get("dealer_codes"));
    if (dealerIds.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "NO_DEALERS", "At least one dealer is required");
    }

    UUID rfqId = jdbc.queryForObject("""
        INSERT INTO rfqs (user_id, bond_id, side, quantity, status, time_in_force_seconds, settlement_date, expires_at)
        VALUES (?, ?, ?, ?, 'OPEN', ?, ?, now() + (? * interval '1 second'))
        RETURNING id
        """, UUID.class, user.id(), bondId, side, quantity, tif, Date.valueOf(settlementDate), tif);

    for (UUID dealerId : dealerIds) {
      jdbc.update("INSERT INTO rfq_dealers (rfq_id, dealer_id, status) VALUES (?, ?, 'SENT')", rfqId, dealerId);
    }

    auditService.record("RFQ_CREATED", "RFQ", rfqId, user.id(), Map.of(
        "bond_id", bondId, "side", side, "quantity", quantity, "time_in_force_seconds", tif));
    auditService.record("RFQ_SENT_TO_DEALERS", "RFQ", rfqId, user.id(), Map.of("dealer_count", dealerIds.size()));
    outboxService.enqueue("rfq.created", rfqId, Map.of("rfq_id", rfqId, "bond_id", bondId, "side", side, "quantity", quantity));
    quoteBoardCacheService.cacheRfq(rfqId, "OPEN", tif);
    messagingService.status(rfqId, Map.of("event_type", "RFQ_OPENED", "rfq_id", rfqId, "status", "OPEN"));

    dealerSimulator.scheduleQuotes(rfqId);
    return get(rfqId);
  }

  public Map<String, Object> get(UUID rfqId) {
    List<Map<String, Object>> rows = jdbc.queryForList("""
        SELECT r.id, r.side, r.quantity, r.status, r.time_in_force_seconds, r.settlement_date,
               r.created_at, r.expires_at,
               b.id AS bond_id, b.code AS bond_code, b.ticker, b.issuer, b.sector, b.rating,
               b.mid_price, b.mid_yield
        FROM rfqs r
        JOIN bonds b ON b.id = r.bond_id
        WHERE r.id = ?
        """, rfqId);
    if (rows.isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "RFQ_NOT_FOUND", "RFQ not found");
    }
    Map<String, Object> rfq = rows.getFirst();
    rfq.put("dealers", dealers(rfqId));
    return rfq;
  }

  public List<Map<String, Object>> recent() {
    return jdbc.queryForList("""
        SELECT r.id, r.side, r.quantity, r.status, r.created_at, r.expires_at,
               b.code AS bond_code, b.ticker,
               (SELECT q.price FROM quotes q WHERE q.rfq_id = r.id ORDER BY
                 CASE WHEN r.side = 'BUY' THEN q.price END ASC,
                 CASE WHEN r.side = 'SELL' THEN q.price END DESC
                LIMIT 1) AS best_price
        FROM rfqs r
        JOIN bonds b ON b.id = r.bond_id
        ORDER BY r.created_at DESC
        LIMIT 20
        """);
  }

  public List<Map<String, Object>> dealers(UUID rfqId) {
    return jdbc.queryForList("""
        SELECT d.id, d.code, d.name, rd.status
        FROM rfq_dealers rd
        JOIN dealers d ON d.id = rd.dealer_id
        WHERE rd.rfq_id = ?
        ORDER BY d.code
        """, rfqId);
  }

  public List<Map<String, Object>> quotes(UUID rfqId) {
    return jdbc.queryForList("""
        SELECT q.id, q.rfq_id, q.side, q.price, q.yield_value, q.spread_bps, q.quantity,
               q.status, q.received_at, q.expires_at, q.latency_ms, q.response_latency_ms, q.quote_reason,
               d.id AS dealer_id, d.code AS dealer, d.name AS dealer_name
        FROM quotes q
        JOIN dealers d ON d.id = q.dealer_id
        WHERE q.rfq_id = ?
        ORDER BY q.received_at ASC
        """, rfqId);
  }

  @Transactional
  public Map<String, Object> cancel(UUID rfqId) {
    CurrentUser user = AuthContext.require();
    Map<String, Object> rfq = get(rfqId);
    String status = String.valueOf(rfq.get("status"));
    if (status.equals("EXECUTED")) {
      throw new ApiException(HttpStatus.CONFLICT, "RFQ_ALREADY_EXECUTED", "Executed RFQs cannot be cancelled");
    }
    if (status.equals("CANCELLED")) {
      return rfq;
    }
    jdbc.update("UPDATE rfqs SET status = 'CANCELLED' WHERE id = ?", rfqId);
    jdbc.update("UPDATE quotes SET status = 'INACTIVE' WHERE rfq_id = ? AND status = 'ACTIVE'", rfqId);
    auditService.record("RFQ_CANCELLED", "RFQ", rfqId, user.id(), Map.of("status", "CANCELLED"));
    outboxService.enqueue("rfq.expired", rfqId, Map.of("rfq_id", rfqId, "status", "CANCELLED"));
    messagingService.status(rfqId, Map.of("event_type", "RFQ_CANCELLED", "rfq_id", rfqId, "status", "CANCELLED"));
    return get(rfqId);
  }

  @Scheduled(fixedDelay = 1000)
  public void expireQuotesAndRfqs() {
    List<Map<String, Object>> expiredQuotes = jdbc.queryForList("""
        UPDATE quotes
        SET status = 'EXPIRED'
        WHERE status = 'ACTIVE' AND expires_at <= now()
        RETURNING id, rfq_id, dealer_id, price
        """);
    for (Map<String, Object> quote : expiredQuotes) {
      UUID quoteId = (UUID) quote.get("id");
      UUID rfqId = (UUID) quote.get("rfq_id");
      auditService.record("QUOTE_EXPIRED", "QUOTE", quoteId, null, Map.of("rfq_id", rfqId, "price", quote.get("price")));
      outboxService.enqueue("quote.expired", quoteId, Map.of("quote_id", quoteId, "rfq_id", rfqId));
      messagingService.quote(rfqId, Map.of("event_type", "QUOTE_EXPIRED", "quote_id", quoteId, "rfq_id", rfqId));
    }

    List<Map<String, Object>> expiredRfqs = jdbc.queryForList("""
        UPDATE rfqs
        SET status = 'EXPIRED'
        WHERE status IN ('OPEN', 'QUOTING') AND expires_at <= now()
        RETURNING id
        """);
    for (Map<String, Object> rfq : expiredRfqs) {
      UUID rfqId = (UUID) rfq.get("id");
      auditService.record("RFQ_EXPIRED", "RFQ", rfqId, null, Map.of("status", "EXPIRED"));
      outboxService.enqueue("rfq.expired", rfqId, Map.of("rfq_id", rfqId, "status", "EXPIRED"));
      messagingService.status(rfqId, Map.of("event_type", "RFQ_EXPIRED", "rfq_id", rfqId, "status", "EXPIRED"));
    }
  }

  @SuppressWarnings("unchecked")
  private List<UUID> resolveDealerIds(Object dealerIds, Object dealerCodes) {
    List<String> values = new ArrayList<>();
    if (dealerIds instanceof List<?> ids) {
      for (Object id : ids) values.add(String.valueOf(id));
    }
    if (values.isEmpty() && dealerCodes instanceof List<?> codes) {
      for (Object code : codes) values.add(String.valueOf(code));
    }
    List<UUID> ids = new ArrayList<>();
    for (String value : values) {
      try {
        ids.add(UUID.fromString(value));
      } catch (IllegalArgumentException ignored) {
        List<UUID> matches = jdbc.query("SELECT id FROM dealers WHERE code = ?", (rs, rowNum) -> (UUID) rs.getObject("id"), value.toUpperCase());
        if (!matches.isEmpty()) {
          ids.add(matches.getFirst());
        }
      }
    }
    return ids;
  }

  private boolean isBlankish(Object value) {
    if (value == null) return true;
    String text = String.valueOf(value);
    return text.isBlank() || text.equals("null");
  }
}
