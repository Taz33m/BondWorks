# Event Replay

Event replay turns an RFQ into a replayable lifecycle timeline. It is designed to show that BondWorks preserves enough persisted state to reconstruct how a trade moved from request creation through quote competition, execution, analytics, and audit evidence.

## Product Goals

- Replay the full RFQ lifecycle from persisted data.
- Show the difference between RFQ events, dealer quote events, execution events, analytics events, and compliance/audit records.
- Help reviewers inspect the event-driven architecture without reading database tables directly.

## Replay Phases

The v1 replay model groups audit events into five phases:

1. `RFQ`: `RFQ_CREATED`, `RFQ_SENT_TO_DEALERS`, `RFQ_CANCELLED`, `RFQ_EXPIRED`.
2. `QUOTES_RECEIVED`: `QUOTE_RECEIVED`, `QUOTE_EXPIRED`.
3. `EXECUTION`: `QUOTE_EXECUTED`, `TRADE_CREATED`, `BEST_EXECUTION_RATIONALE_SUBMITTED`, `EXECUTION_FAILED`.
4. `ANALYTICS`: `ANALYTICS_GENERATED`.
5. `AUDIT`: fallback phase for any lifecycle event that does not fit the above buckets.

## Backend Endpoint

`GET /api/event-replay/rfqs/{rfqId}`

The endpoint scopes audit events by RFQ ID, quote IDs that belong to the RFQ, and trade IDs created from the RFQ.

```json
{
  "rfq": {
    "id": "cccccccc-0000-0000-0000-000000000001",
    "status": "EXECUTED",
    "bond_code": "UST-10Y-2036",
    "quote_count": 4,
    "trade_count": 1
  },
  "phase_order": ["RFQ", "QUOTES_RECEIVED", "EXECUTION", "ANALYTICS", "AUDIT"],
  "events": [
    {
      "sequence": 1,
      "occurred_at": "2026-04-29T01:00:00Z",
      "phase": "RFQ",
      "event_type": "RFQ_CREATED",
      "entity_type": "RFQ",
      "entity_id": "cccccccc-0000-0000-0000-000000000001",
      "actor": "Demo Trader",
      "summary": "RFQ opened and stored",
      "payload": {}
    }
  ]
}
```

## UI Workflow

Navigate to `/replay`.

The replay screen includes:

- RFQ selector sourced from persisted RFQs.
- Summary strip for selected RFQ, status, security, and event count.
- Lifecycle coverage cards for RFQ, quote, execution, analytics, and audit phases.
- Ordered timeline with event type, actor, entity ID, timestamp, summary, and payload.

## Why It Matters

Financial infrastructure products need traceability. Event replay makes the RFQ workflow inspectable after the fact and demonstrates that quote competition, execution decisions, analytics, and compliance records are connected by durable identifiers.
