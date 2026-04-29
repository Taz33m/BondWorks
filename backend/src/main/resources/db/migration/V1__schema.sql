CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  role TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE demo_api_keys (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  name TEXT NOT NULL,
  api_key TEXT UNIQUE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE bonds (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code TEXT UNIQUE NOT NULL,
  ticker TEXT NOT NULL,
  issuer TEXT NOT NULL,
  cusip_like_id TEXT UNIQUE NOT NULL,
  coupon NUMERIC(9,4) NOT NULL,
  maturity_date DATE NOT NULL,
  sector TEXT NOT NULL,
  rating TEXT,
  currency TEXT NOT NULL DEFAULT 'USD',
  face_value NUMERIC(18,2) NOT NULL DEFAULT 100,
  mid_price NUMERIC(18,6) NOT NULL,
  mid_yield NUMERIC(18,6),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dealers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  personality TEXT NOT NULL,
  avg_latency_ms INT NOT NULL,
  base_spread_bps NUMERIC(18,6) NOT NULL,
  response_probability NUMERIC(8,6) NOT NULL,
  aggressiveness NUMERIC(8,6) NOT NULL DEFAULT 0.5,
  size_capacity NUMERIC(8,6) NOT NULL DEFAULT 0.75,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dealer_axes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  dealer_id UUID NOT NULL REFERENCES dealers(id),
  bond_id UUID NOT NULL REFERENCES bonds(id),
  stance TEXT NOT NULL,
  inventory_fit_score NUMERIC(8,6) NOT NULL,
  inventory_adjustment_bps NUMERIC(18,6) NOT NULL,
  description TEXT NOT NULL,
  UNIQUE (dealer_id, bond_id)
);

CREATE TABLE dealer_statistics_snapshot (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  dealer_id UUID NOT NULL REFERENCES dealers(id),
  sector TEXT NOT NULL,
  historical_price_quality NUMERIC(8,6) NOT NULL,
  response_rate NUMERIC(8,6) NOT NULL,
  latency_score NUMERIC(8,6) NOT NULL,
  sector_win_rate NUMERIC(8,6) NOT NULL,
  size_capacity NUMERIC(8,6) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (dealer_id, sector)
);

CREATE TABLE rfqs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  bond_id UUID NOT NULL REFERENCES bonds(id),
  side TEXT NOT NULL CHECK (side IN ('BUY', 'SELL')),
  quantity NUMERIC(20,2) NOT NULL CHECK (quantity > 0),
  status TEXT NOT NULL,
  time_in_force_seconds INT NOT NULL CHECK (time_in_force_seconds > 0),
  settlement_date DATE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE rfq_dealers (
  rfq_id UUID NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
  dealer_id UUID NOT NULL REFERENCES dealers(id),
  status TEXT NOT NULL,
  PRIMARY KEY (rfq_id, dealer_id)
);

CREATE TABLE quotes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  rfq_id UUID NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
  dealer_id UUID NOT NULL REFERENCES dealers(id),
  side TEXT NOT NULL,
  price NUMERIC(18,6) NOT NULL,
  yield_value NUMERIC(18,6),
  spread_bps NUMERIC(18,6),
  quantity NUMERIC(20,2) NOT NULL,
  status TEXT NOT NULL,
  received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL,
  latency_ms INT NOT NULL
);

CREATE INDEX idx_quotes_rfq ON quotes(rfq_id);
CREATE INDEX idx_quotes_status_expiry ON quotes(status, expires_at);

CREATE TABLE trades (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  rfq_id UUID NOT NULL REFERENCES rfqs(id),
  quote_id UUID NOT NULL REFERENCES quotes(id),
  bond_id UUID NOT NULL REFERENCES bonds(id),
  dealer_id UUID NOT NULL REFERENCES dealers(id),
  user_id UUID NOT NULL REFERENCES users(id),
  side TEXT NOT NULL,
  quantity NUMERIC(20,2) NOT NULL,
  execution_price NUMERIC(18,6) NOT NULL,
  execution_yield NUMERIC(18,6),
  settlement_date DATE NOT NULL,
  executed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (rfq_id)
);

CREATE TABLE market_prints (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bond_id UUID NOT NULL REFERENCES bonds(id),
  price NUMERIC(18,6) NOT NULL,
  quantity NUMERIC(20,2) NOT NULL,
  side TEXT NOT NULL,
  printed_at TIMESTAMPTZ NOT NULL,
  source TEXT NOT NULL DEFAULT 'SIM_TRACE'
);

CREATE INDEX idx_market_prints_bond_time ON market_prints(bond_id, printed_at DESC);

CREATE TABLE execution_analytics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  trade_id UUID NOT NULL UNIQUE REFERENCES trades(id),
  best_quote_id UUID REFERENCES quotes(id),
  cover_quote_id UUID REFERENCES quotes(id),
  selected_quote_rank INT,
  competition_status TEXT NOT NULL,
  slippage_bps NUMERIC(18,6),
  spread_paid_bps NUMERIC(18,6),
  quote_dispersion_bps NUMERIC(18,6),
  response_latency_ms INT,
  time_to_execute_ms INT,
  missed_savings_usd NUMERIC(18,2),
  cover_price NUMERIC(18,6),
  cover_distance_bps NUMERIC(18,6),
  price_improvement_vs_cover_usd NUMERIC(18,2),
  recent_print_price NUMERIC(18,6),
  tape_vwap NUMERIC(18,6),
  execution_vs_tape_bps NUMERIC(18,6),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE execution_rationales (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  trade_id UUID NOT NULL UNIQUE REFERENCES trades(id),
  selected_quote_id UUID NOT NULL REFERENCES quotes(id),
  best_quote_id UUID REFERENCES quotes(id),
  reason_code TEXT NOT NULL,
  reason_text TEXT,
  created_by UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id UUID NOT NULL,
  user_id UUID REFERENCES users(id),
  payload JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_events_time ON audit_events(created_at DESC);
CREATE INDEX idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX idx_audit_events_type ON audit_events(event_type);

CREATE TABLE outbox_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  topic TEXT NOT NULL,
  event_key TEXT NOT NULL,
  payload JSONB NOT NULL,
  status TEXT NOT NULL DEFAULT 'NEW',
  attempts INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status_time ON outbox_events(status, created_at);

CREATE TABLE idempotency_keys (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  endpoint TEXT NOT NULL,
  idempotency_key TEXT NOT NULL,
  request_hash TEXT NOT NULL,
  response_body JSONB,
  http_status INT,
  status TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL,
  UNIQUE (user_id, endpoint, idempotency_key)
);
