from bondworks import Client

client = Client(api_key="demo-bondworks-local-key")

rfq = client.create_rfq(
    bond_code="UST-10Y-2036",
    side="BUY",
    quantity=5_000_000,
    dealers=["CITI", "JPM", "GS", "MS", "BOFA", "BARC", "DB"],
)

quotes = client.wait_for_quotes(rfq.id, timeout=5, min_quotes=5)
best_quote = quotes.best_by_price(rfq.side)
trade = client.execute_quote(rfq.id, best_quote.id)
report = client.get_execution_report(trade.trade_id)

print(f"Executed {trade.dealer} at {trade.execution_price}")
print(f"Slippage: {report.slippage_bps} bps")
print(f"Cover: {report.cover_price} ({report.cover_distance_bps} bps)")
