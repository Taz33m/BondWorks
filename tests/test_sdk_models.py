from decimal import Decimal

from bondworks.models import Quote, QuoteList


def test_best_by_price_buy_and_sell():
    quotes = QuoteList(
        [
            Quote(id="q1", rfq_id="r1", dealer="JPM", side="OFFER", price=Decimal("101.204"), quantity=Decimal("5000000"), status="ACTIVE"),
            Quote(id="q2", rfq_id="r1", dealer="CITI", side="OFFER", price=Decimal("101.183"), quantity=Decimal("5000000"), status="ACTIVE"),
        ]
    )

    assert quotes.best_by_price("BUY").dealer == "CITI"
    assert quotes.best_by_price("SELL").dealer == "JPM"
