#!/usr/bin/env python3
from __future__ import annotations

import csv
import json
import os
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "data" / "raw" / "openfigi"
CURATED = ROOT / "data" / "curated" / "instruments.csv"


def main():
    RAW.mkdir(parents=True, exist_ok=True)
    jobs = []
    with CURATED.open(newline="") as f:
        for row in csv.DictReader(f):
            if row["ticker"] and row["asset_class"] == "Bond":
                jobs.append({"idType": "TICKER", "idValue": row["ticker"], "marketSecDes": "Corp" if "Corporate" in row["sector"] else "Govt"})
    request = urllib.request.Request(
        "https://api.openfigi.com/v3/mapping",
        data=json.dumps(jobs[:5]).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    if os.getenv("OPENFIGI_API_KEY"):
        request.add_header("X-OPENFIGI-APIKEY", os.environ["OPENFIGI_API_KEY"])
    with urllib.request.urlopen(request, timeout=30) as response:
        payload = json.load(response)
    (RAW / "mapping_response.json").write_text(json.dumps(payload, indent=2))
    print(f"wrote {RAW / 'mapping_response.json'}")


if __name__ == "__main__":
    main()
