from bondworks import Client

client = Client(api_key="demo-bondworks-local-key")

rfq = client.create_rfq(
    bond_code="UST-10Y-2036",
    side="BUY",
    quantity=5_000_000,
    dealers=["CITI", "JPM", "GS", "MS", "BOFA", "BARC", "DB"],
)

print(rfq)
