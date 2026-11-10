// Live verification harness: runs the same BRD rule, security, concurrency and
// latency checks against both ELMS implementations and writes JSON evidence.
// Usage: node verify.mjs  (both apps must be running)
import { writeFileSync } from 'node:fs';

const RUN = Date.now().toString(36);
const Y = 2026;

async function call(base, method, path, token, body) {
  const t0 = performance.now();
  const res = await fetch(base + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const ms = performance.now() - t0;
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  return { status: res.status, data, ms };
}

// ---- Adapters: same operations, each app's own API shape ----
const cursor = {
  name: 'Cursor', base: 'http://localhost:8081',
  admin: { email: 'hr@elms.local', password: 'Password123!' },
  annual: 1, sick: 2,
  async login(e, p) { const r = await call(this.base, 'POST', '/api/auth/login', null, { email: e, password: p }); return { ...r, token: r.data?.token, id: r.data?.userId }; },
  createUser(t, u) { return call(this.base, 'POST', '/api/users', t, { email: u.email, password: u.password, firstName: u.first, lastName: 'Test', role: u.role, managerId: u.managerId ?? null, department: 'QA', jobTitle: u.role }); },
  setManager(t, id, u, managerId) { return call(this.base, 'PUT', `/api/users/${id}`, t, { firstName: u.first, lastName: 'Test', role: u.role, status: 'ACTIVE', managerId, department: 'QA', jobTitle: u.role }); },
  deactivate(t, id, u) { return call(this.base, 'PUT', `/api/users/${id}`, t, { firstName: u.first, lastName: 'Test', role: u.role, status: 'INACTIVE', managerId: u.managerId ?? null }); },
  ensureBalances() { return null; }, // Cursor creates balances on user creation
  submit(t, lt, s, e) { return call(this.base, 'POST', '/api/leave-requests', t, { leaveTypeId: lt, startDate: s, endDate: e, reason: 'verify ' + RUN }); },
  approve(t, id) { return call(this.base, 'POST', `/api/leave-requests/${id}/approve`, t, { comment: 'ok' }); },
  reject(t, id) { return call(this.base, 'POST', `/api/leave-requests/${id}/reject`, t, { comment: 'no' }); },
  cancel(t, id) { return call(this.base, 'POST', `/api/leave-requests/${id}/cancel`, t); },
  async balance(t, lt) { const r = await call(this.base, 'GET', '/api/leave-balances/me', t); const b = (r.data || []).find(x => x.leaveTypeId === lt); return b && { available: +b.available, used: +b.used, pending: +b.pending }; },
  adjust(t, empId, lt, reason) { return call(this.base, 'POST', '/api/leave-balances/adjust', t, { employeeId: empId, leaveTypeId: lt, year: Y, entitled: 20, adjustment: 1, reason }); },
  days(r) { return r.data?.durationDays; },
  requestById(t, id) { return call(this.base, 'GET', `/api/leave-requests/${id}`, t); },
  otherBalances(t, empId) { return call(this.base, 'GET', `/api/leave-balances/employee/${empId}`, t); },
  me(t) { return call(this.base, 'GET', '/api/auth/me', t); },
  allRequests(t) { return call(this.base, 'GET', '/api/leave-requests', t); },
  myRequests(t) { return call(this.base, 'GET', '/api/leave-requests/me', t); },
  myBalances(t) { return call(this.base, 'GET', '/api/leave-balances/me', t); },
  policies(t) { return call(this.base, 'GET', '/api/leave-policies', t); },
  audit(t) { return call(this.base, 'GET', '/api/reports/audit-logs', t); },
  employees(t) { return call(this.base, 'GET', '/api/users', t); },
};

const ibm = {
  name: 'IBM Bob', base: 'http://localhost:8082',
  admin: { email: 'hr@elms.com', password: 'Admin@123' },
  annual: 1, sick: 2,
  async login(e, p) { const r = await call(this.base, 'POST', '/api/auth/login', null, { email: e, password: p }); return { ...r, token: r.data?.token, id: r.data?.userId }; },
  createUser(t, u) { return call(this.base, 'POST', '/api/hr/employees', t, { email: u.email, password: u.password, firstName: u.first, lastName: 'Test', role: u.role, managerId: u.managerId ?? null, department: 'QA', jobTitle: u.role }); },
  setManager(t, id, u, managerId) { return call(this.base, 'PUT', `/api/hr/employees/${id}`, t, { managerId }); },
  deactivate(t, id) { return call(this.base, 'PATCH', `/api/hr/employees/${id}/deactivate`, t); },
  ensureBalances(t) { return call(this.base, 'POST', `/api/hr/balances/bulk-assign?year=${Y}`, t); },
  submit(t, lt, s, e) { return call(this.base, 'POST', '/api/leave-requests', t, { leaveTypeId: lt, startDate: s, endDate: e, reason: 'verify ' + RUN }); },
  approve(t, id) { return call(this.base, 'PATCH', `/api/manager/requests/${id}/review`, t, { action: 'APPROVE', comment: 'ok' }); },
  reject(t, id) { return call(this.base, 'PATCH', `/api/manager/requests/${id}/review`, t, { action: 'REJECT', comment: 'no' }); },
  cancel(t, id) { return call(this.base, 'PATCH', `/api/leave-requests/${id}/cancel`, t); },
  async balance(t, lt) { const r = await call(this.base, 'GET', '/api/employee/balances', t); const b = (r.data || []).find(x => x.leaveTypeId === lt); return b && { available: +b.availableDays, used: +b.usedDays, pending: +b.pendingDays }; },
  adjust(t, empId, lt, reason) { return call(this.base, 'POST', '/api/hr/balances/adjust', t, { employeeId: empId, leaveTypeId: lt, year: Y, deltaDays: 1, reason }); },
  days(r) { return r.data?.workingDays; },
  requestById(t, id) { return call(this.base, 'GET', `/api/leave-requests/${id}`, t); },
  otherBalances(t, empId) { return call(this.base, 'GET', `/api/manager/employees/${empId}/balances`, t); },
  me(t) { return call(this.base, 'GET', '/api/auth/me', t); },
  allRequests(t) { return call(this.base, 'GET', '/api/hr/leave-requests', t); },
  myRequests(t) { return call(this.base, 'GET', '/api/leave-requests/my', t); },
  myBalances(t) { return call(this.base, 'GET', '/api/employee/balances', t); },
  policies(t) { return call(this.base, 'GET', '/api/hr/policies', t); },
  audit(t) { return call(this.base, 'GET', '/api/hr/audit-logs', t); },
  employees(t) { return call(this.base, 'GET', '/api/hr/employees', t); },
};

const short = (r) => ({ status: r.status, message: typeof r.data === 'object' && r.data ? (r.data.message ?? (Array.isArray(r.data) ? `array(${r.data.length})` : Object.keys(r.data).slice(0, 6).join(','))) : String(r.data).slice(0, 120) });

async function makeUser(app, hrTok, label, role, managerId) {
  const u = { email: `${label}.${RUN}@verify.test`, password: 'Verify#12345', first: label, role, managerId };
  const c = await app.createUser(hrTok, u);
  if (c.status >= 300) throw new Error(`${app.name} create ${label}: ${c.status} ${JSON.stringify(c.data)}`);
  u.id = c.data.id;
  return u;
}
async function tokenFor(app, u) { const l = await app.login(u.email, u.password); u.token = l.token; return u; }

async function runApp(app) {
  const out = { app: app.name, rules: [], security: [], concurrency: [], perf: [] };
  const rule = (id, title, expected, pass, evidence) => out.rules.push({ id, title, expected, pass, evidence });
  const sec = (id, title, expected, pass, evidence) => out.security.push({ id, title, expected, pass, evidence });

  const hr = await app.login(app.admin.email, app.admin.password);
  const H = hr.token;

  // Fresh actors
  const mgr = await makeUser(app, H, 'mgr', 'MANAGER');
  const mgr2 = await makeUser(app, H, 'mgr2', 'MANAGER');
  const emp = await makeUser(app, H, 'emp', 'EMPLOYEE', mgr.id);
  const emp2 = await makeUser(app, H, 'emp2', 'EMPLOYEE', mgr.id);

  // Finding: does a newly created employee get balances automatically?
  await tokenFor(app, emp2);
  const preBal = await app.myBalances(emp2.token);
  sec('S-01', 'New employee receives leave balances automatically on creation', 'balances present', Array.isArray(preBal.data) && preBal.data.length > 0, { balancesAfterCreate: Array.isArray(preBal.data) ? preBal.data.length : preBal.data });
  const preSubmit = await app.submit(emp2.token, app.annual, '2026-12-28', '2026-12-29');
  sec('S-02', 'New employee can submit leave without a manual HR step', '2xx', preSubmit.status < 300, short(preSubmit));
  if (preSubmit.status < 300) await app.cancel(emp2.token, preSubmit.data.id);

  const bulk = await app.ensureBalances(H);
  out.setup = { bulkAssign: bulk ? bulk.status : 'n/a (automatic)' };
  for (const u of [mgr, mgr2, emp, emp2]) await tokenFor(app, u);

  // RULE-01
  const r01 = await app.submit(null, app.annual, '2026-12-01', '2026-12-01');
  rule('RULE-01', 'Must be authenticated to submit', '401/403', r01.status === 401 || r01.status === 403, short(r01));

  // RULE-02
  const r02 = await app.submit(emp.token, app.annual, '2026-12-10', '2026-12-08');
  rule('RULE-02', 'Start date must be <= end date', '4xx', r02.status >= 400 && r02.status < 500, short(r02));

  // RULE-03
  const r03 = await app.submit(emp.token, app.annual, '2026-11-02', '2026-11-03');
  rule('RULE-03', 'No past dates', '4xx', r03.status >= 400 && r03.status < 500, short(r03));

  // RULE-04 (sick = 10 days; 15 working days requested)
  const r04 = await app.submit(emp.token, app.sick, '2026-11-16', '2026-12-04');
  rule('RULE-04', 'Insufficient balance blocked (15 days vs 10 sick)', '4xx', r04.status >= 400 && r04.status < 500, short(r04));

  // RULE-13 weekend exclusion: Fri 4 Dec -> Mon 7 Dec
  const r13a = await app.submit(emp.token, app.annual, '2026-12-04', '2026-12-07');
  const d13a = app.days(r13a);
  rule('RULE-13a', 'Weekends excluded (Fri 4 Dec - Mon 7 Dec = 2 days)', '2', r13a.status < 300 && Number(d13a) === 2, { ...short(r13a), durationDays: d13a });
  if (r13a.status < 300) await app.cancel(emp.token, r13a.data.id);
  // RULE-13 holiday exclusion: Mon 21 - Fri 25 Dec (25 Dec = Christmas)
  const r13b = await app.submit(emp.token, app.annual, '2026-12-21', '2026-12-25');
  const d13b = app.days(r13b);
  rule('RULE-13b', 'Public holiday excluded (21-25 Dec incl. Christmas = 4 days)', '4', r13b.status < 300 && Number(d13b) === 4, { ...short(r13b), durationDays: d13b });
  if (r13b.status < 300) await app.cancel(emp.token, r13b.data.id);

  // RULE-05 overlap
  const base0 = await app.balance(emp.token, app.annual);
  const A = await app.submit(emp.token, app.annual, '2026-11-17', '2026-11-18');
  const B = await app.submit(emp.token, app.annual, '2026-11-18', '2026-11-19');
  rule('RULE-05', 'Overlapping active requests blocked', '4xx on 2nd', A.status < 300 && B.status >= 400 && B.status < 500, { first: short(A), second: short(B) });

  // RULE-06 employee approving own request
  const r06 = await app.approve(emp.token, A.data?.id);
  rule('RULE-06a', 'Employee cannot approve own request', '4xx', r06.status >= 400 && r06.status < 500, short(r06));
  // manager approving own request (manager has no manager)
  const M1 = await app.submit(mgr.token, app.annual, '2026-11-24', '2026-11-24');
  const r06b = await app.approve(mgr.token, M1.data?.id);
  rule('RULE-06b', 'Manager cannot approve own request', '4xx', M1.status < 300 && r06b.status >= 400 && r06b.status < 500, { submit: short(M1), approve: short(r06b) });
  if (M1.status < 300) await app.cancel(mgr.token, M1.data.id);
  // bypass attempt: HR makes the manager their own manager, then manager self-approves
  const selfMgr = await app.setManager(H, mgr.id, mgr, mgr.id);
  let r06c = { status: 'skipped' }, M2 = { status: 'skipped' };
  if (selfMgr.status < 300) {
    M2 = await app.submit(mgr.token, app.annual, '2026-11-25', '2026-11-25');
    r06c = await app.approve(mgr.token, M2.data?.id);
  }
  rule('RULE-06c', 'Self-approval impossible even if HR sets user as own manager', 'set-self-manager rejected OR approve 4xx',
    selfMgr.status >= 400 || (typeof r06c.status === 'number' && r06c.status >= 400),
    { setSelfAsManager: short(selfMgr), submit: M2.status === 'skipped' ? 'skipped' : short(M2), selfApprove: r06c.status === 'skipped' ? 'skipped' : short(r06c) });
  await app.setManager(H, mgr.id, mgr, null);

  // RULE-07 other manager / HR
  const r07 = await app.approve(mgr2.token, A.data?.id);
  rule('RULE-07a', 'Non-assigned manager cannot decide', '4xx', r07.status >= 400 && r07.status < 500, short(r07));

  // RULE-10 reject leaves balance unchanged
  const rej = await app.reject(mgr.token, A.data?.id);
  const afterRej = await app.balance(emp.token, app.annual);
  rule('RULE-10', 'Rejected request does not affect balance', 'available unchanged', rej.status < 300 && afterRej.available === base0.available && afterRej.used === base0.used,
    { reject: short(rej), before: base0, after: afterRej });

  // RULE-08 only pending can be decided
  const r08 = await app.approve(mgr.token, A.data?.id);
  rule('RULE-08', 'Only PENDING can be approved/rejected (approve a rejected one)', '4xx', r08.status >= 400 && r08.status < 500, short(r08));

  // RULE-09 approval reduces balance
  const C = await app.submit(emp.token, app.annual, '2026-11-23', '2026-11-24');
  const midC = await app.balance(emp.token, app.annual);
  const appr = await app.approve(mgr.token, C.data?.id);
  const afterAppr = await app.balance(emp.token, app.annual);
  rule('RULE-09', 'Approved request reduces available balance (2 days)', 'used +2, available -2', appr.status < 300 && afterAppr.used === base0.used + 2 && afterAppr.available === base0.available - 2,
    { submit: short(C), approve: short(appr), before: base0, whilePending: midC, after: afterAppr });

  // RULE-11 cancelling approved restores
  const canc = await app.cancel(emp.token, C.data?.id);
  const afterCanc = await app.balance(emp.token, app.annual);
  rule('RULE-11', 'Cancelling approved leave restores balance', 'back to baseline', canc.status < 300 && afterCanc.available === base0.available && afterCanc.used === base0.used,
    { cancel: short(canc), after: afterCanc, baseline: base0 });

  // RULE-07b HR deciding a request (BRD: only assigned manager)
  const D = await app.submit(emp.token, app.annual, '2026-11-30', '2026-11-30');
  const r07b = await app.approve(H, D.data?.id);
  rule('RULE-07b', 'HR (not the assigned manager) cannot decide', '4xx', r07b.status >= 400 && r07b.status < 500, short(r07b));
  await app.cancel(emp.token, D.data?.id);

  // RULE-12 adjustment requires reason
  const r12a = await app.adjust(H, emp.id, app.annual, '');
  const r12b = await app.adjust(H, emp.id, app.annual, 'Verification adjustment');
  rule('RULE-12', 'HR adjustment requires a reason', 'blank -> 4xx, with reason -> 2xx', r12a.status >= 400 && r12a.status < 500 && r12b.status < 300, { blank: short(r12a), withReason: short(r12b) });

  // ---- Security / robustness ----
  const idor = await app.requestById(emp2.token, A.data?.id);
  sec('S-03', "Employee cannot read another employee's leave request by id", '403/404', idor.status === 403 || idor.status === 404 || idor.status === 405, { ...short(idor), body: idor.status === 200 ? { employeeName: idor.data.employeeName, reason: idor.data.reason } : undefined });
  const xteam = await app.otherBalances(mgr2.token, emp.id);
  sec('S-04', "Manager cannot view balances of another manager's employee", '403', xteam.status === 403, short(xteam));
  const empHr = await app.allRequests(emp.token);
  sec('S-05', 'Employee blocked from HR-only endpoint (all requests)', '403', empHr.status === 403, short(empHr));
  const pol = await app.policies(H);
  sec('S-06', 'HR can list leave policies', '200', pol.status === 200, short(pol));
  const aud = await app.audit(H);
  sec('S-07', 'HR can list audit logs', '200', aud.status === 200, short(aud));
  // deactivated user: existing token and new login
  const victim = await makeUser(app, H, 'leaver', 'EMPLOYEE', mgr.id);
  await tokenFor(app, victim);
  await app.deactivate(H, victim.id, victim);
  const oldTok = await app.me(victim.token);
  const relog = await app.login(victim.email, victim.password);
  sec('S-08', 'Deactivated user: existing token rejected', '401/403', oldTok.status === 401 || oldTok.status === 403, short(oldTok));
  sec('S-09', 'Deactivated user: login rejected with a 4xx (not 5xx)', '401/403', relog.status === 401 || relog.status === 403, short(relog));

  // ---- Concurrency (RULE-14 / RULE-05 under race) ----
  const c1 = await makeUser(app, H, 'race1', 'EMPLOYEE', mgr.id);
  const c2 = await makeUser(app, H, 'race2', 'EMPLOYEE', mgr.id);
  await app.ensureBalances(H);
  await tokenFor(app, c1); await tokenFor(app, c2);
  const same = await Promise.all(Array.from({ length: 8 }, () => app.submit(c1.token, app.annual, '2026-12-14', '2026-12-15')));
  const okSame = same.filter(r => r.status < 300).length;
  out.concurrency.push({ id: 'C-01', title: '8 identical overlapping submits in parallel (expect exactly 1 accepted)', pass: okSame === 1, accepted: okSame, statuses: same.map(r => r.status), messages: [...new Set(same.filter(r => r.status >= 300).map(r => short(r).message))] });
  const weeks = [['2026-11-16', '2026-11-17'], ['2026-11-19', '2026-11-20'], ['2026-11-23', '2026-11-24'], ['2026-11-26', '2026-11-27'], ['2026-11-30', '2026-12-01'], ['2026-12-03', '2026-12-04'], ['2026-12-08', '2026-12-09'], ['2026-12-10', '2026-12-11']];
  const over = await Promise.all(weeks.map(([s, e]) => app.submit(c2.token, app.sick, s, e)));
  const okOver = over.filter(r => r.status < 300).length;
  const finalSick = await app.balance(c2.token, app.sick);
  out.concurrency.push({ id: 'C-02', title: '8 parallel 2-day sick requests vs 10-day balance (expect <= 5 accepted, balance never negative)', pass: okOver <= 5 && finalSick.available >= 0, accepted: okOver, finalBalance: finalSick, statuses: over.map(r => r.status), messages: [...new Set(over.filter(r => r.status >= 300).map(r => short(r).message))] });

  // ---- Latency ----
  async function bench(label, fn, n = 40) {
    const ms = [];
    let errors = 0;
    for (let i = 0; i < n; i++) { const r = await fn(); if (r.status >= 300) errors++; ms.push(r.ms); }
    ms.sort((a, b) => a - b);
    const q = (p) => +ms[Math.min(ms.length - 1, Math.floor(p * ms.length))].toFixed(1);
    out.perf.push({ label, n, p50: q(0.5), p95: q(0.95), max: +ms[ms.length - 1].toFixed(1), errors });
  }
  async function burst(label, fn, total = 200, conc = 20) {
    let i = 0, errors = 0; const t0 = performance.now();
    await Promise.all(Array.from({ length: conc }, async () => { while (i++ < total) { const r = await fn(); if (r.status >= 300) errors++; } }));
    const s = (performance.now() - t0) / 1000;
    out.perf.push({ label, n: total, concurrency: conc, seconds: +s.toFixed(2), rps: +(total / s).toFixed(1), errors });
  }
  for (let i = 0; i < 5; i++) await app.myBalances(emp.token); // warm-up
  await bench('POST login (bcrypt)', () => app.login(emp.email, emp.password), 20);
  await bench('GET my balances', () => app.myBalances(emp.token));
  await bench('GET my requests', () => app.myRequests(emp.token));
  await bench('GET all requests (HR)', () => app.allRequests(H));
  await bench('GET employees (HR)', () => app.employees(H));
  await burst('GET all requests x200 @20 parallel', () => app.allRequests(H));
  const allNow = await app.allRequests(H);
  out.dataVolume = { requestsInDb: Array.isArray(allNow.data) ? allNow.data.length : null };
  return out;
}

const results = { runId: RUN, at: new Date().toISOString(), apps: [] };
for (const app of [cursor, ibm]) {
  try { results.apps.push(await runApp(app)); }
  catch (e) { results.apps.push({ app: app.name, fatal: String(e.stack || e) }); }
}
const file = new URL('./results.json', import.meta.url);
writeFileSync(file, JSON.stringify(results, null, 2));
for (const a of results.apps) {
  console.log(`\n=== ${a.app}`);
  if (a.fatal) { console.log('FATAL', a.fatal); continue; }
  for (const r of [...a.rules, ...a.security]) console.log(`${r.pass ? 'PASS' : 'FAIL'}  ${r.id}  ${r.title}  ${JSON.stringify(r.evidence)}`);
  for (const c of a.concurrency) console.log(`${c.pass ? 'PASS' : 'FAIL'}  ${c.id}  accepted=${c.accepted} ${JSON.stringify(c.statuses)} ${JSON.stringify(c.messages)} ${c.finalBalance ? JSON.stringify(c.finalBalance) : ''}`);
  for (const p of a.perf) console.log('PERF', JSON.stringify(p));
  console.log('DATA', JSON.stringify(a.dataVolume), JSON.stringify(a.setup));
}
