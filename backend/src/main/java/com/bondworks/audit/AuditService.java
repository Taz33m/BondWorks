package com.bondworks.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;

  public AuditService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
    this.jdbc = jdbc;
    this.objectMapper = objectMapper;
  }

  public void record(String eventType, String entityType, UUID entityId, UUID userId, Map<String, Object> payload) {
    try {
      jdbc.update("""
          INSERT INTO audit_events (event_type, entity_type, entity_id, user_id, payload)
          VALUES (?, ?, ?, ?, ?::jsonb)
          """, eventType, entityType, entityId, userId, objectMapper.writeValueAsString(payload));
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to record audit event", ex);
    }
  }

  public List<Map<String, Object>> list(String search, String eventType) {
    String text = search == null ? "" : search.toLowerCase();
    String type = eventType == null || eventType.equals("ALL_EVENTS") ? "" : eventType;
    return jdbc.queryForList("""
        SELECT id, event_type, entity_type, entity_id, user_id, payload, created_at
        FROM audit_events
        WHERE (? = '' OR lower(event_type) LIKE '%' || ? || '%' OR lower(entity_id::text) LIKE '%' || ? || '%' OR lower(payload::text) LIKE '%' || ? || '%')
          AND (? = '' OR event_type = ?)
        ORDER BY created_at DESC
        LIMIT 300
        """, text, text, text, text, type, type);
  }
}
