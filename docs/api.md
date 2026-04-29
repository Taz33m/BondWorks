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
