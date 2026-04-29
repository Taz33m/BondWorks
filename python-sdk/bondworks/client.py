from __future__ import annotations

import time
from typing import Iterable

import httpx

from .exceptions import BondWorksApiError
from .models import ExecutionReport, Quote, QuoteList, Rfq, Trade


class Client:
    def __init__(self, api_key: str = "demo-bondworks-local-key", base_url: str = "http://localhost:8080"):
        self.base_url = base_url.rstrip("/")
        self._client = httpx.Client(
            base_url=self.base_url,
            headers={"X-API-Key": api_key, "Content-Type": "application/json"},
            timeout=15,
        )

    def create_rfq(
        self,
        bond_id: str | None = None,
        bond_code: str | None = "UST-10Y-2036",
        side: str = "BUY",
        quantity: int = 5_000_000,
        dealers: Iterable[str] = ("CITI", "JPM", "GS", "MS", "BOFA"),
        time_in_force_seconds: int = 30,
        settlement_date: str | None = None,
    ) -> Rfq:
        payload = {
            "side": side,
            "quantity": quantity,
            "dealer_codes": list(dealers),
            "time_in_force_seconds": time_in_force_seconds,
        }
        if bond_id:
            payload["bond_id"] = bond_id
        elif bond_code:
            payload["bond_code"] = bond_code
        if settlement_date:
            payload["settlement_date"] = settlement_date
        return Rfq.model_validate(self._request("POST", "/api/rfqs", json=payload))

    def get_rfq(self, rfq_id: str) -> Rfq:
        return Rfq.model_validate(self._request("GET", f"/api/rfqs/{rfq_id}"))

    def get_quotes(self, rfq_id: str) -> QuoteList:
        return QuoteList(Quote.model_validate(row) for row in self._request("GET", f"/api/rfqs/{rfq_id}/quotes"))

    def wait_for_quotes(self, rfq_id: str, timeout: float = 5.0, min_quotes: int = 1) -> QuoteList:
        deadline = time.time() + timeout
        last = QuoteList()
        while time.time() < deadline:
            last = self.get_quotes(rfq_id)
            if len([quote for quote in last if quote.status == "ACTIVE"]) >= min_quotes:
                return last
            time.sleep(0.25)
        return last

    def execute_quote(
        self,
        rfq_id: str,
        quote_id: str,
        reason_code: str | None = None,
        reason_text: str | None = None,
    ) -> Trade:
        payload = {"quote_id": quote_id}
        if reason_code:
            payload["reason_code"] = reason_code
            payload["reason_text"] = reason_text or ""
        headers = {"Idempotency-Key": f"sdk-{rfq_id}-{quote_id}-{int(time.time() * 1000)}"}
        return Trade.model_validate(self._request("POST", f"/api/rfqs/{rfq_id}/execute", json=payload, headers=headers))

    def execute_best_quote(self, rfq_id: str, rfq_side: str = "BUY") -> Trade:
        quote = self.get_quotes(rfq_id).best_by_price(rfq_side)
        return self.execute_quote(rfq_id, quote.id)

    def get_execution_report(self, trade_id: str) -> ExecutionReport:
        return ExecutionReport.model_validate(self._request("GET", f"/api/trades/{trade_id}/analytics"))

    def _request(self, method: str, path: str, **kwargs):
        response = self._client.request(method, path, **kwargs)
        if response.is_error:
            try:
                body = response.json()
            except ValueError:
                body = {"message": response.text}
            raise BondWorksApiError(response.status_code, body.get("code"), body.get("message", "Request failed"))
        return response.json()
