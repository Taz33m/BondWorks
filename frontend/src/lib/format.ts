export function money(value: unknown, compact = false) {
  const n = Number(value ?? 0);
  if (compact && Math.abs(n) >= 1_000_000) return `$${Math.round(n / 1_000_000)}MM`;
  return n.toLocaleString('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });
}

export function px(value: unknown) {
  if (value === null || value === undefined || value === '') return '--';
  return Number(value).toFixed(3);
}

export function bps(value: unknown) {
  if (value === null || value === undefined || value === '') return '--';
  return `${Number(value).toFixed(1)} bps`;
}

export function pct(value: unknown, decimals = 3) {
  if (value === null || value === undefined || value === '') return '--';
  return `${Number(value).toFixed(decimals)}%`;
}

export function ms(value: unknown) {
  if (value === null || value === undefined || value === '') return '--';
  const n = Number(value);
  return n >= 1000 ? `${(n / 1000).toFixed(1)}s` : `${Math.round(n)}ms`;
}

export function utc(value: unknown) {
  if (!value) return '--';
  return new Date(String(value)).toISOString().replace('T', ' ').slice(0, 23);
}

export function localTime(value: unknown) {
  if (!value) return '--';
  return new Date(String(value)).toLocaleTimeString([], { hour12: false });
}
