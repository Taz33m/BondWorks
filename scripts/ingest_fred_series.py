#!/usr/bin/env python3
from __future__ import annotations

import json
import os
import sys
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "data" / "raw" / "fred"


def fetch(series_id: str):
    api_key = os.getenv("FRED_API_KEY")
    if not api_key:
        raise SystemExit("FRED_API_KEY is required for FRED series ingestion")
    params = urllib.parse.urlencode({
        "series_id": series_id,
        "api_key": api_key,
        "file_type": "json",
        "sort_order": "desc",
        "limit": 10,
    })
    url = f"https://api.stlouisfed.org/fred/series/observations?{params}"
    with urllib.request.urlopen(url, timeout=30) as response:
        return json.load(response)


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    series = sys.argv[1:] or ["DGS10", "FEDFUNDS", "CPIAUCSL", "UNRATE"]
    for series_id in series:
        payload = fetch(series_id)
        path = OUT / f"{series_id}.json"
        path.write_text(json.dumps(payload, indent=2))
        print(f"wrote {path}")


if __name__ == "__main__":
    main()
