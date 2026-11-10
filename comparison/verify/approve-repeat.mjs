// Repeats submit -> approve/reject on IBM Bob to measure how often decisions fail,
// then checks the database-visible outcome through the API.
const B = 'http://localhost:8082/api';
const RUN = Date.now().toString(36);
async function call(m, p, t, b) {
  const r = await fetch(B + p, { method: m, headers: { 'Content-Type': 'application/json', ...(t ? { Authorization: 'Bearer ' + t } : {}) }, body: b ? JSON.stringify(b) : undefined });
  const x = await r.text(); let d; try { d = JSON.parse(x); } catch { d = x; }
  return { s: r.status, d };
}
const login = async (e, p) => (await call('POST', '/auth/login', null, { email: e, password: p })).d;
const H = (await login('hr@elms.com', 'Admin@123')).token;
const mk = async (n, role, managerId) => { const email = `${n}.${RUN}@verify.test`; const r = await call('POST', '/hr/employees', H, { email, password: 'Verify#12345', firstName: n, lastName: 'Rep', role, managerId }); return { id: r.d.id, email }; };
const m = await mk('repmgr', 'MANAGER');
const e = await mk('repemp', 'EMPLOYEE', m.id);
await call('POST', '/hr/balances/bulk-assign?year=2026', H);
const M = (await login(m.email, 'Verify#12345')).token;
const E = (await login(e.email, 'Verify#12345')).token;
const days = ['2026-11-16', '2026-11-17', '2026-11-18', '2026-11-19', '2026-11-20', '2026-11-23', '2026-11-24', '2026-11-25', '2026-11-26', '2026-11-27'];
const tally = { APPROVE: {}, REJECT: {} };
for (let i = 0; i < days.length; i++) {
  const action = i % 2 ? 'REJECT' : 'APPROVE';
  const s = await call("POST", "/leave-requests", E, { leaveTypeId: 2, startDate: days[i], endDate: days[i] });
  tally.SUBMIT = tally.SUBMIT || {}; tally.SUBMIT[s.s] = (tally.SUBMIT[s.s] || 0) + 1;
  if (!s.d || !s.d.id) continue;
  const r = await call('PATCH', `/manager/requests/${s.d.id}/review`, M, { action, comment: 'x' });
  tally[action][r.s] = (tally[action][r.s] || 0) + 1;
}
const mine = (await call('GET', '/leave-requests/my', E)).d;
const byStatus = mine.reduce((a, r) => ((a[r.status] = (a[r.status] || 0) + 1), a), {});
const bal = (await call('GET', '/employee/balances', E)).d.find((b) => b.leaveTypeId === 2);
console.log(JSON.stringify({ httpStatusesByAction: tally, persistedStatuses: byStatus, sickBalance: bal }, null, 1));
