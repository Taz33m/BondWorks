# API

## Auth

`POST /api/auth/demo-login`

```json
{ "email": "trader@demo.com" }
```

## RFQ

`POST /api/rfqs`

```json
{
  "bond_id": "aaaaaaaa-0000-0000-0000-000000000003",
  "side": "BUY",
  "quantity": 5000000,
  "dealer_ids": ["bbbbbbbb-0000-0000-0000-000000000001"],
  "time_in_force_seconds": 30,
  "settlement_date": "2026-04-30"
}
```

## Execute

`POST /api/rfqs/{id}/execute`

Headers:

```text
Idempotency-Key: any-client-generated-key
```

Body:

```json
{ "quote_id": "quote-uuid" }
```

Non-best quote execution requires:

```json
{
  "quote_id": "quote-uuid",
  "reason_code": "SETTLEMENT_RELIABILITY",
  "reason_text": "JPM selected over best price because of settlement reliability."
}
```

## Market Context

`GET /api/market/context`

Returns:

```json
{
  "market_status": "OPEN",
  "rates_regime": "Stable",
  "yield_curve": [{ "tenor": "10Y", "rate": 4.18 }],
  "reference_rates": [{ "rate_type": "SOFR", "value": 3.66 }]
}
```

## Event Replay

`GET /api/event-replay/rfqs/{rfqId}`

Returns an ordered RFQ lifecycle replay from persisted audit events:

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
      "actor": "Demo Trader",
      "summary": "RFQ opened and stored",
      "payload": {}
    }
  ]
}
```

See [event-replay.md](event-replay.md) for the replay workflow and phase model.
