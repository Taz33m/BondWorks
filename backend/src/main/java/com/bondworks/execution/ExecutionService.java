package com.bondworks.execution;

import com.bondworks.analytics.AnalyticsService;
import com.bondworks.audit.AuditService;
import com.bondworks.auth.AuthContext;
import com.bondworks.auth.CurrentUser;
import com.bondworks.cache.QuoteBoardCacheService;
import com.bondworks.common.ApiException;
import com.bondworks.common.Numbers;
import com.bondworks.events.OutboxService;
import com.bondworks.websocket.RfqMessagingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionService {
  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;
  private final AnalyticsService analyticsService;
  private final AuditService auditService;
  private final OutboxService outboxService;
  private final RfqMessagingService messagingService;
  private final QuoteBoardCacheService quoteBoardCacheService;

  public ExecutionService(
      JdbcTemplate jdbc,
      ObjectMapper objectMapper,
      AnalyticsService analyticsService,
      AuditService auditService,
      OutboxService outboxService,
      RfqMessagingService messagingService,
      QuoteBoardCacheService quoteBoardCacheService) {
    this.jdbc = jdbc;
    this.objectMapper = objectMapper;
    this.analyticsService = analyticsService;
    this.auditService = auditService;
    this.outboxService = outboxService;
    this.messagingService = messagingService;
    this.quoteBoardCacheService = quoteBoardCacheService;
  }

  @Transactional
  public ExecutionResult execute(UUID rfqId, String idempotencyKey, Map<String, Object> request) {
    CurrentUser user = AuthContext.require();
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED", "Execution requires Idempotency-Key");
    }
    String endpoint = "POST /api/rfqs/" + rfqId + "/execute";
    String requestHash = requestHash(request);
    int inserted = jdbc.update("""
        INSERT INTO idempotency_keys (user_id, endpoint, idempotency_key, request_hash, status, expires_at)
        VALUES (?, ?, ?, ?, 'PROCESSING', now() + interval '24 hours')
        ON CONFLICT DO NOTHING
        """, user.id(), endpoint, idempotencyKey, requestHash);
    if (inserted == 0) {
      return replayOrCollision(user.id(), endpoint, idempotencyKey, requestHash);
    }

    try {
      Map<String, Object> body = executeLocked(rfqId, request, user);
      jdbc.update("""
          UPDATE idempotency_keys
          SET response_body = ?::jsonb, http_status = 200, status = 'COMPLETED'
          WHERE user_id = ? AND endpoint = ? AND idempotency_key = ?
          """, objectMapper.writeValueAsString(body), user.id(), endpoint, idempotencyKey);
      quoteBoardCacheService.cacheIdempotentResponse(user.id(), endpoint, idempotencyKey, body);
      return new ExecutionResult(200, body);
    } catch (ApiException ex) {
      jdbc.update("""
          DELETE FROM idempotency_keys
          WHERE user_id = ? AND endpoint = ? AND idempotency_key = ? AND status = 'PROCESSING'
          """, user.id(), endpoint, idempotencyKey);
      throw ex;
    } catch (Exception ex) {
      jdbc.update("""
          DELETE FROM idempotency_keys
          WHERE user_id = ? AND endpoint = ? AND idempotency_key = ? AND status = 'PROCESSING'
          """, user.id(), endpoint, idempotencyKey);
      throw new IllegalStateException("Execution failed", ex);
    }
  }

  private Map<String, Object> executeLocked(UUID rfqId, Map<String, Object> request, CurrentUser user) {
    UUID quoteId = requiredQuoteId(request.get("quote_id"));
    Map<String, Object> rfq = lockRfq(rfqId);
    if (!rfq.get("user_id").equals(user.id()) && !user.role().equals("ADMIN")) {
      throw new ApiException(HttpStatus.NOT_FOUND, "RFQ_NOT_FOUND", "RFQ not found");
    }
    String rfqStatus = String.valueOf(rfq.get("status"));
    if (rfqStatus.equals("EXPIRED")) {
      throw new ApiException(HttpStatus.CONFLICT, "RFQ_EXPIRED", "RFQ is expired");
    }
    if (rfqStatus.equals("CANCELLED")) {
      throw new ApiException(HttpStatus.CONFLICT, "RFQ_CANCELLED", "RFQ is cancelled");
    }
    if (rfqStatus.equals("EXECUTED")) {
      throw new ApiException(HttpStatus.CONFLICT, "RFQ_ALREADY_EXECUTED", "RFQ is already executed");
    }
    if (Boolean.TRUE.equals(rfq.get("rfq_expired"))) {
      jdbc.update("UPDATE rfqs SET status = 'EXPIRED' WHERE id = ?", rfqId);
      throw new ApiException(HttpStatus.CONFLICT, "RFQ_EXPIRED", "RFQ is expired");
    }

    Map<String, Object> selected = lockQuote(rfqId, quoteId);
    if (!String.valueOf(selected.get("status")).equals("ACTIVE")) {
      throw new ApiException(HttpStatus.CONFLICT, "QUOTE_INACTIVE", "Quote is not active");
    }
    if (Boolean.TRUE.equals(selected.get("quote_expired"))) {
      jdbc.update("UPDATE quotes SET status = 'EXPIRED' WHERE id = ?", quoteId);
      throw new ApiException(HttpStatus.CONFLICT, "QUOTE_EXPIRED", "Quote is expired");
    }

    List<Map<String, Object>> rankedQuotes = rankedExecutableQuotes(rfqId, String.valueOf(rfq.get("side")));
    int rank = selectedRank(rankedQuotes, quoteId);
    if (rank == -1) {
      throw new ApiException(HttpStatus.CONFLICT, "QUOTE_NOT_EXECUTABLE", "Quote is no longer executable");
    }
    Map<String, Object> bestQuote = rankedQuotes.getFirst();
    boolean selectedBest = rank == 1;
    String reasonCode = stringOrNull(request.get("reason_code"));
    String reasonText = stringOrNull(request.get("reason_text"));
    if (!selectedBest && (reasonCode == null || reasonCode.isBlank())) {
      throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "BEST_EXECUTION_RATIONALE_REQUIRED", "A best-execution rationale is required when executing a non-best quote");
    }

    UUID tradeId = jdbc.queryForObject("""
        INSERT INTO trades (rfq_id, quote_id, bond_id, dealer_id, user_id, side, quantity, execution_price, execution_yield, settlement_date)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING id
        """, UUID.class,
        rfqId,
        quoteId,
        rfq.get("bond_id"),
        selected.get("dealer_id"),
        user.id(),
        rfq.get("side"),
        rfq.get("quantity"),
        selected.get("price"),
        selected.get("yield_value"),
        rfq.get("settlement_date"));

    jdbc.update("UPDATE rfqs SET status = 'EXECUTED' WHERE id = ?", rfqId);
    jdbc.update("UPDATE quotes SET status = 'EXECUTED' WHERE id = ?", quoteId);
    jdbc.update("UPDATE quotes SET status = 'INACTIVE' WHERE rfq_id = ? AND id <> ? AND status = 'ACTIVE'", rfqId, quoteId);

    if (!selectedBest) {
      jdbc.update("""
          INSERT INTO execution_rationales (trade_id, selected_quote_id, best_quote_id, reason_code, reason_text, created_by)
          VALUES (?, ?, ?, ?, ?, ?)
          """, tradeId, quoteId, bestQuote.get("id"), reasonCode, reasonText, user.id());
      auditService.record("BEST_EXECUTION_RATIONALE_SUBMITTED", "TRADE", tradeId, user.id(), Map.of(
          "selected_quote_id", quoteId,
          "best_quote_id", bestQuote.get("id"),
          "reason_code", reasonCode,
          "reason_text", reasonText == null ? "" : reasonText));
    }

    auditService.record("QUOTE_EXECUTED", "QUOTE", quoteId, user.id(), Map.of(
        "rfq_id", rfqId, "trade_id", tradeId, "price", selected.get("price"), "rank", rank));
    auditService.record("TRADE_CREATED", "TRADE", tradeId, user.id(), Map.of(
        "rfq_id", rfqId, "quote_id", quoteId, "dealer_id", selected.get("dealer_id")));
    outboxService.enqueue("trade.executed", tradeId, Map.of("trade_id", tradeId, "rfq_id", rfqId, "quote_id", quoteId));
    Map<String, Object> analytics = analyticsService.generate(tradeId, rankedQuotes, rank);
    messagingService.status(rfqId, Map.of("event_type", "QUOTE_EXECUTED", "rfq_id", rfqId, "quote_id", quoteId, "trade_id", tradeId, "status", "EXECUTED"));

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("trade_id", tradeId);
    response.put("rfq_id", rfqId);
    response.put("status", "EXECUTED");
    response.put("execution_price", selected.get("price"));
    response.put("execution_yield", selected.get("yield_value"));
    response.put("dealer_id", selected.get("dealer_id"));
    response.put("dealer", selected.get("dealer"));
    response.put("selected_quote_rank", rank);
    response.put("analytics", analytics);
    return response;
  }

  private ExecutionResult replayOrCollision(UUID userId, String endpoint, String key, String requestHash) {
    Map<String, Object> row = jdbc.queryForList("""
        SELECT request_hash, response_body::text AS response_body, http_status, status
        FROM idempotency_keys
        WHERE user_id = ? AND endpoint = ? AND idempotency_key = ?
        """, userId, endpoint, key).getFirst();
    if (!requestHash.equals(String.valueOf(row.get("request_hash")))) {
      throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_REQUEST", "Idempotency key was reused with a different request");
    }
    if (row.get("response_body") == null) {
      throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_REQUEST_IN_PROGRESS", "A matching execution request is still processing");
    }
    try {
      Map<String, Object> body = objectMapper.readValue(String.valueOf(row.get("response_body")), new TypeReference<>() {});
      return new ExecutionResult(((Number) row.get("http_status")).intValue(), body);
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to replay idempotent response", ex);
    }
  }

  private Map<String, Object> lockRfq(UUID rfqId) {
    List<Map<String, Object>> rows = jdbc.queryForList("""
        SELECT r.*, r.expires_at <= now() AS rfq_expired
        FROM rfqs r
        WHERE r.id = ?
        FOR UPDATE
        """, rfqId);
    if (rows.isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "RFQ_NOT_FOUND", "RFQ not found");
    }
    return rows.getFirst();
  }

  private Map<String, Object> lockQuote(UUID rfqId, UUID quoteId) {
    List<Map<String, Object>> rows = jdbc.queryForList("""
        SELECT q.*, q.expires_at <= now() AS quote_expired, d.code AS dealer
        FROM quotes q
        JOIN dealers d ON d.id = q.dealer_id
        WHERE q.id = ? AND q.rfq_id = ?
        FOR UPDATE
        """, quoteId, rfqId);
    if (rows.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "QUOTE_RFQ_MISMATCH", "Quote does not belong to RFQ");
    }
    return rows.getFirst();
  }

  private List<Map<String, Object>> rankedExecutableQuotes(UUID rfqId, String side) {
    List<Map<String, Object>> quotes = new ArrayList<>(jdbc.queryForList("""
        SELECT q.*, d.code AS dealer
        FROM quotes q
        JOIN dealers d ON d.id = q.dealer_id
        WHERE q.rfq_id = ? AND q.status = 'ACTIVE' AND q.expires_at > now()
        """, rfqId));
    Comparator<Map<String, Object>> comparator = Comparator
        .comparing((Map<String, Object> q) -> Numbers.bd(q.get("price")))
        .thenComparing(q -> String.valueOf(q.get("received_at")))
        .thenComparing(q -> String.valueOf(q.get("dealer")));
    if (side.equals("SELL")) {
      comparator = comparator.reversed();
    }
    quotes.sort(comparator);
    return quotes;
  }

  private int selectedRank(List<Map<String, Object>> rankedQuotes, UUID quoteId) {
    for (int i = 0; i < rankedQuotes.size(); i++) {
      if (rankedQuotes.get(i).get("id").equals(quoteId)) {
        return i + 1;
      }
    }
    return -1;
  }

  private String requestHash(Map<String, Object> request) {
    try {
      Map<String, Object> stable = new LinkedHashMap<>();
      stable.put("quote_id", request.get("quote_id"));
      stable.put("reason_code", request.get("reason_code"));
      stable.put("reason_text", request.get("reason_text"));
      byte[] bytes = objectMapper.writeValueAsBytes(stable);
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to hash execution request", ex);
    }
  }

  private String stringOrNull(Object value) {
    if (value == null) return null;
    String text = String.valueOf(value);
    return text.equals("null") ? null : text;
  }

  private UUID requiredQuoteId(Object value) {
    String text = stringOrNull(value);
    if (text == null || text.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "QUOTE_ID_REQUIRED", "Execution requires quote_id");
    }
    try {
      return UUID.fromString(text);
    } catch (IllegalArgumentException ex) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUOTE_ID", "quote_id must be a UUID");
    }
  }

  public record ExecutionResult(int status, Map<String, Object> body) {}
}
