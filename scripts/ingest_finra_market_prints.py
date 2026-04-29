#!/usr/bin/env python3
from __future__ import annotations

import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    client_id = os.getenv("FINRA_CLIENT_ID")
    client_secret = os.getenv("FINRA_CLIENT_SECRET")
    if not client_id or not client_secret:
        raise SystemExit("FINRA credentials are not configured. V1 uses data/curated/market_prints.csv simulated TRACE-like prints.")
    raise SystemExit("FINRA ingestion placeholder: add entitlement-specific endpoint and schema mapping once API access is confirmed.")


if __name__ == "__main__":
    main()
