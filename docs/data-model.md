# Data Model Notes

PostgreSQL is the source of truth. UUIDs are internal primary keys; readable demo codes are exposed for bonds and dealers.

Core tables:

- `users`
- `bonds` / `instruments` view
- `yield_curve_points`
- `reference_rates`
- `dealers`
- `dealer_axes`
- `dealer_statistics_snapshot`
- `rfqs`
- `rfq_dealers`
- `quotes`
- `trades`
- `execution_analytics`
- `execution_rationales`
- `market_prints`
- `audit_events`
- `outbox_events`
- `idempotency_keys`
- `agent_generated_fixtures`
- `market_color_notes`
- `execution_memos`

Money-like values use `NUMERIC` in Postgres and `BigDecimal` in Java.
