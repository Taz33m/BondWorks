from __future__ import annotations

from decimal import Decimal
from typing import Any

from pydantic import BaseModel, Field


class Quote(BaseModel):
    id: str
    rfq_id: str
    dealer: str
    side: str
    price: Decimal
    yield_value: Decimal | None = None
    spread_bps: Decimal | None = None
    quantity: Decimal
    status: str
    latency_ms: int | None = None


class QuoteList(list[Quote]):
    def best_by_price(self, rfq_side: str = "BUY") -> Quote:
        active = [quote for quote in self if quote.status == "ACTIVE"]
        if not active:
            raise ValueError("No active quotes available")
        return min(active, key=lambda q: q.price) if rfq_side == "BUY" else max(active, key=lambda q: q.price)


class Rfq(BaseModel):
    id: str
    side: str
    quantity: Decimal
    status: str
    bond_code: str | None = None
    expires_at: str | None = None


class Trade(BaseModel):
    trade_id: str = Field(alias="trade_id")
    rfq_id: str
    status: str
    execution_price: Decimal
    dealer: str | None = None
    selected_quote_rank: int | None = None
    analytics: dict[str, Any] | None = None


class ExecutionReport(BaseModel):
    trade_id: str
    selected_quote_rank: int | None = None
    slippage_bps: Decimal | None = None
    spread_paid_bps: Decimal | None = None
    cover_price: Decimal | None = None
    cover_distance_bps: Decimal | None = None
    tape_vwap: Decimal | None = None
    missed_savings_usd: Decimal | None = None
