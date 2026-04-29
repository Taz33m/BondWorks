# BondWorks Lite HyperFrames Design

## Creative Direction

The film should feel like a fixed-income RFQ workstation, not a SaaS dashboard. It should borrow the density, composure, and data-first attitude of a Bloomberg-like terminal while staying readable in a modern web-app demo.

The strongest visual proof is the Live RFQ Blotter. Every other screen exists to frame one institutional execution story:

Dealer selection -> quote competition -> execution decision -> best-execution proof -> audit trail.

## Visual Language

- Canvas: 1920x1080.
- Background: near-black graphite.
- Surfaces: thin bordered panels, compact header bars, no card nesting.
- Motion: panels slide subtly, table rows arrive progressively, highlights move with restraint.
- Typography: Public Sans for UI labels and JetBrains Mono for prices, IDs, yields, spreads, timestamps, and system text.
- Tone: controlled, professional, real-time, audit-ready.

## Palette

- `#080a0e` page background.
- `#101318` base surface.
- `#11161d` header and sidebar surface.
- `#181d24` primary panel.
- `#222832` raised panel.
- `#2a323c` border.
- `#3b4652` strong border.
- `#d8dde5` primary text.
- `#8d98a6` muted labels.
- `#667280` secondary muted text.
- `#7895b2` steel blue accent.
- `#a6b8c9` light blue accent.
- `#6fa782` execution/best-state green.
- `#c49a61` warning/attention amber.
- `#c87870` expired/error red.

## Components

- Top terminal status bar with product context, session, market state, and role.
- Compact icon sidebar with BondWorks logo.
- RFQ ticket panel with instrument, side, size, settlement, dealer recommendations, and simulated tape.
- Live RFQ blotter with progressive dealer rows, best quote highlight, countdown, and footer metrics.
- Execution decision overlay requiring best-execution rationale when selecting a non-best quote.
- Trade analytics screen with quote-rank, cover price, cover distance, missed savings, tape VWAP, and latency chart.
- Audit timeline with immutable lifecycle events.
- Final architecture strip: React/TypeScript, Spring Boot, Postgres, Redis, Redpanda, WebSocket, Python SDK.

## Must Avoid

- SaaS dashboard look.
- Crypto/Robinhood visual language.
- AI-app framing.
- Neon gradients, floating blobs, oversized hero cards, or generic admin panels.
- Overly playful type or inconsistent font usage.
