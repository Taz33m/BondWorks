#!/usr/bin/env python3
from __future__ import annotations

import csv
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CURATED = ROOT / "data" / "curated"

DEALERS = {"CITI", "JPM", "GS", "MS", "BOFA", "BARC", "DB"}
STANCES = {"WANTS_TO_SELL", "NEUTRAL", "WANTS_TO_BUY", "LOW_BALANCE_SHEET", "LOW_BALANCE"}


def rows(name: str):
    with (CURATED / name).open(newline="") as f:
        yield from csv.DictReader(f)


def require(condition: bool, message: str):
    if not condition:
        raise AssertionError(message)


def validate_axes():
    instruments = {row["bond_code"] for row in rows("instruments.csv")}
    for row in rows("dealer_axes.csv"):
        require(row["dealer"] in DEALERS, f"unknown dealer {row['dealer']}")
        require(row["bond_code"] in instruments, f"unknown bond {row['bond_code']}")
        require(row["stance"] in STANCES, f"invalid stance {row['stance']}")
        fit = float(row["inventory_fit_score"])
        adj = float(row["inventory_adjustment_bps"])
        require(0 <= fit <= 1, f"inventory fit out of range: {fit}")
        require(-3 <= adj <= 3, f"inventory adjustment out of range: {adj}")
        require(len(row["description"]) <= 280, "description too long")


def validate_dealer_stats():
    for row in rows("dealer_statistics_snapshot.csv"):
        require(row["dealer"] in DEALERS, f"unknown dealer {row['dealer']}")
        for key in ["historical_price_quality", "response_rate", "latency_score", "sector_win_rate", "size_capacity", "quote_reliability"]:
            value = float(row[key])
            require(0 <= value <= 1, f"{key} out of range: {value}")


def validate_market_prints():
    instruments = {row["bond_code"] for row in rows("instruments.csv")}
    for row in rows("market_prints.csv"):
        require(row["bond_code"] in instruments, f"unknown print bond {row['bond_code']}")
        require(float(row["price"]) > 0, "price must be positive")
        require(float(row["quantity"]) > 0, "quantity must be positive")


def main():
    validate_axes()
    validate_dealer_stats()
    validate_market_prints()
    print("data validation passed")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"data validation failed: {exc}", file=sys.stderr)
        raise
