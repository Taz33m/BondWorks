# BondWorks Lite Data Strategy

BondWorks uses three layers of data:

1. **Real public market context**: Treasury curve, SOFR/EFFR/OBFR, optional FRED macro series.
2. **Real-ish transaction benchmarks**: TRACE-like market prints for tape VWAP and last-print analytics. V1 uses simulated prints unless FINRA access is configured.
3. **Simulated proprietary microstructure**: dealer RFQ quotes, axes, inventory fit, latency, hit rate, relationship quality, execution rationales.

Do not commit real API keys or restricted identifiers. Use safe identifiers:

- `bond_code`
- FIGI when allowed
- display name
- issuer
- fake CUSIP-like display IDs

Real CUSIPs are intentionally excluded from the public seed data.

## Directory Layout

```text
data/
  raw/
    treasury/
    nyfed/
    fred/
    finra/
    openfigi/
  curated/
  generated/
    scenarios/
    agent_fixtures/
    demo_rfqs/
```

## Source Rules

- Treasury Fiscal Data and Treasury rate datasets: public, no key expected.
- New York Fed reference-rate data: public, no key expected.
- FRED: requires `FRED_API_KEY`.
- OpenFIGI: no key required for low-volume use.
- FINRA fixed-income/TRACE APIs: credentials optional and deferred for v1.
- OpenRouter/LLM: optional, qualitative fixture generation only. The backend owns official numbers.
