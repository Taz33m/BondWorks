#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import json
import os
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "data" / "generated" / "agent_fixtures" / "dealer_axes_explanations.json"
MODEL = os.getenv("OPENROUTER_MODEL", "nvidia/nemotron-3-super-120b-a12b:free")

PROMPT = """Generate concise qualitative dealer-axis explanations for BondWorks Lite.
Return JSON array only. Each item: dealer, bond_code, stance, inventory_fit_score,
inventory_adjustment_bps, description. Use only dealers CITI,JPM,GS,MS,BOFA,BARC,DB
and bond_code UST-10Y-2036. Descriptions max 280 chars. Do not invent prices."""


def fallback():
    return [
        {
            "dealer": "CITI",
            "bond_code": "UST-10Y-2036",
            "stance": "WANTS_TO_SELL",
            "inventory_fit_score": 0.91,
            "inventory_adjustment_bps": -1.2,
            "description": "CITI is axed to reduce 10Y duration inventory and is likely to show a tighter offer.",
        }
    ]


def validate(items):
    dealers = {"CITI", "JPM", "GS", "MS", "BOFA", "BARC", "DB"}
    stances = {"WANTS_TO_SELL", "NEUTRAL", "WANTS_TO_BUY", "LOW_BALANCE_SHEET", "LOW_BALANCE"}
    for item in items:
        assert item["dealer"] in dealers
        assert item["bond_code"] == "UST-10Y-2036"
        assert item["stance"] in stances
        assert 0 <= float(item["inventory_fit_score"]) <= 1
        assert -3 <= float(item["inventory_adjustment_bps"]) <= 3
        assert len(item["description"]) <= 280


def generate():
    api_key = os.getenv("OPENROUTER_API_KEY")
    if not api_key:
        return fallback(), "FALLBACK_NO_KEY"
    body = {
        "model": MODEL,
        "messages": [{"role": "user", "content": PROMPT}],
        "temperature": 0.2,
    }
    request = urllib.request.Request(
        "https://openrouter.ai/api/v1/chat/completions",
        data=json.dumps(body).encode(),
        headers={"Content-Type": "application/json", "Authorization": f"Bearer {api_key}"},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=60) as response:
        payload = json.load(response)
    content = payload["choices"][0]["message"]["content"]
    return json.loads(content), "VALIDATED"


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    items, status = generate()
    validate(items)
    wrapped = {
        "fixture_type": "dealer_axes_explanations",
        "model": MODEL,
        "prompt_hash": hashlib.sha256(PROMPT.encode()).hexdigest(),
        "validation_status": status,
        "payload": items,
    }
    OUT.write_text(json.dumps(wrapped, indent=2))
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
