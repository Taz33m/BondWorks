import { useCallback, useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom';
import {
  Activity,
  Bell,
  BriefcaseBusiness,
  Clock3,
  FileText,
  History,
  LayoutGrid,
  ListTree,
  LogIn,
  LogOut,
  PlusSquare,
  Search,
  Send,
  Settings,
  ShieldCheck,
  Shuffle,
  TimerReset
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from 'recharts';
import { api, AnyRecord, clearToken, setToken, token } from './lib/api';
import { bps, localTime, money, ms, pct, px, utc } from './lib/format';
import { useLiveRfq } from './lib/useLiveRfq';

const demoBondCode = 'UST-10Y-2036';
const rationaleReasons = [
  ['FASTER_RESPONSE', 'Faster response'],
  ['RELATIONSHIP_COVERAGE', 'Relationship / coverage'],
  ['SETTLEMENT_RELIABILITY', 'Settlement reliability'],
  ['SIZE_CAPACITY', 'Larger size capacity'],
  ['NEGOTIATED_IMPROVEMENT', 'Price improvement negotiated'],
  ['OTHER', 'Other']
];

function App() {
  const [authToken, setAuthToken] = useState(() => token());

  return (
    <Routes>
      <Route path="/login" element={authToken ? <Navigate to="/dashboard" replace /> : <Login onLogin={setAuthToken} />} />
      <Route path="/*" element={authToken ? <Shell onLogout={() => setAuthToken(null)} /> : <Navigate to="/login" replace />} />
    </Routes>
  );
}

function Shell({ onLogout }: { onLogout: () => void }) {
  const location = useLocation();
  const navigate = useNavigate();
  const active = location.pathname.startsWith('/rfq/new') ? 'new'
    : location.pathname.startsWith('/rfqs') ? 'rfqs'
    : location.pathname.startsWith('/rfq/') ? 'rfqs'
    : location.pathname.startsWith('/trades') ? 'trades'
    : location.pathname.startsWith('/dealers') ? 'dealers'
    : location.pathname.startsWith('/audit') ? 'audit'
    : location.pathname.startsWith('/docs') ? 'docs'
    : 'dashboard';

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">BW</div>
        <NavIcon to="/dashboard" id="dashboard" active={active} icon={<LayoutGrid size={20} />} label="Dash" />
        <NavIcon to="/rfq/new" id="new" active={active} icon={<PlusSquare size={20} />} label="New" />
        <NavIcon to="/rfqs" id="rfqs" active={active} icon={<ListTree size={20} />} label="RFQs" />
        <NavIcon to="/trades" id="trades" active={active} icon={<Shuffle size={20} />} label="Trades" />
        <NavIcon to="/dealers" id="dealers" active={active} icon={<BriefcaseBusiness size={20} />} label="Dealers" />
        <NavIcon to="/audit" id="audit" active={active} icon={<History size={20} />} label="Audit" />
        <div style={{ marginTop: 'auto' }} />
        <NavIcon to="/docs" id="docs" active={active} icon={<FileText size={20} />} label="Docs" />
      </aside>
      <header className="topbar">
        <div style={{ display: 'flex', alignItems: 'center', gap: 18 }}>
          <div className="topbar-title">BondWorks Lite // {topbarLabel(active)}</div>
          <div style={{ width: 1, height: 18, background: 'var(--border)' }} />
          <div className="terminal-tabs">
            <span className="active">SESSION_ACTIVE</span>
            <span>MARKET_OPEN</span>
            <span>SIM_TRACE</span>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <Bell size={18} color="var(--muted)" />
          <Clock3 size={18} color="var(--muted)" />
          <Settings size={18} color="var(--muted)" />
          <div style={{ width: 1, height: 24, background: 'var(--border)' }} />
          <span className="mono" style={{ fontSize: 11, color: 'var(--text)' }}>TRADER_09</span>
          <div style={{ width: 26, height: 26, background: 'var(--surface-high)', color: 'var(--blue-soft)', display: 'grid', placeItems: 'center', fontWeight: 900 }}>JD</div>
          <button
            className="icon-action"
            title="Log out"
            onClick={() => {
              clearToken();
              onLogout();
              navigate('/login', { replace: true });
            }}
          >
            <LogOut size={16} />
          </button>
        </div>
      </header>
      <main className="page">
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/rfqs" element={<RfqBlotter />} />
          <Route path="/rfq/new" element={<NewRfq />} />
          <Route path="/rfq/demo" element={<Navigate to="/rfqs" replace />} />
          <Route path="/rfq/:id" element={<LiveRfq />} />
          <Route path="/trades" element={<Trades />} />
          <Route path="/trades/:id" element={<TradeAnalytics />} />
          <Route path="/dealers" element={<DealerPerformance />} />
          <Route path="/audit" element={<AuditLog />} />
          <Route path="/docs" element={<Docs />} />
        </Routes>
      </main>
    </div>
  );
}

function NavIcon({ to, id, active, icon, label }: { to: string; id: string; active: string; icon: JSX.Element; label: string }) {
  return (
    <Link className={`nav-item ${active === id ? 'active' : ''}`} to={to} title={label}>
      {icon}
    </Link>
  );
}

function topbarLabel(active: string) {
  return {
    dashboard: 'Institutional Terminal',
    new: 'New RFQ Ticket',
    rfqs: 'RFQ Blotter',
    trades: 'Analytics',
    dealers: 'Dealer Analytics',
    audit: 'Audit Log',
    docs: 'Docs'
  }[active] ?? 'Terminal';
}

function Login({ onLogin }: { onLogin: (next: string) => void }) {
  const navigate = useNavigate();
  const login = useMutation({
    mutationFn: (email: string) => api.demoLogin(email),
    onSuccess: data => {
      setToken(data.token);
      onLogin(data.token);
      navigate('/dashboard', { replace: true });
    }
  });
  return (
    <div className="login-shell">
      <section className="modal login-panel">
        <div className="topbar-title" style={{ marginBottom: 12 }}>BondWorks Lite // Demo Login</div>
        <h1 style={{ margin: '0 0 8px', fontSize: 30, lineHeight: 1.15, fontWeight: 700 }}>Fixed-Income RFQ Workstation</h1>
        <p style={{ color: 'var(--muted)', marginBottom: 22 }}>Simulated dealer liquidity, execution analytics, and audit-ready trade lifecycle.</p>
        <div style={{ display: 'grid', gap: 10 }}>
          {[
            ['trader@demo.com', 'CONTINUE AS TRADER'],
            ['dealer@demo.com', 'CONTINUE AS DEALER'],
            ['admin@demo.com', 'CONTINUE AS ADMIN']
          ].map(([email, label]) => (
            <button className="action primary" style={{ height: 44 }} key={email} disabled={login.isPending} onClick={() => login.mutate(email)}>
              <LogIn size={16} style={{ verticalAlign: 'middle', marginRight: 8 }} />
              {login.isPending ? 'AUTHENTICATING...' : label}
            </button>
          ))}
        </div>
        {login.error ? <div className="login-error">{login.error.message}</div> : null}
      </section>
    </div>
  );
}

function RfqBlotter() {
  const rfqs = useQuery({ queryKey: ['rfqs'], queryFn: api.rfqs, refetchInterval: 3500 });
  const rows = rfqs.data ?? [];
  const activeCount = rows.filter(r => ['OPEN', 'QUOTING'].includes(String(r.status))).length;

  return (
    <>
      <section className="metric-grid">
        <Metric label="RFQs On Blotter" value={rows.length} color="blue" />
        <Metric label="Active RFQs" value={activeCount} color={activeCount ? 'green' : undefined} />
        <Metric label="Executed" value={rows.filter(r => r.status === 'EXECUTED').length} />
        <Metric label="Expired / Cancelled" value={rows.filter(r => ['EXPIRED', 'CANCELLED'].includes(String(r.status))).length} color="orange" />
      </section>
      <section className="panel-low" style={{ flex: 1, overflow: 'auto' }}>
        <div className="panel-header">
          <span className="label">RFQ Blotter</span>
          <Link className="action primary" to="/rfq/new" style={{ display: 'inline-flex', alignItems: 'center', height: 28 }}>New RFQ</Link>
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>RFQ ID</th>
              <th>Security</th>
              <th>Side</th>
              <th className="right">Size</th>
              <th>Status</th>
              <th className="right">Best Px</th>
              <th>Created</th>
              <th>Expires</th>
              <th className="right">Action</th>
            </tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.id}>
                <td><Link className="mono" style={{ color: 'var(--blue-soft)' }} to={`/rfq/${r.id}`}>{shortId(r.id)}</Link></td>
                <td className="mono">{r.ticker ?? r.bond_code}</td>
                <td><span className={`badge ${r.side === 'SELL' ? 'red' : ''}`}>{r.side}</span></td>
                <td className="right mono">{money(r.quantity, true)}</td>
                <td><span className={`badge ${statusTone(r.status)}`}>{r.status}</span></td>
                <td className="right mono">{px(r.best_price)}</td>
                <td className="mono">{localTime(r.created_at)}</td>
                <td className="mono">{localTime(r.expires_at)}</td>
                <td className="right"><Link className="action" to={`/rfq/${r.id}`}>Open</Link></td>
              </tr>
            ))}
            {!rfqs.isLoading && rows.length === 0 ? (
              <tr>
                <td colSpan={9} style={{ height: 140, textAlign: 'center', color: 'var(--muted)' }}>
                  No RFQs on the blotter. Create a new request to start quote competition.
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </section>
      <StatusTape left={`RFQ blotter refreshed ${rows.length} records`} />
    </>
  );
}

function NewRfq() {
  const navigate = useNavigate();
  const [side, setSide] = useState('BUY');
  const [quantity, setQuantity] = useState(5_000_000);
  const [tif, setTif] = useState(30);
  const [settlement, setSettlement] = useState(() => new Date(Date.now() + 86400000).toISOString().slice(0, 10));
  const bonds = useQuery({ queryKey: ['bonds'], queryFn: api.bonds });
  const dealers = useQuery({ queryKey: ['dealers'], queryFn: api.dealers });
  const defaultBond = bonds.data?.find(b => b.code === demoBondCode) ?? bonds.data?.[0];
  const [bondId, setBondId] = useState<string>('');
  const selectedBond = bonds.data?.find(b => b.id === (bondId || defaultBond?.id));
  const [selectedDealers, setSelectedDealers] = useState<string[]>([]);
  const [dealerSelectionTouched, setDealerSelectionTouched] = useState(false);
  const previewId = useMemo(() => `RFQ-${Math.random().toString(16).slice(2, 7).toUpperCase()}`, []);
  const effectiveDealers = selectedDealers;
  useEffect(() => {
    if (!dealerSelectionTouched && dealers.data?.length) {
      setSelectedDealers(dealers.data.map(d => d.id));
    }
  }, [dealerSelectionTouched, dealers.data]);
  const recs = useQuery({
    queryKey: ['recommendations', selectedBond?.id, side, quantity],
    queryFn: () => api.recommendations(selectedBond!.id, side, quantity),
    enabled: Boolean(selectedBond?.id)
  });
  const prints = useQuery({
    queryKey: ['market-prints', selectedBond?.id],
    queryFn: () => api.marketPrints(selectedBond!.id),
    enabled: Boolean(selectedBond?.id)
  });
  const create = useMutation({
    mutationFn: () => api.createRfq({
      bond_id: selectedBond?.id,
      side,
      quantity,
      dealer_ids: effectiveDealers,
      time_in_force_seconds: tif,
      settlement_date: settlement
    }),
    onSuccess: rfq => navigate(`/rfq/${rfq.id}`)
  });

  return (
    <>
      <div className="grid-12" style={{ flex: 1, minHeight: 0 }}>
        <section className="panel" style={{ gridColumn: 'span 8', display: 'flex', flexDirection: 'column' }}>
          <div className="panel-header">
            <span className="label">Request For Quote Entry</span>
            <span className="mono" style={{ fontSize: 10, color: 'var(--muted-2)' }}>ID PREVIEW: {previewId}</span>
          </div>
          <div style={{ padding: 30, display: 'grid', gap: 26 }}>
            <div className="field">
              <label className="label">Instrument</label>
              <select className="select" value={selectedBond?.id ?? ''} onChange={e => setBondId(e.target.value)}>
                {bonds.data?.map(b => <option key={b.id} value={b.id}>{b.ticker} // {b.code}</option>)}
              </select>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
              <div className="field">
                <label className="label">Side</label>
                <div className="side-toggle">
                  <button className={side === 'BUY' ? 'active buy' : ''} onClick={() => setSide('BUY')}>BUY</button>
                  <button className={side === 'SELL' ? 'active sell' : ''} onClick={() => setSide('SELL')}>SELL</button>
                </div>
              </div>
              <div className="field">
                <label className="label">Quantity (USD)</label>
                <input className="input mono" value={quantity} onChange={e => setQuantity(Number(e.target.value.replace(/\D/g, '')))} />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
              <div className="field">
                <label className="label">Time In Force</label>
                <select className="select" value={tif} onChange={e => setTif(Number(e.target.value))}>
                  <option value={30}>30s</option>
                  <option value={60}>60s</option>
                  <option value={90}>90s</option>
                </select>
              </div>
              <div className="field">
                <label className="label">Settlement</label>
                <input className="input mono" type="date" value={settlement} onChange={e => setSettlement(e.target.value)} />
              </div>
            </div>
            <section className="panel-low">
              <div className="panel-header">
                <span className="label">Recommended Dealers</span>
                <span className="mono" style={{ fontSize: 11, color: 'var(--blue-soft)' }}>{effectiveDealers.length} SELECTED // TIF {tif}s</span>
              </div>
              <table className="table">
                <tbody>
                  {recs.data?.map(rec => (
                    <tr key={rec.id}>
                      <td className="mono" style={{ width: 70, color: 'var(--text)' }}>{rec.code}</td>
                      <td className="mono" style={{ width: 70, color: Number(rec.score) >= 85 ? 'var(--green)' : 'var(--blue-soft)' }}>{rec.score}</td>
                      <td>{rec.rationale}</td>
                      <td className="right">
                        <input
                          type="checkbox"
                          checked={effectiveDealers.includes(rec.id)}
                          onChange={e => {
                            const current = new Set(effectiveDealers);
                            e.target.checked ? current.add(rec.id) : current.delete(rec.id);
                            setDealerSelectionTouched(true);
                            setSelectedDealers([...current]);
                          }}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
            <button className="action primary" style={{ height: 56, fontSize: 15, letterSpacing: '0.08em' }} disabled={!selectedBond || !effectiveDealers.length || create.isPending} onClick={() => create.mutate()}>
              SUBMIT RFQ <Send size={18} style={{ verticalAlign: 'middle', marginLeft: 10 }} />
            </button>
            {create.error ? <div className="badge red">{create.error.message}</div> : null}
          </div>
        </section>
        <aside className="panel-low" style={{ gridColumn: 'span 4', display: 'flex', flexDirection: 'column' }}>
          <div className="panel-header"><span className="label">Instrument Intelligence</span></div>
          <div style={{ padding: 22 }}>
            <h2 className="mono" style={{ margin: 0, fontSize: 24 }}>{selectedBond?.ticker ?? '--'}</h2>
            <p className="mono" style={{ color: 'var(--muted-2)', fontSize: 12 }}>{selectedBond?.issuer} // Demo ID {selectedBond?.cusip_like_id}</p>
            <div className="metric-grid" style={{ gridTemplateColumns: '1fr 1fr', marginTop: 20 }}>
              <Metric label="Mid Price" value={px(selectedBond?.mid_price)} />
              <Metric label="Mid Yield" value={pct(selectedBond?.mid_yield, 2)} color="blue" />
              <Metric label="Rating" value={selectedBond?.rating ?? '--'} color="green" />
              <Metric label="Est. Notional" value={money((Number(selectedBond?.mid_price ?? 100) / 100) * quantity)} />
            </div>
          </div>
          <div className="panel-header"><span className="label">Simulated Market Tape</span><span className="badge green">Real-Time</span></div>
          <div style={{ overflow: 'auto' }}>
            <table className="table">
              <tbody>
                {prints.data?.slice(0, 8).map(p => (
                  <tr key={p.id}>
                    <td className="mono">{localTime(p.printed_at)}</td>
                    <td className="right mono">{money(p.quantity, true)}</td>
                    <td className="right mono">{px(p.price)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </aside>
      </div>
      <StatusTape />
    </>
  );
}

function LiveRfq() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const rfq = useQuery({ queryKey: ['rfq', id], queryFn: () => api.rfq(id!), enabled: Boolean(id), refetchInterval: 3000 });
  const initialQuotes = useQuery({ queryKey: ['quotes', id], queryFn: () => api.quotes(id!), enabled: Boolean(id), refetchInterval: 2500 });
  const [quotes, setQuotes] = useState<AnyRecord[]>([]);
  const [rationaleTarget, setRationaleTarget] = useState<AnyRecord | null>(null);
  const cancel = useMutation({
    mutationFn: () => api.cancel(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rfq', id] });
      queryClient.invalidateQueries({ queryKey: ['rfqs'] });
    }
  });

  useLiveRfq(id, useCallback(event => {
    if (event.event_type === 'QUOTE_RECEIVED') {
      setQuotes(prev => upsertQuote(prev, event));
    }
    if (event.event_type === 'QUOTE_EXPIRED') {
      setQuotes(prev => prev.map(q => q.id === event.quote_id || q.quote_id === event.quote_id ? { ...q, status: 'EXPIRED' } : q));
    }
  }, []), useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ['rfq', id] });
    queryClient.invalidateQueries({ queryKey: ['quotes', id] });
  }, [id, queryClient]));

  const allQuotes = useMemo<AnyRecord[]>(() => {
    const base = quotes.length ? quotes : initialQuotes.data ?? [];
    return base.map(q => ({ ...q, id: q.id ?? q.quote_id, yield_value: q.yield_value ?? q.yield }));
  }, [quotes, initialQuotes.data]);

  const activeQuotes = allQuotes.filter(q => q.status === 'ACTIVE');
  const best = rankQuotes(activeQuotes, rfq.data?.side)[0];
  const responseRate = `${new Set(allQuotes.map(q => q.dealer)).size} / ${rfq.data?.dealers?.length ?? 0} DEALERS`;
  const dispersion = quoteDispersion(activeQuotes, rfq.data?.mid_price);
  const countdown = useCountdown(rfq.data?.expires_at);

  function executeQuote(quote: AnyRecord, rationale?: AnyRecord) {
    api.execute(id!, { quote_id: quote.id, ...rationale })
      .then(trade => navigate(`/trades/${trade.trade_id}`))
      .catch(err => {
        if (err.code === 'BEST_EXECUTION_RATIONALE_REQUIRED') setRationaleTarget(quote);
        else alert(err.message);
      });
  }

  if (rfq.isLoading) return <Loading />;

  return (
    <>
      <section className="panel" style={{ minHeight: 104, display: 'grid', gridTemplateColumns: '2fr 3fr 3fr 2fr', alignItems: 'center' }}>
        <div style={{ padding: '0 22px', borderRight: '1px solid var(--border)' }}>
          <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
            <span className="label">RFQ ID</span><span className="mono" style={{ color: 'var(--blue-soft)' }}>{shortId(rfq.data?.id)}</span>
            <span className={`badge ${rfq.data?.status === 'EXECUTED' ? 'green' : rfq.data?.status === 'EXPIRED' ? 'red' : 'green'}`}>{rfq.data?.status}</span>
          </div>
          <div className="mono" style={{ color: 'var(--red)', fontSize: 24, marginTop: 12 }}><TimerReset size={20} /> {countdown}</div>
        </div>
        <div style={{ padding: '0 22px', borderRight: '1px solid var(--border)', display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 18 }}>
          <Field label="Security" value={rfq.data?.ticker} />
          <Field label="Side / Size" value={`${rfq.data?.side} ${money(rfq.data?.quantity)}`} accent={rfq.data?.side === 'BUY' ? 'var(--blue-soft)' : 'var(--red)'} />
          <Field label="Settlement" value={String(rfq.data?.settlement_date ?? '--')} />
        </div>
        <div style={{ padding: '0 22px', borderRight: '1px solid var(--border)', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 18 }}>
          <Field label="Mid Price" value={px(rfq.data?.mid_price)} large />
          <Field label="Benchmark Yield" value={pct(rfq.data?.mid_yield, 2)} large />
        </div>
        <div style={{ padding: '0 22px', display: 'flex', justifyContent: 'flex-end' }}>
          <button
            className="action"
            disabled={!['OPEN', 'QUOTING'].includes(String(rfq.data?.status)) || cancel.isPending}
            onClick={() => cancel.mutate()}
          >
            CANCEL RFQ
          </button>
        </div>
      </section>

      <section className="panel-low" style={{ flex: 1, overflow: 'auto' }}>
        <table className="table">
          <thead>
            <tr>
              {['Dealer', 'Status', 'Price', 'Yield', 'Spread', 'Size', 'Latency', 'Action'].map(h => <th key={h} className={['Price', 'Yield', 'Spread', 'Size', 'Latency'].includes(h) ? 'right' : ''}>{h}</th>)}
            </tr>
          </thead>
          <tbody>
            {quoteRows(rfq.data?.dealers ?? [], allQuotes, best).map(row => (
              <tr key={row.dealer} className={`${row.best ? 'best' : ''} ${['WAITING', 'NO RESPONSE', 'EXPIRED'].includes(row.status) ? 'faded' : ''}`}>
                <td className="mono" style={{ fontWeight: 800 }}>{row.dealer}</td>
                <td title={row.quote_reason ?? ''}><span className={`badge ${row.best ? 'green' : row.status === 'ACTIVE' ? '' : row.status === 'EXPIRED' ? 'red' : 'muted'}`}>{row.best ? 'BEST' : row.status}</span></td>
                <td className="right mono" style={{ color: row.best ? 'var(--green)' : undefined }}>{row.price ? px(row.price) : '--'}</td>
                <td className="right mono">{row.yield_value ? pct(row.yield_value, 3) : '--'}</td>
                <td className="right mono">{row.spread_bps ? bps(row.spread_bps) : '--'}</td>
                <td className="right mono">{row.quantity ? money(row.quantity, true) : '--'}</td>
                <td className="right mono">{row.latency_ms ? ms(row.latency_ms) : '--'}</td>
                <td className="right">
                  {row.status === 'ACTIVE'
                    ? <button className={`action ${row.best ? 'green' : ''}`} onClick={() => row.best ? executeQuote(row) : setRationaleTarget(row)}>EXECUTE</button>
                    : <span className="mono" style={{ color: 'var(--muted-2)', fontSize: 11 }}>{row.status}</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <footer className="footer-tape">
        <span>QUOTE DISPERSION <strong style={{ color: 'var(--blue-soft)' }}>{bps(dispersion)}</strong></span>
        <span>BEST DEALER <strong style={{ color: 'var(--green)' }}>{best?.dealer ?? '--'}</strong></span>
        <span>AVG LATENCY <strong>{ms(avg(activeQuotes.map(q => Number(q.latency_ms))))}</strong></span>
        <span>RESPONSE RATE <strong>{responseRate}</strong></span>
        <span><span style={{ color: 'var(--green)' }}>●</span> CONNECTED TO LIQUIDITY HUB</span>
      </footer>

      <div className="metric-grid">
        <Metric label="US10Y Yield" value="4.218%" />
        <Metric label="US 2/10 Spread" value="-34.2" />
        <Metric label="Ticker Volume" value="$1.24B" />
        <Metric label="Market Bias" value="65% BUY" color="blue" />
      </div>
      {rationaleTarget ? <RationaleModal quote={rationaleTarget} best={best} onClose={() => setRationaleTarget(null)} onSubmit={r => executeQuote(rationaleTarget, r)} /> : null}
    </>
  );
}

function TradeAnalytics() {
  const { id } = useParams();
  const trade = useQuery({ queryKey: ['trade', id], queryFn: () => api.trade(id!), enabled: Boolean(id) });
  const quotes = useQuery({ queryKey: ['quotes', trade.data?.rfq_id], queryFn: () => api.quotes(trade.data!.rfq_id), enabled: Boolean(trade.data?.rfq_id) });
  const audit = useQuery({ queryKey: ['audit', trade.data?.rfq_id], queryFn: () => api.audit(trade.data!.rfq_id), enabled: Boolean(trade.data?.rfq_id) });
  const analytics = trade.data?.analytics ?? {};
  const quoteChart = quotes.data?.map(q => ({ dealer: q.dealer, price: Number(q.price), latency: Number(q.latency_ms), status: q.status })) ?? [];

  if (trade.isLoading) return <Loading />;
  return (
    <>
      <section className="metric-grid" style={{ gridTemplateColumns: 'repeat(8, 1fr)' }}>
        <Metric label="Trade ID" value={shortId(trade.data?.id)} />
        <Metric label="RFQ" value={shortId(trade.data?.rfq_id)} />
        <Metric label="Status" value="EXECUTED" color="green" />
        <Metric label="Instrument" value={trade.data?.ticker} />
        <Metric label="Size" value={money(trade.data?.quantity, true)} />
        <Metric label="Dealer" value={trade.data?.dealer} color="blue" />
        <Metric label="Price" value={px(trade.data?.execution_price)} />
        <Metric label="Time" value={localTime(trade.data?.executed_at)} />
      </section>
      <div className="grid-12" style={{ flex: 1 }}>
        <section className="panel" style={{ gridColumn: 'span 3', padding: 20 }}>
          <h2 className="label"><ShieldCheck size={14} /> Execution Quality</h2>
          <Quality label="Selected Quote Rank" value={`#${analytics.selected_quote_rank ?? '--'}`} green />
          <Quality label="Cover Price" value={px(analytics.cover_price)} />
          <Quality label="Cover Distance" value={bps(analytics.cover_distance_bps)} />
          <Quality label="Slippage" value={bps(analytics.slippage_bps)} />
          <Quality label="Spread Paid" value={bps(analytics.spread_paid_bps)} />
          <Quality label="Missed Savings" value={money(analytics.missed_savings_usd)} />
          <Quality label="Tape VWAP" value={px(analytics.tape_vwap)} />
          <Quality label="Exec vs Tape" value={bps(analytics.execution_vs_tape_bps)} />
        </section>
        <section className="panel" style={{ gridColumn: 'span 6', display: 'grid', gridTemplateRows: '1fr 1fr' }}>
          <div style={{ padding: 20 }}>
            <h2 className="label">Quote Comparison (Price)</h2>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={quoteChart}>
                <CartesianGrid stroke="#2a323c" />
                <XAxis dataKey="dealer" stroke="#667280" />
                <YAxis stroke="#667280" domain={['dataMin - 0.01', 'dataMax + 0.01']} />
                <Tooltip contentStyle={{ background: '#11161d', border: '1px solid #2a323c' }} />
                <Bar dataKey="price">
                  {quoteChart.map(row => <Cell key={row.dealer} fill={row.status === 'EXECUTED' ? '#6fa782' : '#7895b2'} />)}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
          <div style={{ padding: 20, borderTop: '1px solid var(--border)' }}>
            <h2 className="label">Dealer Latency (MS)</h2>
            <ResponsiveContainer width="100%" height={210}>
              <BarChart data={quoteChart}>
                <CartesianGrid stroke="#2a323c" />
                <XAxis dataKey="dealer" stroke="#667280" />
                <YAxis stroke="#667280" />
                <Tooltip contentStyle={{ background: '#11161d', border: '1px solid #2a323c' }} />
                <Bar dataKey="latency" fill="#a6b8c9" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>
        <section className="panel" style={{ gridColumn: 'span 3', overflow: 'auto' }}>
          <div className="panel-header"><span className="label">Audit Timeline</span><span className="mono">{audit.data?.length ?? 0} EVENTS</span></div>
          <div style={{ padding: 18, display: 'grid', gap: 16 }}>
            {audit.data?.slice().reverse().map(event => (
              <div key={event.id} className="panel-low" style={{ padding: 12, borderColor: event.event_type.includes('EXECUTED') ? '#3e674c' : undefined }}>
                <div className="mono" style={{ color: 'var(--muted-2)', marginBottom: 8 }}>{localTime(event.created_at)}</div>
                <div className={`badge ${event.event_type.includes('EXECUTED') || event.event_type.includes('GENERATED') ? 'green' : ''}`}>{event.event_type}</div>
                <pre className="mono" style={{ whiteSpace: 'pre-wrap', color: 'var(--muted)', fontSize: 11 }}>{JSON.stringify(event.payload, null, 2)}</pre>
              </div>
            ))}
          </div>
        </section>
      </div>
      <StatusTape />
    </>
  );
}

function AuditLog() {
  const [search, setSearch] = useState('');
  const [eventType, setEventType] = useState('ALL_EVENTS');
  const audit = useQuery({ queryKey: ['audit', search, eventType], queryFn: () => api.audit(search, eventType), refetchInterval: 2500 });
  return (
    <>
      <section className="panel" style={{ padding: 12, display: 'flex', gap: 12, alignItems: 'center' }}>
        <div style={{ position: 'relative', width: 320 }}>
          <Search size={16} style={{ position: 'absolute', left: 12, top: 11, color: 'var(--muted-2)' }} />
          <input className="input" style={{ height: 38, paddingLeft: 38 }} placeholder="Search RFQ ID, trader, or event..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select className="select" style={{ width: 220, height: 38 }} value={eventType} onChange={e => setEventType(e.target.value)}>
          {['ALL_EVENTS', 'RFQ_CREATED', 'RFQ_SENT_TO_DEALERS', 'QUOTE_RECEIVED', 'QUOTE_EXPIRED', 'QUOTE_EXECUTED', 'TRADE_CREATED', 'ANALYTICS_GENERATED', 'BEST_EXECUTION_RATIONALE_SUBMITTED', 'EXECUTION_FAILED'].map(x => <option key={x}>{x}</option>)}
        </select>
        <span className="badge green" style={{ marginLeft: 'auto' }}>Live Feed</span>
        <button className="action">CSV Export</button>
      </section>
      <section className="panel-low" style={{ flex: 1, overflow: 'auto' }}>
        <table className="table">
          <thead><tr><th>Timestamp (UTC)</th><th>Event Type</th><th>Entity ID</th><th>Actor</th><th>Details</th></tr></thead>
          <tbody>
            {audit.data?.map(event => (
              <tr key={event.id}>
                <td className="mono">{utc(event.created_at)}</td>
                <td><span className={`badge ${event.event_type.includes('FAILED') ? 'red' : event.event_type.includes('EXECUT') ? 'green' : ''}`}>{event.event_type}</span></td>
                <td className="mono" style={{ color: 'var(--blue-soft)' }}>{shortId(event.entity_id)}</td>
                <td className="mono">{event.user_id ? shortId(event.user_id) : 'SYSTEM'}</td>
                <td className="mono">{JSON.stringify(event.payload)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <StatusTape left={`Showing ${audit.data?.length ?? 0} events`} />
    </>
  );
}

function DealerPerformance() {
  const perf = useQuery({ queryKey: ['dealer-performance'], queryFn: api.dealerPerformance });
  const rows = perf.data?.dealers ?? [];
  return (
    <>
      <section className="metric-grid">
        <Metric label="Total RFQs Sent" value={perf.data?.summary?.total_rfqs_sent ?? 0} color="blue" />
        <Metric label="Aggregate Win Rate" value={`${perf.data?.summary?.aggregate_win_rate ?? 0}%`} color="blue" />
        <Metric label="Avg Execution Latency" value={ms(perf.data?.summary?.avg_execution_latency_ms)} color="orange" />
        <Metric label="Primary Counterparty" value={perf.data?.summary?.primary_counterparty ?? '--'} color="blue" />
      </section>
      <section className="panel-low" style={{ flex: 1, overflow: 'auto' }}>
        <table className="table">
          <thead><tr><th>#</th><th>Dealer</th><th className="right">RFQs</th><th className="right">Quotes</th><th className="right">Win Rate</th><th className="right">Avg Spread</th><th className="right">Latency</th></tr></thead>
          <tbody>
            {rows.map((d: AnyRecord, i: number) => (
              <tr key={d.dealer}>
                <td className="mono">{String(i + 1).padStart(2, '0')}</td>
                <td className="mono" style={{ fontWeight: 800 }}>{d.dealer} {i === 0 ? <span className="badge">TOP RANK</span> : null}</td>
                <td className="right mono">{d.rfqs}</td>
                <td className="right mono">{d.quotes}</td>
                <td className="right mono">{d.win_rate}%</td>
                <td className="right mono">{bps(d.avg_spread_bps)}</td>
                <td className="right mono" style={{ color: Number(d.avg_latency_ms) > 1000 ? 'var(--red)' : 'var(--green)' }}>{ms(d.avg_latency_ms)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <div className="grid-12" style={{ height: 220 }}>
        <section className="panel" style={{ gridColumn: 'span 8', padding: 18 }}>
          <div className="label">RFQ Win Rate vs Quote Latency</div>
          <ResponsiveContainer width="100%" height={160}>
            <BarChart data={rows}>
              <XAxis dataKey="dealer" stroke="#667280" />
              <YAxis stroke="#667280" />
              <Tooltip contentStyle={{ background: '#11161d', border: '1px solid #2a323c' }} />
              <Bar dataKey="win_rate" fill="#7895b2" />
              <Bar dataKey="avg_latency_ms" fill="#c49a61" />
            </BarChart>
          </ResponsiveContainer>
        </section>
        <section className="panel" style={{ gridColumn: 'span 4', padding: 18 }}>
          <div className="label">Market Health Alerts</div>
          <p className="badge red">High latency detected: MS / BARC</p>
          <p className="badge">Quote coverage optimal</p>
          <p className="badge green">Feed updated 45ms ago</p>
        </section>
      </div>
    </>
  );
}

function Dashboard() {
  const dash = useQuery({ queryKey: ['dashboard'], queryFn: api.dashboard, refetchInterval: 4000 });
  const market = useQuery({ queryKey: ['market-context'], queryFn: api.marketContext });
  const metrics = dash.data?.metrics ?? {};
  const curve = market.data?.yield_curve?.map((p: AnyRecord) => ({ tenor: p.tenor, y: Number(p.rate) })) ?? [];
  const rates = Object.fromEntries((market.data?.reference_rates ?? []).map((r: AnyRecord) => [r.rate_type, r.value]));
  return (
    <>
      <section className="metric-grid">
        <Metric label="Active RFQs" value={metrics.active_rfqs ?? 0} color="blue" />
        <Metric label="Avg Slippage" value={bps(metrics.avg_slippage_bps ?? 0)} />
        <Metric label="Dealer Hit Rate" value={`${metrics.dealer_hit_rate ?? 0}%`} color="orange" />
        <Metric label="Trades Today" value={metrics.trades_today ?? 0} />
      </section>
      <div className="grid-12" style={{ flex: 1 }}>
        <section className="panel-low" style={{ gridColumn: 'span 8', overflow: 'auto' }}>
          <div className="panel-header"><span className="label"><Activity size={14} /> Active RFQs</span><span className="mono" style={{ color: 'var(--blue-soft)' }}>Streaming {dash.data?.active_rfqs?.length ?? 0} Objects</span></div>
          <table className="table">
            <thead><tr><th>RFQ ID</th><th>Security</th><th>Side</th><th className="right">Size</th><th>Status</th><th className="right">Best Px</th></tr></thead>
            <tbody>{dash.data?.active_rfqs?.map((r: AnyRecord) => (
              <tr key={r.id}>
                <td><Link className="mono" style={{ color: 'var(--blue-soft)' }} to={`/rfq/${r.id}`}>{shortId(r.id)}</Link></td>
                <td className="mono">{r.security}</td>
                <td><span className={`badge ${r.side === 'BUY' ? '' : 'red'}`}>{r.side}</span></td>
                <td className="right mono">{money(r.quantity, true)}</td>
                <td><span className="badge orange">{r.status}</span></td>
                <td className="right mono">{px(r.best_price)}</td>
              </tr>
            ))}</tbody>
          </table>
        </section>
        <section className="panel" style={{ gridColumn: 'span 4', padding: 18 }}>
          <div className="label">Yield Curve (UST)</div>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={curve.length ? curve : [{ tenor: '2Y', y: 4.12 }, { tenor: '5Y', y: 4.09 }, { tenor: '10Y', y: 4.18 }, { tenor: '30Y', y: 4.51 }]}>
              <CartesianGrid stroke="#2a323c" />
              <XAxis dataKey="tenor" stroke="#667280" />
              <YAxis stroke="#667280" domain={[3.8, 4.7]} />
              <Line type="monotone" dataKey="y" stroke="#7895b2" strokeWidth={2} dot />
            </LineChart>
          </ResponsiveContainer>
          <div className="footer-tape" style={{ marginTop: 10 }}>
            <span>SOFR {pct(rates.SOFR, 2)}</span>
            <span>EFFR {pct(rates.EFFR, 2)}</span>
            <span>REGIME {market.data?.rates_regime ?? 'Stable'}</span>
          </div>
        </section>
        <section className="panel-low" style={{ gridColumn: 'span 12', overflow: 'auto' }}>
          <div className="panel-header"><span className="label">Recent Trades</span></div>
          <table className="table">
            <tbody>{dash.data?.recent_trades?.map((t: AnyRecord) => (
              <tr key={t.id}>
                <td className="mono">{localTime(t.executed_at)}</td>
                <td className="mono">{t.security}</td>
                <td className="mono">{t.dealer}</td>
                <td><span className="badge">{t.side}</span></td>
                <td className="right mono">{money(t.quantity, true)}</td>
                <td className="right mono">{px(t.execution_price)}</td>
                <td className="right mono">{bps(t.slippage_bps)}</td>
              </tr>
            ))}</tbody>
          </table>
        </section>
      </div>
      <StatusTape />
    </>
  );
}

function Trades() {
  const trades = useQuery({ queryKey: ['trades'], queryFn: api.trades });
  return (
    <section className="panel-low" style={{ flex: 1, overflow: 'auto' }}>
      <table className="table">
        <thead><tr><th>Trade</th><th>RFQ</th><th>Security</th><th>Dealer</th><th>Side</th><th className="right">Size</th><th className="right">Price</th><th className="right">Slippage</th></tr></thead>
        <tbody>{trades.data?.map(t => (
          <tr key={t.id}>
            <td><Link to={`/trades/${t.id}`} className="mono" style={{ color: 'var(--blue-soft)' }}>{shortId(t.id)}</Link></td>
            <td className="mono">{shortId(t.rfq_id)}</td>
            <td className="mono">{t.ticker}</td>
            <td className="mono">{t.dealer}</td>
            <td><span className="badge">{t.side}</span></td>
            <td className="right mono">{money(t.quantity, true)}</td>
            <td className="right mono">{px(t.execution_price)}</td>
            <td className="right mono">{bps(t.slippage_bps)}</td>
          </tr>
        ))}</tbody>
      </table>
    </section>
  );
}

function Docs() {
  return (
    <section className="panel" style={{ padding: 28, flex: 1 }}>
      <h1 className="mono">BondWorks Lite Docs</h1>
      <p>See the repository README and docs folder for architecture, API examples, data model, and demo script.</p>
      <p className="badge green">Spring Boot // Postgres // Redis // Redpanda // WebSocket // React // Python SDK</p>
    </section>
  );
}

function Metric({ label, value, color }: { label: string; value: any; color?: 'blue' | 'green' | 'orange' }) {
  return (
    <div className="metric">
      <span className="label">{label}</span>
      <span className={`metric-value ${color ?? ''}`}>{value}</span>
    </div>
  );
}

function Field({ label, value, large, accent }: { label: string; value: any; large?: boolean; accent?: string }) {
  return (
    <div>
      <span className="label">{label}</span>
      <div className="mono" style={{ fontSize: large ? 24 : 15, color: accent ?? 'var(--text)', marginTop: 8 }}>{value ?? '--'}</div>
    </div>
  );
}

function Quality({ label, value, green }: { label: string; value: any; green?: boolean }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', padding: '14px 0' }}>
      <span style={{ color: 'var(--muted)' }}>{label}</span>
      <span className="mono" style={{ color: green ? 'var(--green)' : 'var(--text)', fontSize: 16 }}>{value}</span>
    </div>
  );
}

function RationaleModal({ quote, best, onClose, onSubmit }: { quote: AnyRecord; best?: AnyRecord; onClose: () => void; onSubmit: (body: AnyRecord) => void }) {
  const [reason, setReason] = useState('FASTER_RESPONSE');
  const [text, setText] = useState('');
  return (
    <div className="modal-backdrop">
      <section className="modal">
        <div className="panel-header"><span className="label">Best-Execution Rationale Required</span></div>
        <div style={{ padding: 22 }}>
          <p>You selected <strong>{quote.dealer}</strong>, but <strong>{best?.dealer}</strong> has the best price.</p>
          <div style={{ display: 'grid', gap: 8, margin: '18px 0' }}>
            {rationaleReasons.map(([code, label]) => (
              <label key={code} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input type="radio" checked={reason === code} onChange={() => setReason(code)} /> {label}
              </label>
            ))}
          </div>
          <textarea className="input" style={{ height: 90, paddingTop: 12 }} placeholder="Optional details..." value={text} onChange={e => setText(e.target.value)} />
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 18 }}>
            <button className="action" onClick={onClose}>CANCEL</button>
            <button className="action primary" onClick={() => onSubmit({ reason_code: reason, reason_text: text })}>SUBMIT + EXECUTE</button>
          </div>
        </div>
      </section>
    </div>
  );
}

function StatusTape({ left = 'SYSTEM: STABLE' }: { left?: string }) {
  return (
    <div className="footer-tape">
      <span><span style={{ color: 'var(--green)' }}>●</span> {left}</span>
      <span>US10Y: 4.182 (-0.02) &nbsp; US02Y: 4.451 (+0.01) &nbsp; SERVER: NY-DC3-A2 &nbsp; V2.4.1-LITE</span>
    </div>
  );
}

function Loading() {
  return <section className="panel" style={{ padding: 30 }}><span className="badge">Loading...</span></section>;
}

function useCountdown(expiresAt: string | undefined) {
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    const id = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, [expiresAt]);
  if (!expiresAt) return '--';
  const seconds = Math.max(0, Math.floor((new Date(expiresAt).getTime() - now) / 1000));
  return `00:${String(seconds).padStart(2, '0')}`;
}

function upsertQuote(quotes: AnyRecord[], event: AnyRecord) {
  const id = event.quote_id ?? event.id;
  const normalized = { ...event, id, yield_value: event.yield_value ?? event.yield };
  const existing = quotes.some(q => (q.id ?? q.quote_id) === id);
  return existing ? quotes.map(q => (q.id ?? q.quote_id) === id ? { ...q, ...normalized } : q) : [...quotes, normalized];
}

function rankQuotes(quotes: AnyRecord[], side = 'BUY'): AnyRecord[] {
  return [...quotes].sort((a, b) => side === 'SELL' ? Number(b.price) - Number(a.price) : Number(a.price) - Number(b.price));
}

function quoteRows(dealers: AnyRecord[], quotes: AnyRecord[], best: AnyRecord | undefined): AnyRecord[] {
  return dealers.map(dealer => {
    const quote = quotes.find(q => q.dealer_id === dealer.id || q.dealer === dealer.code);
    if (!quote) return { dealer: dealer.code, status: 'WAITING' };
    return { ...quote, dealer: quote.dealer ?? dealer.code, status: quote.status ?? 'ACTIVE', best: best && (quote.id ?? quote.quote_id) === (best.id ?? best.quote_id) };
  });
}

function quoteDispersion(quotes: AnyRecord[], mid: unknown) {
  if (quotes.length < 2 || !mid) return null;
  const prices = quotes.map(q => Number(q.price));
  return Math.abs(Math.max(...prices) - Math.min(...prices)) / Number(mid) * 10000;
}

function avg(values: number[]) {
  const valid = values.filter(Number.isFinite);
  return valid.length ? valid.reduce((a, b) => a + b, 0) / valid.length : 0;
}

function statusTone(status: unknown) {
  const value = String(status);
  if (['OPEN', 'QUOTING', 'EXECUTED', 'ACTIVE'].includes(value)) return 'green';
  if (['EXPIRED', 'CANCELLED', 'FAILED', 'INACTIVE'].includes(value)) return 'red';
  if (['DRAFT', 'WAITING'].includes(value)) return 'orange';
  return '';
}

function shortId(id: unknown) {
  const text = String(id ?? '--');
  return text === '--' ? text : text.slice(0, 8).toUpperCase();
}

export default App;
