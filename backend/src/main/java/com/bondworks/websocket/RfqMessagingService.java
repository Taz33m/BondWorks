package com.bondworks.websocket;

import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RfqMessagingService {
  private final SimpMessagingTemplate messagingTemplate;

  public RfqMessagingService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void quote(UUID rfqId, Map<String, Object> payload) {
    messagingTemplate.convertAndSend("/topic/rfqs/" + rfqId + "/quotes", payload);
  }

  public void status(UUID rfqId, Map<String, Object> payload) {
    messagingTemplate.convertAndSend("/topic/rfqs/" + rfqId + "/status", payload);
  }
}
