ALTER TABLE bonds
  ADD COLUMN IF NOT EXISTS figi TEXT,
  ADD COLUMN IF NOT EXISTS asset_class TEXT NOT NULL DEFAULT 'Bond',
  ADD COLUMN IF NOT EXISTS benchmark_tenor TEXT,
  ADD COLUMN IF NOT EXISTS source TEXT NOT NULL DEFAULT 'CURATED_SEED';

UPDATE bonds
SET benchmark_tenor = CASE
  WHEN code LIKE 'UST-2Y%' THEN '2Y'
  WHEN code LIKE 'UST-5Y%' THEN '5Y'
  WHEN code LIKE 'UST-10Y%' THEN '10Y'
  WHEN code LIKE 'UST-20Y%' THEN '20Y'
  WHEN code LIKE 'UST-30Y%' THEN '30Y'
  WHEN maturity_date <= current_date + interval '3 years' THEN '3Y'
  WHEN maturity_date <= current_date + interval '7 years' THEN '7Y'
  WHEN maturity_date <= current_date + interval '12 years' THEN '10Y'
  ELSE '30Y'
END
WHERE benchmark_tenor IS NULL;

CREATE OR REPLACE VIEW instruments AS
SELECT
  id,
  code AS bond_code,
  figi,
  ticker AS display_name,
  issuer,
  split_part(ticker, ' ', 1) AS ticker,
  asset_class,
  sector,
  coupon,
  maturity_date,
  rating,
  currency,
  face_value,
  benchmark_tenor,
  source,
  created_at
FROM bonds;

CREATE TABLE IF NOT EXISTS yield_curve_points (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  curve_date DATE NOT NULL,
  tenor TEXT NOT NULL,
  rate NUMERIC(18,6) NOT NULL,
  source TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (curve_date, tenor, source)
);

CREATE TABLE IF NOT EXISTS reference_rates (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  rate_date DATE NOT NULL,
  rate_type TEXT NOT NULL,
  value NUMERIC(18,6) NOT NULL,
  volume NUMERIC(20,2),
  source TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (rate_date, rate_type, source)
);

ALTER TABLE market_prints
  ADD COLUMN IF NOT EXISTS yield_value NUMERIC(18,6),
  ADD COLUMN IF NOT EXISTS venue_label TEXT,
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

UPDATE market_prints SET venue_label = COALESCE(venue_label, source);

ALTER TABLE dealer_statistics_snapshot
  ADD COLUMN IF NOT EXISTS asset_class TEXT NOT NULL DEFAULT 'Bonds',
  ADD COLUMN IF NOT EXISTS avg_spread_bps NUMERIC(18,6),
  ADD COLUMN IF NOT EXISTS avg_latency_ms NUMERIC(18,6),
  ADD COLUMN IF NOT EXISTS quote_reliability NUMERIC(8,6),
  ADD COLUMN IF NOT EXISTS snapshot_date DATE NOT NULL DEFAULT current_date,
  ADD COLUMN IF NOT EXISTS source TEXT NOT NULL DEFAULT 'SIMULATED_PROPRIETARY',
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

UPDATE dealer_statistics_snapshot s
SET avg_spread_bps = COALESCE(s.avg_spread_bps, d.base_spread_bps),
    avg_latency_ms = COALESCE(s.avg_latency_ms, d.avg_latency_ms),
    quote_reliability = COALESCE(s.quote_reliability, s.response_rate)
FROM dealers d
WHERE d.id = s.dealer_id;

ALTER TABLE dealer_axes
  ADD COLUMN IF NOT EXISTS valid_from TIMESTAMPTZ NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS valid_to TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS source TEXT NOT NULL DEFAULT 'SIMULATED_PROPRIETARY',
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE quotes
  ADD COLUMN IF NOT EXISTS response_latency_ms INT,
  ADD COLUMN IF NOT EXISTS quote_reason TEXT;

UPDATE quotes
SET response_latency_ms = COALESCE(response_latency_ms, latency_ms),
    quote_reason = COALESCE(quote_reason, 'Seeded historical quote for demo RFQ lifecycle.')
WHERE response_latency_ms IS NULL OR quote_reason IS NULL;

CREATE TABLE IF NOT EXISTS agent_generated_fixtures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  fixture_type TEXT NOT NULL,
  model TEXT,
  prompt_hash TEXT,
  seed TEXT,
  payload JSONB NOT NULL,
  validation_status TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS market_color_notes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  bond_id UUID REFERENCES bonds(id),
  note_date DATE NOT NULL,
  note_text TEXT NOT NULL,
  source TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (bond_id, note_date, source)
);

CREATE TABLE IF NOT EXISTS execution_memos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  trade_id UUID REFERENCES trades(id),
  memo_text TEXT NOT NULL,
  source TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO yield_curve_points (curve_date, tenor, rate, source) VALUES
('2026-04-28', '1M', 3.92, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '2M', 3.90, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '3M', 3.88, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '4M', 3.86, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '6M', 3.84, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '1Y', 3.78, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '2Y', 4.45, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '3Y', 4.36, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '5Y', 4.24, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '7Y', 4.20, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '10Y', 4.18, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '20Y', 4.39, 'CURATED_TREASURY_CONTEXT'),
('2026-04-28', '30Y', 4.51, 'CURATED_TREASURY_CONTEXT')
ON CONFLICT DO NOTHING;

INSERT INTO reference_rates (rate_date, rate_type, value, volume, source) VALUES
('2026-04-27', 'SOFR', 3.66, 3058000000000, 'CURATED_NYFED_CONTEXT'),
('2026-04-27', 'EFFR', 3.64, NULL, 'CURATED_NYFED_CONTEXT'),
('2026-04-27', 'OBFR', 3.65, NULL, 'CURATED_NYFED_CONTEXT')
ON CONFLICT DO NOTHING;

INSERT INTO market_color_notes (bond_id, note_date, note_text, source) VALUES
('aaaaaaaa-0000-0000-0000-000000000003', '2026-04-28', 'Treasury liquidity is stable with dealers showing visible dispersion around the 10Y point; CITI is axed to reduce duration inventory.', 'CURATED_SIMULATED_COLOR')
ON CONFLICT DO NOTHING;
