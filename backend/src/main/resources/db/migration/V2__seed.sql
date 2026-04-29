INSERT INTO users (id, email, name, role) VALUES
('11111111-1111-1111-1111-111111111111', 'trader@demo.com', 'Demo Trader', 'BUY_SIDE_TRADER'),
('22222222-2222-2222-2222-222222222222', 'dealer@demo.com', 'Demo Dealer', 'DEALER'),
('33333333-3333-3333-3333-333333333333', 'admin@demo.com', 'Demo Admin', 'ADMIN');

INSERT INTO demo_api_keys (user_id, name, api_key) VALUES
('11111111-1111-1111-1111-111111111111', 'demo', 'demo-bondworks-local-key');

INSERT INTO bonds (id, code, ticker, issuer, cusip_like_id, coupon, maturity_date, sector, rating, mid_price, mid_yield) VALUES
('aaaaaaaa-0000-0000-0000-000000000001', 'UST-2Y-2028', 'UST 4.00 2028', 'US Treasury', 'BWUST2Y28', 4.0000, '2028-04-30', 'Treasury', 'AAA', 99.842000, 4.125000),
('aaaaaaaa-0000-0000-0000-000000000002', 'UST-5Y-2031', 'UST 4.10 2031', 'US Treasury', 'BWUST5Y31', 4.1000, '2031-04-30', 'Treasury', 'AAA', 100.312000, 4.098000),
('aaaaaaaa-0000-0000-0000-000000000003', 'UST-10Y-2036', 'UST 4.25 2036', 'US Treasury', 'BWUST10Y36', 4.2500, '2036-05-15', 'Treasury', 'AAA', 101.125000, 4.180000),
('aaaaaaaa-0000-0000-0000-000000000004', 'UST-20Y-2046', 'UST 4.50 2046', 'US Treasury', 'BWUST20Y46', 4.5000, '2046-05-15', 'Treasury', 'AAA', 101.625000, 4.392000),
('aaaaaaaa-0000-0000-0000-000000000005', 'UST-30Y-2056', 'UST 4.75 2056', 'US Treasury', 'BWUST30Y56', 4.7500, '2056-05-15', 'Treasury', 'AAA', 102.250000, 4.510000),
('aaaaaaaa-0000-0000-0000-000000000006', 'AAPL-2032', 'AAPL 4.10 2032', 'Apple Inc.', 'AAPL2032X1', 4.1000, '2032-08-08', 'Corporate IG', 'AA+', 99.742000, 4.257000),
('aaaaaaaa-0000-0000-0000-000000000007', 'MSFT-2031', 'MSFT 3.90 2031', 'Microsoft Corp.', 'MSFT2031X1', 3.9000, '2031-11-15', 'Corporate IG', 'AAA', 99.884000, 4.032000),
('aaaaaaaa-0000-0000-0000-000000000008', 'AMZN-2034', 'AMZN 4.55 2034', 'Amazon.com Inc.', 'AMZN2034X1', 4.5500, '2034-12-01', 'Corporate IG', 'AA-', 100.420000, 4.530000),
('aaaaaaaa-0000-0000-0000-000000000009', 'JPM-2033', 'JPM 5.20 2033', 'JPMorgan Chase', 'JPM2033X1', 5.2000, '2033-06-01', 'Financial IG', 'A-', 101.040000, 5.032000),
('aaaaaaaa-0000-0000-0000-000000000010', 'GS-2030', 'GS 5.05 2030', 'Goldman Sachs', 'GS2030X1', 5.0500, '2030-09-10', 'Financial IG', 'A', 100.812000, 4.970000),
('aaaaaaaa-0000-0000-0000-000000000011', 'XOM-2035', 'XOM 4.65 2035', 'Exxon Mobil', 'XOM2035X1', 4.6500, '2035-03-15', 'Corporate IG', 'AA-', 100.105000, 4.672000),
('aaaaaaaa-0000-0000-0000-000000000012', 'JNJ-2033', 'JNJ 4.00 2033', 'Johnson & Johnson', 'JNJ2033X1', 4.0000, '2033-02-01', 'Corporate IG', 'AAA', 99.612000, 4.091000),
('aaaaaaaa-0000-0000-0000-000000000013', 'NVDA-2034', 'NVDA 4.75 2034', 'NVIDIA Corp.', 'NVDA2034X1', 4.7500, '2034-10-15', 'Corporate IG', 'A+', 101.211000, 4.620000),
('aaaaaaaa-0000-0000-0000-000000000014', 'WMT-2032', 'WMT 3.95 2032', 'Walmart Inc.', 'WMT2032X1', 3.9500, '2032-07-01', 'Corporate IG', 'AA', 99.503000, 4.012000),
('aaaaaaaa-0000-0000-0000-000000000015', 'PFE-2031', 'PFE 4.30 2031', 'Pfizer Inc.', 'PFE2031X1', 4.3000, '2031-05-01', 'Corporate IG', 'A', 99.950000, 4.335000),
('aaaaaaaa-0000-0000-0000-000000000016', 'F-2029', 'F 6.20 2029', 'Ford Motor Credit', 'F2029X1', 6.2000, '2029-08-15', 'High Yield', 'BB+', 98.650000, 6.875000),
('aaaaaaaa-0000-0000-0000-000000000017', 'CCL-2030', 'CCL 7.10 2030', 'Carnival Corp.', 'CCL2030X1', 7.1000, '2030-06-01', 'High Yield', 'BB-', 97.420000, 7.720000),
('aaaaaaaa-0000-0000-0000-000000000018', 'DISH-2028', 'DISH 8.00 2028', 'DISH Network', 'DISH2028X1', 8.0000, '2028-11-15', 'High Yield', 'B', 92.115000, 10.950000),
('aaaaaaaa-0000-0000-0000-000000000019', 'RCL-2031', 'RCL 6.75 2031', 'Royal Caribbean', 'RCL2031X1', 6.7500, '2031-02-15', 'High Yield', 'BB', 99.120000, 6.982000),
('aaaaaaaa-0000-0000-0000-000000000020', 'UAL-2029', 'UAL 7.25 2029', 'United Airlines', 'UAL2029X1', 7.2500, '2029-09-01', 'High Yield', 'BB-', 98.120000, 7.860000),
('aaaaaaaa-0000-0000-0000-000000000021', 'NYC-2036', 'NYC GO 4.00 2036', 'New York City GO', 'NYC2036X1', 4.0000, '2036-08-01', 'Municipal', 'AA', 100.220000, 3.842000),
('aaaaaaaa-0000-0000-0000-000000000022', 'CA-2034', 'CA GO 3.75 2034', 'State of California', 'CA2034X1', 3.7500, '2034-04-01', 'Municipal', 'AA-', 99.712000, 3.801000),
('aaaaaaaa-0000-0000-0000-000000000023', 'TX-2033', 'TX TRAN 3.60 2033', 'Texas Transportation', 'TX2033X1', 3.6000, '2033-10-01', 'Municipal', 'AAA', 100.033000, 3.612000),
('aaaaaaaa-0000-0000-0000-000000000024', 'CHGO-2032', 'CHGO 4.30 2032', 'City of Chicago', 'CHGO2032X1', 4.3000, '2032-12-01', 'Municipal', 'A', 98.884000, 4.512000),
('aaaaaaaa-0000-0000-0000-000000000025', 'MTA-2035', 'MTA 4.15 2035', 'MTA Revenue', 'MTA2035X1', 4.1500, '2035-07-01', 'Municipal', 'A+', 99.204000, 4.301000);

INSERT INTO dealers (id, code, name, personality, avg_latency_ms, base_spread_bps, response_probability, aggressiveness, size_capacity) VALUES
('bbbbbbbb-0000-0000-0000-000000000001', 'CITI', 'Citigroup Global Markets', 'aggressive', 420, 5.700000, 0.96, 0.92, 0.86),
('bbbbbbbb-0000-0000-0000-000000000002', 'JPM', 'J.P. Morgan Securities', 'balanced', 280, 7.800000, 0.98, 0.78, 0.91),
('bbbbbbbb-0000-0000-0000-000000000003', 'GS', 'Goldman Sachs', 'balanced', 830, 6.600000, 0.93, 0.80, 0.82),
('bbbbbbbb-0000-0000-0000-000000000004', 'MS', 'Morgan Stanley', 'conservative', 1200, 9.200000, 0.88, 0.60, 0.77),
('bbbbbbbb-0000-0000-0000-000000000005', 'BOFA', 'BofA Securities', 'inventory-driven', 1700, 8.600000, 0.82, 0.68, 0.80),
('bbbbbbbb-0000-0000-0000-000000000006', 'BARC', 'Barclays Capital', 'outlier', 4800, 11.600000, 0.75, 0.48, 0.66),
('bbbbbbbb-0000-0000-0000-000000000007', 'DB', 'Deutsche Bank', 'conservative', 2600, 10.800000, 0.55, 0.42, 0.62);

INSERT INTO dealer_axes (dealer_id, bond_id, stance, inventory_fit_score, inventory_adjustment_bps, description) VALUES
('bbbbbbbb-0000-0000-0000-000000000001', 'aaaaaaaa-0000-0000-0000-000000000003', 'WANTS_TO_SELL', 0.96, -1.200000, 'Axe: wants to sell UST-10Y; better offer for BUY RFQ'),
('bbbbbbbb-0000-0000-0000-000000000002', 'aaaaaaaa-0000-0000-0000-000000000003', 'NEUTRAL', 0.74, 0.100000, 'Axe: neutral UST-10Y inventory; normal quote'),
('bbbbbbbb-0000-0000-0000-000000000003', 'aaaaaaaa-0000-0000-0000-000000000003', 'WANTS_TO_BUY', 0.52, 1.000000, 'Axe: wants to buy UST-10Y; worse offer for BUY RFQ'),
('bbbbbbbb-0000-0000-0000-000000000004', 'aaaaaaaa-0000-0000-0000-000000000003', 'LOW_BALANCE_SHEET', 0.44, 1.500000, 'Axe: low balance sheet for UST duration; wider quote'),
('bbbbbbbb-0000-0000-0000-000000000005', 'aaaaaaaa-0000-0000-0000-000000000003', 'WANTS_TO_SELL', 0.80, -0.300000, 'Axe: modest UST-10Y sell inventory'),
('bbbbbbbb-0000-0000-0000-000000000006', 'aaaaaaaa-0000-0000-0000-000000000003', 'LOW_BALANCE_SHEET', 0.36, 1.800000, 'Axe: outlier balance-sheet cost'),
('bbbbbbbb-0000-0000-0000-000000000007', 'aaaaaaaa-0000-0000-0000-000000000003', 'NEUTRAL', 0.40, 1.200000, 'Axe: low response confidence');

INSERT INTO dealer_statistics_snapshot (dealer_id, sector, historical_price_quality, response_rate, latency_score, sector_win_rate, size_capacity) VALUES
('bbbbbbbb-0000-0000-0000-000000000001', 'Treasury', 0.96, 0.94, 0.86, 0.92, 0.84),
('bbbbbbbb-0000-0000-0000-000000000002', 'Treasury', 0.86, 0.98, 0.97, 0.82, 0.88),
('bbbbbbbb-0000-0000-0000-000000000003', 'Treasury', 0.83, 0.91, 0.72, 0.81, 0.80),
('bbbbbbbb-0000-0000-0000-000000000004', 'Treasury', 0.75, 0.88, 0.64, 0.76, 0.74),
('bbbbbbbb-0000-0000-0000-000000000005', 'Treasury', 0.73, 0.84, 0.58, 0.72, 0.78),
('bbbbbbbb-0000-0000-0000-000000000006', 'Treasury', 0.66, 0.77, 0.42, 0.61, 0.67),
('bbbbbbbb-0000-0000-0000-000000000007', 'Treasury', 0.58, 0.61, 0.54, 0.55, 0.63),
('bbbbbbbb-0000-0000-0000-000000000001', 'Corporate IG', 0.88, 0.92, 0.85, 0.84, 0.78),
('bbbbbbbb-0000-0000-0000-000000000002', 'Corporate IG', 0.90, 0.96, 0.96, 0.86, 0.91),
('bbbbbbbb-0000-0000-0000-000000000003', 'Corporate IG', 0.86, 0.90, 0.70, 0.83, 0.88),
('bbbbbbbb-0000-0000-0000-000000000004', 'Corporate IG', 0.78, 0.86, 0.62, 0.74, 0.81),
('bbbbbbbb-0000-0000-0000-000000000005', 'Corporate IG', 0.82, 0.87, 0.60, 0.78, 0.83),
('bbbbbbbb-0000-0000-0000-000000000006', 'Corporate IG', 0.70, 0.76, 0.44, 0.64, 0.69),
('bbbbbbbb-0000-0000-0000-000000000007', 'Corporate IG', 0.63, 0.66, 0.50, 0.59, 0.64);

INSERT INTO market_prints (bond_id, price, quantity, side, printed_at, source) VALUES
('aaaaaaaa-0000-0000-0000-000000000003', 101.176000, 2000000, 'BUY', now() - interval '1 minute', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000003', 101.169000, 1000000, 'SELL', now() - interval '3 minutes', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000003', 101.181000, 5000000, 'BUY', now() - interval '6 minutes', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000003', 101.155000, 1500000, 'SELL', now() - interval '11 minutes', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000003', 101.190000, 3000000, 'BUY', now() - interval '18 minutes', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000003', 101.148000, 2500000, 'SELL', now() - interval '27 minutes', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000003', 101.132000, 4000000, 'BUY', now() - interval '45 minutes', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000006', 99.742000, 2000000, 'SELL', now() - interval '4 minutes', 'SIM_TRACE'),
('aaaaaaaa-0000-0000-0000-000000000007', 99.884000, 3000000, 'SELL', now() - interval '8 minutes', 'SIM_TRACE');

INSERT INTO rfqs (id, user_id, bond_id, side, quantity, status, time_in_force_seconds, settlement_date, created_at, expires_at) VALUES
('cccccccc-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-0000-0000-0000-000000000003', 'BUY', 5000000, 'EXECUTED', 30, current_date + 1, now() - interval '25 minutes', now() - interval '24 minutes 30 seconds'),
('cccccccc-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-0000-0000-0000-000000000007', 'SELL', 3000000, 'EXECUTED', 30, current_date + 1, now() - interval '40 minutes', now() - interval '39 minutes 30 seconds');

INSERT INTO quotes (id, rfq_id, dealer_id, side, price, yield_value, spread_bps, quantity, status, received_at, expires_at, latency_ms) VALUES
('dddddddd-0000-0000-0000-000000000001', 'cccccccc-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000001', 'OFFER', 101.183000, 4.162000, 5.700000, 5000000, 'EXECUTED', now() - interval '24 minutes 56 seconds', now() - interval '24 minutes 26 seconds', 412),
('dddddddd-0000-0000-0000-000000000002', 'cccccccc-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000003', 'OFFER', 101.191000, 4.158000, 6.500000, 5000000, 'INACTIVE', now() - interval '24 minutes 55 seconds', now() - interval '24 minutes 25 seconds', 830),
('dddddddd-0000-0000-0000-000000000003', 'cccccccc-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000002', 'OFFER', 101.204000, 4.151000, 7.800000, 5000000, 'INACTIVE', now() - interval '24 minutes 57 seconds', now() - interval '24 minutes 27 seconds', 286),
('dddddddd-0000-0000-0000-000000000004', 'cccccccc-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000004', 'OFFER', 101.219000, 4.144000, 9.300000, 5000000, 'INACTIVE', now() - interval '24 minutes 54 seconds', now() - interval '24 minutes 24 seconds', 1200),
('dddddddd-0000-0000-0000-000000000005', 'cccccccc-0000-0000-0000-000000000002', 'bbbbbbbb-0000-0000-0000-000000000003', 'BID', 99.884000, 4.032000, 4.100000, 3000000, 'EXECUTED', now() - interval '39 minutes 56 seconds', now() - interval '39 minutes 26 seconds', 402);

INSERT INTO trades (id, rfq_id, quote_id, bond_id, dealer_id, user_id, side, quantity, execution_price, execution_yield, settlement_date, executed_at) VALUES
('eeeeeeee-0000-0000-0000-000000000001', 'cccccccc-0000-0000-0000-000000000001', 'dddddddd-0000-0000-0000-000000000001', 'aaaaaaaa-0000-0000-0000-000000000003', 'bbbbbbbb-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'BUY', 5000000, 101.183000, 4.162000, current_date + 1, now() - interval '24 minutes 52 seconds'),
('eeeeeeee-0000-0000-0000-000000000002', 'cccccccc-0000-0000-0000-000000000002', 'dddddddd-0000-0000-0000-000000000005', 'aaaaaaaa-0000-0000-0000-000000000007', 'bbbbbbbb-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'SELL', 3000000, 99.884000, 4.032000, current_date + 1, now() - interval '39 minutes 53 seconds');

INSERT INTO execution_analytics (trade_id, best_quote_id, cover_quote_id, selected_quote_rank, competition_status, slippage_bps, spread_paid_bps, quote_dispersion_bps, response_latency_ms, time_to_execute_ms, missed_savings_usd, cover_price, cover_distance_bps, price_improvement_vs_cover_usd, recent_print_price, tape_vwap, execution_vs_tape_bps) VALUES
('eeeeeeee-0000-0000-0000-000000000001', 'dddddddd-0000-0000-0000-000000000001', 'dddddddd-0000-0000-0000-000000000002', 1, 'COMPETITIVE', 5.735000, 5.735000, 3.558000, 412, 4200, 0.00, 101.191000, 0.791100, 400.00, 101.176000, 101.173700, 0.919200),
('eeeeeeee-0000-0000-0000-000000000002', 'dddddddd-0000-0000-0000-000000000005', NULL, 1, 'INSUFFICIENT_COMPETITION', 0.000000, 0.000000, NULL, 402, 7100, 0.00, NULL, NULL, NULL, 99.884000, 99.884000, 0.000000);

INSERT INTO audit_events (event_type, entity_type, entity_id, user_id, payload, created_at) VALUES
('RFQ_CREATED', 'RFQ', 'cccccccc-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '{"bond":"UST-10Y-2036","side":"BUY","quantity":5000000}'::jsonb, now() - interval '25 minutes'),
('RFQ_SENT_TO_DEALERS', 'RFQ', 'cccccccc-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '{"dealers":["CITI","JPM","GS","MS"]}'::jsonb, now() - interval '24 minutes 59 seconds'),
('QUOTE_RECEIVED', 'QUOTE', 'dddddddd-0000-0000-0000-000000000001', NULL, '{"dealer":"CITI","price":101.183}'::jsonb, now() - interval '24 minutes 56 seconds'),
('QUOTE_EXECUTED', 'QUOTE', 'dddddddd-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '{"dealer":"CITI","price":101.183}'::jsonb, now() - interval '24 minutes 52 seconds'),
('TRADE_CREATED', 'TRADE', 'eeeeeeee-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '{"rfq":"cccccccc-0000-0000-0000-000000000001"}'::jsonb, now() - interval '24 minutes 52 seconds'),
('ANALYTICS_GENERATED', 'TRADE', 'eeeeeeee-0000-0000-0000-000000000001', NULL, '{"selected_quote_rank":1,"cover_price":101.191}'::jsonb, now() - interval '24 minutes 51 seconds');
