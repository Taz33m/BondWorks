# BondWorks Lite Data Strategy

BondWorks combines public market benchmarks with simulated proprietary dealer behavior to recreate the decision environment of an institutional fixed-income RFQ desk.

## Layer 1: Real Public Market Context

Use public market context where it is safe and available:

- Treasury yield curve points: `yield_curve_points`
- SOFR/EFFR/OBFR reference rates: `reference_rates`
- Optional FRED macro/rates series in `data/raw/fred`

These inputs support the market status panel, benchmark yields, mid-yield assumptions, and README/technical-paper credibility.

## Layer 2: Real-ish Transaction Benchmarks

Use TRACE-like prints for execution analytics:

- `market_prints`
- execution vs last print
- execution vs tape VWAP
- execution vs recent high/low

V1 uses simulated prints labeled `SIM_TRACE`. FINRA ingestion is deferred until credentials and redistribution terms are clear.

Tape VWAP:

```text
tape_vwap = sum(price_i * quantity_i) / sum(quantity_i)
```

The backend uses the last 30 minutes of prints for the same bond, then falls back to the last 10 prints.

## Layer 3: Simulated Proprietary Microstructure

Simulate what real RFQ platforms and dealers do not expose publicly:

- dealer quote responses
- dealer axes
- inventory fit
- response latency
- dealer hit rate
- smart dealer-selection history
- best-execution rationale history

The official numbers remain deterministic and backend-owned.

## LLM Use

LLMs may generate qualitative fixtures only:

- dealer-axis explanations
- market color
- execution memo text
- compliance summaries

LLMs must not determine quote price, best quote, trade state, slippage, cover price, audit truth, or idempotency behavior.

Generated fixtures are validated and stored with metadata in `agent_generated_fixtures`.

## Identifier Policy

Use safe identifiers in the public repo:

- `bond_code`
- FIGI where allowed
- display name
- issuer
- fake CUSIP-like display IDs

Do not publish real CUSIPs without confirming licensing rights.
