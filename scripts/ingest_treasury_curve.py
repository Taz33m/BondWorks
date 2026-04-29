#!/usr/bin/env python3
from __future__ import annotations

import csv
import json
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "data" / "raw" / "treasury"
CURATED = ROOT / "data" / "curated" / "yield_curve_points.csv"

TENOR_FIELDS = {
    "1M": ["bc_1month", "1_mo", "1mo"],
    "2M": ["bc_2month", "2_mo", "2mo"],
    "3M": ["bc_3month", "3_mo", "3mo"],
    "4M": ["bc_4month", "4_mo", "4mo"],
    "6M": ["bc_6month", "6_mo", "6mo"],
    "1Y": ["bc_1year", "1_yr", "1yr"],
    "2Y": ["bc_2year", "2_yr", "2yr"],
    "3Y": ["bc_3year", "3_yr", "3yr"],
    "5Y": ["bc_5year", "5_yr", "5yr"],
    "7Y": ["bc_7year", "7_yr", "7yr"],
    "10Y": ["bc_10year", "10_yr", "10yr"],
    "20Y": ["bc_20year", "20_yr", "20yr"],
    "30Y": ["bc_30year", "30_yr", "30yr"],
}


def main():
    RAW.mkdir(parents=True, exist_ok=True)
    url = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v2/accounting/od/daily_treasury_rates?sort=-record_date&page[size]=1"
    with urllib.request.urlopen(url, timeout=30) as response:
        payload = json.load(response)
    (RAW / "daily_treasury_rates_latest.json").write_text(json.dumps(payload, indent=2))
    record = payload["data"][0]
    curve_date = record.get("record_date")
    points = []
    for tenor, candidates in TENOR_FIELDS.items():
        raw_value = next((record.get(key) for key in candidates if record.get(key) not in (None, "")), None)
        if raw_value is not None:
            points.append({"curve_date": curve_date, "tenor": tenor, "rate": raw_value, "source": "UST_TREASURY"})
    if not points:
        raise SystemExit("Treasury response did not contain recognizable tenor fields; inspect raw JSON")
    with CURATED.open("w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=["curve_date", "tenor", "rate", "source"])
        writer.writeheader()
        writer.writerows(points)
    print(f"wrote {CURATED}")


if __name__ == "__main__":
    main()
