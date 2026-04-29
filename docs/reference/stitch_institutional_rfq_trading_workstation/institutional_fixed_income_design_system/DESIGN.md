---
name: Institutional Fixed-Income Design System
colors:
  surface: '#10131a'
  surface-dim: '#10131a'
  surface-bright: '#363941'
  surface-container-lowest: '#0b0e15'
  surface-container-low: '#191b23'
  surface-container: '#1d2027'
  surface-container-high: '#272a31'
  surface-container-highest: '#32353c'
  on-surface: '#e1e2ec'
  on-surface-variant: '#c2c6d6'
  inverse-surface: '#e1e2ec'
  inverse-on-surface: '#2e3038'
  outline: '#8c909f'
  outline-variant: '#424754'
  surface-tint: '#adc6ff'
  primary: '#adc6ff'
  on-primary: '#002e6a'
  primary-container: '#4d8eff'
  on-primary-container: '#00285d'
  inverse-primary: '#005ac2'
  secondary: '#c0c6db'
  on-secondary: '#293040'
  secondary-container: '#404758'
  on-secondary-container: '#aeb5c9'
  tertiary: '#ffb786'
  on-tertiary: '#502400'
  tertiary-container: '#df7412'
  on-tertiary-container: '#461f00'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc6ff'
  on-primary-fixed: '#001a42'
  on-primary-fixed-variant: '#004395'
  secondary-fixed: '#dce2f7'
  secondary-fixed-dim: '#c0c6db'
  on-secondary-fixed: '#141b2b'
  on-secondary-fixed-variant: '#404758'
  tertiary-fixed: '#ffdcc6'
  tertiary-fixed-dim: '#ffb786'
  on-tertiary-fixed: '#311400'
  on-tertiary-fixed-variant: '#723600'
  background: '#10131a'
  on-background: '#e1e2ec'
  surface-variant: '#32353c'
typography:
  data-lg:
    fontFamily: monospace
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  data-md:
    fontFamily: monospace
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  data-sm:
    fontFamily: monospace
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-caps:
    fontFamily: inter
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.05em
  ui-text:
    fontFamily: publicSans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
spacing:
  unit: 4px
  panel-gap: 2px
  cell-padding-x: 8px
  cell-padding-y: 4px
  container-margin: 12px
---

## Brand & Style

The design system is engineered for high-velocity institutional trading, where information density and cognitive clarity are paramount. It adopts a **Data-Driven Minimalism** style, stripping away all decorative elements to prioritize rapid data scanning and execution.

The aesthetic is inspired by legacy terminal interfaces but refined with modern layouts. It evokes a sense of mission-critical stability and cold precision. There is no room for playfulness; every pixel serves a functional purpose. The interface remains quiet until a market event occurs, ensuring that the trader's attention is directed exactly where it is needed most.

## Colors

The palette is anchored in a "Deep Navy" dark mode to reduce eye strain during long trading sessions.

- **Background & Surfaces:** A tiered dark approach. The core background is the darkest (#080B12), while active panels and modules use Charcoal Blue (#111827) to create a subtle sense of hierarchy without needing heavy shadows.
- **Accents & Semantics:** Color is used exclusively as a functional signal. Success Green indicates the best price or a completed trade; Risk Red warns of expired quotes or critical errors.
- **Interactive States:** Blue (#3B82F6) is reserved for active selections, hover states on actionable rows, and focus indicators.

## Typography

The typography strategy bifurcates UI navigation and financial data.

- **UI Labels:** Use **Public Sans** for its institutional and clear character. Use Uppercase styling for section headers and table column titles to differentiate them from the data they contain.
- **Financial Data:** All numeric values, timestamps, CUSIPs, and ticker symbols must use a **Monospace font** (JetBrains Mono or the system monospace equivalent). This ensures that columns of numbers align perfectly (tabular figures), allowing traders to compare yields and spreads at a glance without visual shifting.
- **Density:** Font sizes are kept small (11px to 14px) to maximize the volume of information visible on a single screen.

## Layout & Spacing

This design system utilizes a **Fluid Grid** model designed for ultra-wide monitors. The layout is optimized for "Full-Screen Density," minimizing whitespace.

- **Panel Structure:** Individual modules (Order Book, Blotter, Inventory) are separated by a consistent 2px gap, creating a seamless "tiled" appearance.
- **Table Density:** Compactness is the priority. Rows should be no taller than 24px-28px.
- **Alignment:** All numeric data—specifically yields, prices, and volumes—must be right-aligned. Text-based data like Tickers or Side (Buy/Sell) remains left-aligned. This ensures the decimal points in financial figures are visually stacked for rapid comparison.

## Elevation & Depth

In a high-density trading environment, shadows are distracting and consume valuable screen real estate. Instead, this design system uses **Bold Borders** and **Tonal Layers** to establish hierarchy.

- **Panel Definition:** Borders are thin (1px) and use the Slate color (#243044).
- **Layering:** To indicate a modal or a pop-over (such as a trade ticket), use a slightly lighter surface color than the panel below it, paired with a subtle 1px border.
- **Z-Index Strategy:** Only two levels of depth are permitted: the base "Workstation" level and the "Intervention" level (for active trade entry or configuration).

## Shapes

The design system uses a **Sharp (0px)** roundedness philosophy. Every panel, button, and input field features 90-degree corners. This reinforces the terminal-inspired aesthetic and ensures that every pixel is used for data rather than anti-aliasing curves.

Status badges and tags may use a 1px radius only if necessary to distinguish them as floating elements, but the preference is for a completely rectangular architecture to maintain the "grid" integrity of the workstation.

## Components

- **Compact Tables:** The core of the system. Headers must be sticky with a darker background than the rows. Hover states on rows should use a subtle highlight (#1F2937).
- **Status Badges:** Small, rectangular tags with background colors mapped to the status tokens. Text inside should be monospace and bold for readability.
- **Metric Cards:** Used in the "Top Rail" for key market indicators (e.g., US10Y Yield). These consist of a tiny UI label at the top and a large monospace value below.
- **Trade Inputs:** Input fields should be "flush" with the panel design, using the Slate Border for definition. Active inputs should have a 1px Blue (#3B82F6) glow-less border.
- **Action Buttons:** Standard buttons should be ghost-style with borders; "Primary Action" buttons (like BUY or SELL) should use full color fills with high-contrast text.
- **Narrow Sidebar:** A collapsible navigation rail on the far left, containing icon-only links to different views (Inventory, Analytics, Blotter).