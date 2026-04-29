#!/usr/bin/env python3
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "data" / "curated" / "dealer_axes.csv"

ROWS = [
    ["CITI", "UST-10Y-2036", "WANTS_TO_SELL", 0.96, -1.2, "CITI is axed to reduce 10Y duration inventory and is likely to show a tighter offer.", "2026-04-28T09:30:00Z", "", "SIMULATED_PROPRIETARY"],
    ["JPM", "UST-10Y-2036", "NEUTRAL", 0.74, 0.1, "JPM has neutral UST-10Y inventory and should quote close to profile.", "2026-04-28T09:30:00Z", "", "SIMULATED_PROPRIETARY"],
    ["GS", "UST-10Y-2036", "WANTS_TO_BUY", 0.52, 1.0, "GS wants to buy UST-10Y and is less competitive for a client BUY RFQ.", "2026-04-28T09:30:00Z", "", "SIMULATED_PROPRIETARY"],
]


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["dealer", "bond_code", "stance", "inventory_fit_score", "inventory_adjustment_bps", "description", "valid_from", "valid_to", "source"])
        writer.writerows(ROWS)
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
