package com.bondworks.events;

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
@RequestMapping("/api/event-replay")
public class EventReplayController {
  private final JdbcTemplate jdbc;

  public EventReplayController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping("/rfqs/{rfqId}")
  Map<String, Object> replay(@PathVariable UUID rfqId) {
    List<Map<String, Object>> rfqs = jdbc.queryForList("""
        SELECT r.id, r.status, r.side, r.quantity, r.created_at, r.expires_at,
               b.code AS bond_code, b.ticker, b.issuer,
               COUNT(DISTINCT q.id) AS quote_count,
               COUNT(DISTINCT t.id) AS trade_count
        FROM rfqs r
        JOIN bonds b ON b.id = r.bond_id
        LEFT JOIN quotes q ON q.rfq_id = r.id
        LEFT JOIN trades t ON t.rfq_id = r.id
        WHERE r.id = ?
        GROUP BY r.id, b.code, b.ticker, b.issuer
        """, rfqId);
    if (rfqs.isEmpty()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "RFQ_NOT_FOUND", "RFQ not found");
    }

    List<Map<String, Object>> events = jdbc.queryForList("""
        WITH scoped_events AS (
          SELECT ae.*
          FROM audit_events ae
          WHERE (ae.entity_type = 'RFQ' AND ae.entity_id = ?)
             OR (ae.entity_type = 'QUOTE' AND ae.entity_id IN (SELECT id FROM quotes WHERE rfq_id = ?))
             OR (ae.entity_type = 'TRADE' AND ae.entity_id IN (SELECT id FROM trades WHERE rfq_id = ?))
        )
        SELECT
          ROW_NUMBER() OVER (ORDER BY se.created_at ASC, se.id ASC) AS sequence,
          se.created_at AS occurred_at,
          CASE
            WHEN se.event_type IN ('RFQ_CREATED', 'RFQ_SENT_TO_DEALERS', 'RFQ_CANCELLED', 'RFQ_EXPIRED') THEN 'RFQ'
            WHEN se.event_type IN ('QUOTE_RECEIVED', 'QUOTE_EXPIRED') THEN 'QUOTES_RECEIVED'
            WHEN se.event_type IN ('QUOTE_EXECUTED', 'TRADE_CREATED', 'BEST_EXECUTION_RATIONALE_SUBMITTED', 'EXECUTION_FAILED') THEN 'EXECUTION'
            WHEN se.event_type = 'ANALYTICS_GENERATED' THEN 'ANALYTICS'
            ELSE 'AUDIT'
          END AS phase,
          se.event_type,
          se.entity_type,
          se.entity_id,
          COALESCE(u.name, 'SYSTEM') AS actor,
          se.payload,
          CASE
            WHEN se.event_type = 'RFQ_CREATED' THEN 'RFQ opened and stored'
            WHEN se.event_type = 'RFQ_SENT_TO_DEALERS' THEN 'Dealer request fan-out recorded'
            WHEN se.event_type = 'QUOTE_RECEIVED' THEN 'Dealer quote received'
            WHEN se.event_type = 'QUOTE_EXPIRED' THEN 'Quote expired'
            WHEN se.event_type = 'BEST_EXECUTION_RATIONALE_SUBMITTED' THEN 'Trader submitted best-execution rationale'
            WHEN se.event_type = 'QUOTE_EXECUTED' THEN 'Quote selected for execution'
            WHEN se.event_type = 'TRADE_CREATED' THEN 'Immutable trade record created'
            WHEN se.event_type = 'ANALYTICS_GENERATED' THEN 'Transaction-cost analytics generated'
            ELSE 'Audit event recorded'
          END AS summary
        FROM scoped_events se
        LEFT JOIN users u ON u.id = se.user_id
        ORDER BY se.created_at ASC, se.id ASC
        """, rfqId, rfqId, rfqId);

    return Map.of(
        "rfq", rfqs.getFirst(),
        "events", events,
        "phase_order", List.of("RFQ", "QUOTES_RECEIVED", "EXECUTION", "ANALYTICS", "AUDIT"));
  }
}
