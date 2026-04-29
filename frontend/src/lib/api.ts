const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export type AnyRecord = Record<string, any>;

export function token() {
  return localStorage.getItem('bondworks_token');
}

export function setToken(next: string) {
  localStorage.setItem('bondworks_token', next);
}

export function clearToken() {
  localStorage.removeItem('bondworks_token');
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set('Content-Type', 'application/json');
  const current = token();
  if (current) headers.set('Authorization', `Bearer ${current}`);
  const res = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (!res.ok) {
    let body: AnyRecord = {};
    try {
      body = await res.json();
    } catch {
      body = { message: res.statusText };
    }
    const err = new Error(body.message ?? 'Request failed') as Error & { status?: number; code?: string };
    err.status = res.status;
    err.code = body.code;
    throw err;
  }
  return res.json();
}

export const api = {
  demoLogin(email = 'trader@demo.com') {
    return request<AnyRecord>('/api/auth/demo-login', { method: 'POST', body: JSON.stringify({ email }) });
  },
  bonds() {
    return request<AnyRecord[]>('/api/bonds');
  },
  dealers() {
    return request<AnyRecord[]>('/api/dealers');
  },
  recommendations(bondId: string, side: string, quantity: number) {
    return request<AnyRecord[]>(`/api/rfqs/recommendations?bond_id=${bondId}&side=${side}&quantity=${quantity}`);
  },
  createRfq(body: AnyRecord) {
    return request<AnyRecord>('/api/rfqs', { method: 'POST', body: JSON.stringify(body) });
  },
  rfqs() {
    return request<AnyRecord[]>('/api/rfqs');
  },
  rfq(id: string) {
    return request<AnyRecord>(`/api/rfqs/${id}`);
  },
  quotes(id: string) {
    return request<AnyRecord[]>(`/api/rfqs/${id}/quotes`);
  },
  execute(rfqId: string, body: AnyRecord) {
    const key = crypto.randomUUID();
    return request<AnyRecord>(`/api/rfqs/${rfqId}/execute`, {
      method: 'POST',
      headers: { 'Idempotency-Key': key },
      body: JSON.stringify(body)
    });
  },
  cancel(id: string) {
    return request<AnyRecord>(`/api/rfqs/${id}/cancel`, { method: 'POST', body: '{}' });
  },
  trade(id: string) {
    return request<AnyRecord>(`/api/trades/${id}`);
  },
  trades() {
    return request<AnyRecord[]>('/api/trades');
  },
  analytics(id: string) {
    return request<AnyRecord>(`/api/trades/${id}/analytics`);
  },
  audit(search = '', eventType = 'ALL_EVENTS') {
    return request<AnyRecord[]>(`/api/audit-events?search=${encodeURIComponent(search)}&event_type=${eventType}`);
  },
  eventReplay(rfqId: string) {
    return request<AnyRecord>(`/api/event-replay/rfqs/${rfqId}`);
  },
  dealerPerformance() {
    return request<AnyRecord>('/api/dealers/performance');
  },
  marketPrints(bondId: string) {
    return request<AnyRecord[]>(`/api/bonds/${bondId}/market-prints`);
  },
  dashboard() {
    return request<AnyRecord>('/api/dashboard');
  },
  marketContext() {
    return request<AnyRecord>('/api/market/context');
  }
};

export const WS_URL = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8080/ws';
