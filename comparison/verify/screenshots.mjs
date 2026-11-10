// Captures every page of both apps per role, plus UI evidence for business rules.
// Writes PNGs to ../screenshots/<app>/ and a manifest.json with what each shot shows.
import { chromium } from 'playwright-core';
import { mkdirSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', 'screenshots');
const CHROME = 'C:/Program Files/Google/Chrome/Application/chrome.exe';

const APPS = {
  cursor: {
    name: 'Cursor', base: 'http://localhost:5173', login: '/login',
    users: {
      employee: ['alice@elms.local', 'Password123!'], employee2: ['bob@elms.local', 'Password123!'],
      manager: ['manager@elms.local', 'Password123!'], hr: ['hr@elms.local', 'Password123!'], admin: ['admin@elms.local', 'Password123!'],
    },
    pages: {
      employee: [['/', 'Employee dashboard'], ['/request-leave', 'Request leave'], ['/history', 'Leave history'], ['/notifications', 'Notifications']],
      manager: [['/', 'Manager dashboard'], ['/approvals', 'Pending approvals'], ['/team', 'Team & schedule'], ['/request-leave', 'Manager own leave'], ['/notifications', 'Manager notifications']],
      hr: [['/', 'HR dashboard / reports'], ['/employees', 'Employees'], ['/leave-types', 'Leave types'], ['/policies', 'Leave policies'], ['/balances', 'Balances & adjustments'], ['/requests', 'All requests'], ['/audit', 'Audit log']],
      admin: [['/', 'System admin dashboard'], ['/employees', 'System admin users']],
    },
    requestPath: '/request-leave', hrOnlyPath: '/employees', approvalsPath: '/approvals', balancePath: '/',
  },
  ibm: {
    name: 'IBM Bob', base: 'http://localhost:3000', login: '/login',
    users: {
      employee: ['demo.employee@elms.com', 'Demo@12345'], employee2: ['demo.employee2@elms.com', 'Demo@12345'],
      manager: ['demo.manager@elms.com', 'Demo@12345'], hr: ['hr@elms.com', 'Admin@123'], admin: ['admin@elms.com', 'Admin@123'],
    },
    pages: {
      employee: [['/employee', 'Employee dashboard'], ['/employee/request', 'Request leave'], ['/employee/history', 'Leave history']],
      manager: [['/manager', 'Manager dashboard'], ['/manager/pending', 'Pending approvals'], ['/manager/history', 'Team history'], ['/manager/team', 'My team']],
      hr: [['/hr', 'HR dashboard'], ['/hr/employees', 'Employees'], ['/hr/leave-types', 'Leave types'], ['/hr/policies', 'Leave policies'], ['/hr/balances', 'Balances & adjustments'], ['/hr/requests', 'All requests'], ['/hr/audit-logs', 'Audit logs']],
      admin: [['/hr', 'System admin dashboard'], ['/hr/employees', 'System admin users']],
    },
    requestPath: '/employee/request', hrOnlyPath: '/hr/employees', approvalsPath: '/manager/pending', balancePath: '/employee',
  },
};

// Visible success/error banners in either UI.
async function banners(page) {
  return page.evaluate(() => {
    const out = [];
    for (const el of document.querySelectorAll('body *')) {
      const bg = getComputedStyle(el).backgroundColor;
      const cls = el.className && typeof el.className === 'string' ? el.className : '';
      if (/\b(error|success)\b/.test(cls) || bg === 'rgb(254, 226, 226)' || bg === 'rgb(209, 250, 229)') {
        const t = el.innerText.trim();
        if (t && t.length < 300 && !out.includes(t)) out.push(t);
      }
    }
    return out;
  });
}

async function settle(page) {
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForTimeout(600);
}

async function run(key) {
  const app = APPS[key];
  const dir = path.join(ROOT, key);
  mkdirSync(dir, { recursive: true });
  const manifest = [];
  let n = 0;
  const browser = await chromium.launch({ executablePath: CHROME, headless: true });

  async function shot(page, slug, title, extra = {}) {
    n += 1;
    const file = `${String(n).padStart(2, '0')}-${slug}.png`;
    await page.screenshot({ path: path.join(dir, file), fullPage: true });
    const msgs = await banners(page);
    manifest.push({ file, title, url: page.url().replace(app.base, ''), banners: msgs, ...extra });
    console.log(`${app.name} ${file} ${title} ${msgs.length ? JSON.stringify(msgs) : ''} ${extra.note || ''}`);
  }

  async function session(role) {
    const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    const page = await ctx.newPage();
    await page.goto(app.base + app.login);
    await settle(page);
    const [email, pw] = app.users[role];
    await page.fill('input[type=email]', email);
    await page.fill('input[type=password]', pw);
    await page.click('button[type=submit]');
    await page.waitForURL((u) => !u.pathname.startsWith('/login'), { timeout: 15000 });
    await settle(page);
    return { ctx, page };
  }

  // Login page + invalid credentials
  {
    const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    const page = await ctx.newPage();
    await page.goto(app.base + app.login); await settle(page);
    await shot(page, 'login', 'Login page');
    await page.fill('input[type=email]', app.users.employee[0]);
    await page.fill('input[type=password]', 'wrong-password');
    await page.click('button[type=submit]'); await settle(page);
    await shot(page, 'login-invalid', 'RULE-01: invalid credentials rejected', { rule: 'RULE-01' });
    await page.goto(app.base + app.hrOnlyPath); await settle(page);
    await shot(page, 'unauthenticated-redirect', 'RULE-01: protected page without login redirects', { rule: 'RULE-01' });
    await ctx.close();
  }

  // Every page per role
  for (const role of ['employee', 'manager', 'hr', 'admin']) {
    const { ctx, page } = await session(role);
    for (const [p, title] of app.pages[role]) {
      await page.goto(app.base + p); await settle(page);
      await shot(page, `${role}-${p.replace(/\//g, '-').replace(/^-+|-+$/g, '') || 'home'}`, `${role.toUpperCase()} — ${title}`, { role });
    }
    await ctx.close();
  }

  // Rule evidence through the UI (employee)
  {
    const { ctx, page } = await session('employee');
    async function attempt(slug, title, rule, type, start, end) {
      await page.goto(app.base + app.requestPath); await settle(page);
      await page.selectOption('form select', { label: type });
      const dates = page.locator('form input[type=date]');
      await dates.nth(0).fill(start);
      await dates.nth(1).fill(end);
      const native = await page.evaluate(() => [...document.querySelectorAll('form input[type=date]')].map((i) => i.validationMessage).filter(Boolean));
      await page.click('form button[type=submit]'); await settle(page);
      await shot(page, slug, title, { rule, attempted: { type, start, end }, browserValidation: native, note: native.length ? `browser blocked: ${native.join(' | ')}` : '' });
    }
    await attempt('rule02-end-before-start', 'RULE-02: end date before start date', 'RULE-02', 'Annual Leave', '2026-12-10', '2026-12-08');
    await attempt('rule03-past-date', 'RULE-03: dates in the past', 'RULE-03', 'Annual Leave', '2026-11-02', '2026-11-03');
    // Ranges chosen to avoid each demo employee's existing active leave, so the balance rule is what fires.
    const [s4, e4] = key === 'cursor' ? ['2026-12-03', '2026-12-23'] : ['2026-11-16', '2026-11-30'];
    await attempt('rule04-insufficient', 'RULE-04: more sick days requested than the 10 available', 'RULE-04', 'Sick Leave', s4, e4);
    await attempt('rule05-overlap', 'RULE-05: overlaps the pending 1–2 Dec request', 'RULE-05', 'Annual Leave', '2026-12-02', '2026-12-03');
    await page.goto(app.base + app.hrOnlyPath); await settle(page);
    await shot(page, 'rbac-employee-on-hr-page', 'RBAC: employee opens the HR employees page', { rule: 'RBAC' });
    await ctx.close();
  }

  // Manager approves the second employee's pending request in the UI
  {
    const { ctx, page } = await session('manager');
    await page.goto(app.base + app.approvalsPath); await settle(page);
    // Cursor's table has no reason column, so match the row by employee name.
    const row = page.locator('tr', { hasText: key === 'cursor' ? 'Bob Patel' : 'Liam Hayes' }).first();
    if (key === 'cursor') {
      await row.locator('input[placeholder=Comment]').fill('Approved - good luck with the move');
      await row.getByRole('button', { name: 'Approve' }).click();
    } else {
      await row.getByRole('button', { name: 'Approve' }).click();
      await page.fill('textarea[placeholder="Add a comment..."]', 'Approved - good luck with the move');
      await page.getByRole('button', { name: 'Confirm Approval' }).click();
    }
    await settle(page);
    await shot(page, 'rule09-manager-approves', 'RULE-02/08/09: manager approves a pending request in the UI', { rule: 'RULE-09' });
    await ctx.close();
    const s2 = await session('employee2');
    await s2.page.goto(app.base + app.balancePath); await settle(s2.page);
    await shot(s2.page, 'rule09-employee-balance-after', 'RULE-09: employee balance after the approval attempt', { rule: 'RULE-09' });
    await s2.ctx.close();
  }

  await browser.close();
  writeFileSync(path.join(dir, 'manifest.json'), JSON.stringify(manifest, null, 2));
}

for (const k of process.argv.slice(2).length ? process.argv.slice(2) : ['cursor', 'ibm']) {
  try { await run(k); } catch (e) { console.error('FAILED', k, e.message); }
}
