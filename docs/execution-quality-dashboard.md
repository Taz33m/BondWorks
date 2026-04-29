# Historical Execution Quality Dashboard

The historical execution quality dashboard turns completed RFQs into desk-level execution intelligence. It is the post-MVP view for answering whether the simulator is producing believable best-execution evidence over time, not just on a single trade.

## Product Goals

- Show historical slippage and cover-distance trends.
- Compare dealer win rates and latency behavior.
- Surface quote dispersion by instrument type.
- Track best quote versus executed quote history.
- Rank counterparties by missed savings.

## Backend Endpoint

`GET /api/execution-quality`

The endpoint reads from persisted trade lifecycle tables:

- `trades`
- `quotes`
- `execution_analytics`
- `rfq_dealers`
- `bonds`
- `dealers`

No new warehouse table is required for v1. The dashboard is intentionally query-backed so seeded trades and newly executed RFQs appear immediately.

## Response Shape

```json
{
  "summary": {
    "total_trades": 12,
    "avg_slippage_bps": 4.8,
    "avg_cover_distance_bps": 0.9,
    "avg_quote_dispersion_bps": 3.5,
    "total_missed_savings_usd": 1850.25
  },
  "slippage_over_time": [],
  "dealer_win_rate": [],
  "quote_dispersion_by_instrument_type": [],
  "dealer_latency_distribution": [],
  "best_vs_executed_quote_history": [],
  "missed_savings_leaderboard": []
}
```

## UI Workflow

Navigate to `/quality` from the sidebar.

The screen includes:

- Summary strip for total trades, average slippage, average cover distance, and missed savings.
- Slippage over time line chart.
- Dealer win-rate chart.
- Quote dispersion by instrument type.
- Dealer latency distribution.
- Missed savings leaderboard.
- Best versus executed quote history table with links back to individual trade analytics.

## Interpretation

- Slippage measures execution versus simulated mid-market.
- Cover distance measures how close the executed price was to the next-best quote.
- Quote dispersion captures how wide the dealer competition was.
- Missed savings is zero when the selected quote was best and positive when the trader paid up versus the best available quote.

All metrics remain simulated and deterministic. The dashboard is designed to prove the execution-intelligence workflow and data model, not to represent live market quality.
