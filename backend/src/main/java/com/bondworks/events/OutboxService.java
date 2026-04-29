package com.bondworks.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {
  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final boolean kafkaEnabled;

  public OutboxService(
      JdbcTemplate jdbc,
      ObjectMapper objectMapper,
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${bondworks.kafka-enabled:false}") boolean kafkaEnabled) {
    this.jdbc = jdbc;
    this.objectMapper = objectMapper;
    this.kafkaTemplate = kafkaTemplate;
    this.kafkaEnabled = kafkaEnabled;
  }

  public void enqueue(String topic, UUID key, Map<String, Object> payload) {
    try {
      jdbc.update("""
          INSERT INTO outbox_events (topic, event_key, payload)
          VALUES (?, ?, ?::jsonb)
          """, topic, key.toString(), objectMapper.writeValueAsString(payload));
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to enqueue outbox event", ex);
    }
  }

  @Scheduled(fixedDelay = 1000)
  public void publish() {
    if (!kafkaEnabled) {
      return;
    }
    List<Map<String, Object>> rows = jdbc.queryForList("""
        SELECT id, topic, event_key, payload::text AS payload
        FROM outbox_events
        WHERE status = 'NEW'
        ORDER BY created_at
        LIMIT 50
        """);
    for (Map<String, Object> row : rows) {
      UUID id = (UUID) row.get("id");
      try {
        kafkaTemplate.send(String.valueOf(row.get("topic")), String.valueOf(row.get("event_key")), String.valueOf(row.get("payload")));
        jdbc.update("UPDATE outbox_events SET status = 'PUBLISHED', published_at = now(), attempts = attempts + 1 WHERE id = ?", id);
      } catch (Exception ex) {
        jdbc.update("UPDATE outbox_events SET attempts = attempts + 1 WHERE id = ?", id);
      }
    }
  }
}
