#!/usr/bin/env python3
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "data" / "curated" / "dealer_statistics_snapshot.csv"

ROWS = [
    ["CITI", "Bonds", "Treasury", 0.96, 0.94, 0.86, 0.92, 0.84, 5.7, 420, 0.94, "2026-04-28", "SIMULATED_PROPRIETARY"],
    ["JPM", "Bonds", "Treasury", 0.86, 0.98, 0.97, 0.82, 0.88, 7.8, 280, 0.98, "2026-04-28", "SIMULATED_PROPRIETARY"],
    ["GS", "Bonds", "Treasury", 0.83, 0.91, 0.72, 0.81, 0.80, 6.6, 830, 0.91, "2026-04-28", "SIMULATED_PROPRIETARY"],
]


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["dealer", "asset_class", "sector", "historical_price_quality", "response_rate", "latency_score", "sector_win_rate", "size_capacity", "avg_spread_bps", "avg_latency_ms", "quote_reliability", "snapshot_date", "source"])
        writer.writerows(ROWS)
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
