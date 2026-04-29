#!/usr/bin/env python3
from __future__ import annotations

import csv
import json
import os
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "data" / "raw" / "nyfed"
CURATED = ROOT / "data" / "curated" / "reference_rates.csv"


def main():
    RAW.mkdir(parents=True, exist_ok=True)
    url = os.getenv("NYFED_REFERENCE_RATES_URL")
    if not url:
        raise SystemExit("Set NYFED_REFERENCE_RATES_URL to the desired public NY Fed Markets Data API JSON endpoint")
    with urllib.request.urlopen(url, timeout=30) as response:
        payload = json.load(response)
    (RAW / "reference_rates_latest.json").write_text(json.dumps(payload, indent=2))
    rows = []
    for row in payload.get("refRates", payload.get("data", [])):
        rows.append({
            "rate_date": row.get("effectiveDate") or row.get("rate_date") or row.get("date"),
            "rate_type": row.get("type") or row.get("rate_type") or row.get("rateType"),
            "value": row.get("percentRate") or row.get("value") or row.get("rate"),
            "volume": row.get("volumeInBillions") or row.get("volume"),
            "source": "NYFED",
        })
    if not rows:
        raise SystemExit("NY Fed response did not contain recognizable reference-rate rows")
    with CURATED.open("w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=["rate_date", "rate_type", "value", "volume", "source"])
        writer.writeheader()
        writer.writerows(rows)
    print(f"wrote {CURATED}")


if __name__ == "__main__":
    main()
