# BondWorks Lite Storyboard

## 0.0s-5.5s: Opening Workstation

Show a compact terminal shell with the BondWorks logo, status bar, and title.

On-screen message: "Dealer selection -> quote competition -> execution decision -> best-execution proof."

Purpose: frame the product as an institutional execution workstation immediately.

## 5.5s-14.0s: Dealer Selection Intelligence

Show the New RFQ ticket for `UST 4.25 2036`, `BUY`, `$5,000,000`, `T+2`.

Right rail shows instrument intelligence, mid price, benchmark yield, and simulated market tape. Dealer recommendation rows highlight score and inventory axe:

- CITI: 92, wants to sell UST-10Y.
- JPM: 88, fastest response, neutral inventory.
- GS: 79, wants to buy UST-10Y.

Purpose: answer "who should I ask for liquidity?"

## 14.0s-30.0s: Live RFQ Blotter

Cut to the Live RFQ Blotter. RFQ status moves from `OPEN` to `QUOTING`. Dealer quotes appear progressively.

Quote table columns:

Dealer | Status | Price | Yield | Spread | Size | Latency | Action

Highlight CITI as best price. Show JPM as faster but slightly wider. Mark one dealer slow/no-response and one expired.

Purpose: make real-time dealer liquidity the visual centerpiece.

## 30.0s-39.0s: Execution Decision

Show a non-best execution decision: JPM is selected over CITI. A best-execution rationale panel appears.

Selected reasons:

- Faster response.
- Settlement reliability.

Purpose: connect trading discretion to compliance/auditability.

## 39.0s-50.0s: Trade Analytics

Show the completed trade analytics screen.

Metrics:

- Executed dealer: JPM.
- Best quote: CITI.
- Selected quote rank: #2 of 6.
- Cover price: 101.183.
- Cover distance: 0.8 bps.
- Tape VWAP: 101.171.
- Missed savings: `$400`.

Include quote comparison and latency bars.

Purpose: prove the platform is more than a quote simulator.

## 50.0s-56.0s: Audit Trail

Show immutable audit events:

- RFQ_CREATED
- RFQ_SENT_TO_DEALERS
- QUOTE_RECEIVED
- BEST_EXECUTION_RATIONALE_SUBMITTED
- QUOTE_EXECUTED
- TRADE_CREATED
- ANALYTICS_GENERATED

Purpose: show financial infrastructure discipline.

## 56.0s-60.0s: Architecture Close

Show the backend architecture strip:

React/TypeScript -> Spring Boot -> PostgreSQL + Redis -> Redpanda -> WebSocket -> Python SDK.

Final line: "Public market context + simulated proprietary RFQ microstructure."
