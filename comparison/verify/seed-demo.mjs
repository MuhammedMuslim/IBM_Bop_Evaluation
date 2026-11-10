// Seeds comparable demo data in both apps so every page has content to screenshot.
// Cursor: uses its seeded alice/bob/manager. IBM Bob: has no seeded employee/manager,
// so demo accounts are created through its own HR API.
const j = async (base, m, p, t, b) => {
  const r = await fetch(base + p, { method: m, headers: { 'Content-Type': 'application/json', ...(t ? { Authorization: 'Bearer ' + t } : {}) }, body: b ? JSON.stringify(b) : undefined });
  const x = await r.text(); let d; try { d = JSON.parse(x); } catch { d = x; }
  return { s: r.status, d };
};

// ---------- Cursor ----------
{
  const B = 'http://localhost:8081/api';
  const tok = async (e) => (await j(B, 'POST', '/auth/login', null, { email: e, password: 'Password123!' })).d.token;
  const A = await tok('alice@elms.local'), Bo = await tok('bob@elms.local'), M = await tok('manager@elms.local');
  const p1 = await j(B, 'POST', '/leave-requests', A, { leaveTypeId: 1, startDate: '2026-12-01', endDate: '2026-12-02', reason: 'Dentist and errands' });
  const p2 = await j(B, 'POST', '/leave-requests', Bo, { leaveTypeId: 1, startDate: '2026-12-07', endDate: '2026-12-08', reason: 'Moving house' });
  const p3 = await j(B, 'POST', '/leave-requests', A, { leaveTypeId: 2, startDate: '2026-12-16', endDate: '2026-12-16', reason: 'Medical check-up' });
  const rj = p3.d?.id ? await j(B, 'POST', `/leave-requests/${p3.d.id}/reject`, M, { comment: 'Release week - please move' }) : null;
  console.log('Cursor seed:', p1.s, p2.s, p3.s, rj?.s);
}

// ---------- IBM Bob ----------
{
  const B = 'http://localhost:8082/api';
  const H = (await j(B, 'POST', '/auth/login', null, { email: 'hr@elms.com', password: 'Admin@123' })).d.token;
  const mk = async (email, first, last, role, managerId) => {
    const r = await j(B, 'POST', '/hr/employees', H, { email, password: 'Demo@12345', firstName: first, lastName: last, role, managerId, department: 'Engineering', jobTitle: role === 'MANAGER' ? 'Engineering Manager' : 'Software Engineer' });
    if (r.s >= 300) { const all = (await j(B, 'GET', '/hr/employees', H)).d; return all.find((u) => u.email === email); }
    return r.d;
  };
  const mgr = await mk('demo.manager@elms.com', 'Daniel', 'Brooks', 'MANAGER');
  const e1 = await mk('demo.employee@elms.com', 'Emma', 'Carter', 'EMPLOYEE', mgr.id);
  const e2 = await mk('demo.employee2@elms.com', 'Liam', 'Hayes', 'EMPLOYEE', mgr.id);
  await j(B, 'POST', '/hr/balances/bulk-assign?year=2026', H);
  const tok = async (e) => (await j(B, 'POST', '/auth/login', null, { email: e, password: 'Demo@12345' })).d.token;
  const M = await tok(mgr.email), E1 = await tok(e1.email), E2 = await tok(e2.email);

  // Decisions fail intermittently in IBM Bob (verified separately); retry until one sticks.
  async function decided(token, lt, dates, action, reason) {
    for (const [s, e] of dates) {
      const r = await j(B, 'POST', '/leave-requests', token, { leaveTypeId: lt, startDate: s, endDate: e, reason });
      if (!r.d?.id) continue;
      const rv = await j(B, 'PATCH', `/manager/requests/${r.d.id}/review`, M, { action, comment: action === 'APPROVE' ? 'Enjoy' : 'Release week - please move' });
      if (rv.s === 200) return { ok: true, tries: dates.indexOf(dates.find((d) => d[0] === s)) + 1 };
      await j(B, 'PATCH', `/leave-requests/${r.d.id}/cancel`, token);
    }
    return { ok: false };
  }
  const ap = await decided(E1, 1, [['2026-11-23', '2026-11-25'], ['2026-11-26', '2026-11-27'], ['2026-12-28', '2026-12-29'], ['2027-01-04', '2027-01-05']], 'APPROVE', 'Family trip');
  const rj = await decided(E1, 2, [['2026-12-16', '2026-12-16'], ['2026-12-17', '2026-12-17'], ['2026-12-18', '2026-12-18'], ['2026-12-30', '2026-12-30']], 'REJECT', 'Medical check-up');
  const p1 = await j(B, 'POST', '/leave-requests', E1, { leaveTypeId: 1, startDate: '2026-12-01', endDate: '2026-12-02', reason: 'Dentist and errands' });
  const p2 = await j(B, 'POST', '/leave-requests', E2, { leaveTypeId: 1, startDate: '2026-12-07', endDate: '2026-12-08', reason: 'Moving house' });
  console.log('IBM seed:', JSON.stringify({ mgr: mgr.id, e1: e1.id, e2: e2.id, approved: ap, rejected: rj, pending: [p1.s, p2.s] }));
}
