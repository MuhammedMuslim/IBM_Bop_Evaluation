// IBM: HR sets a manager as their own manager; manager then tries to approve own leave.
const B='http://localhost:8082/api', R=Date.now().toString(36);
const c=async(m,p,t,b)=>{const r=await fetch(B+p,{method:m,headers:{'Content-Type':'application/json',...(t?{Authorization:'Bearer '+t}:{})},body:b?JSON.stringify(b):undefined});return {s:r.status,d:await r.json().catch(()=>null)}};
const H=(await c('POST','/auth/login',null,{email:'hr@elms.com',password:'Admin@123'})).d.token;
const m=(await c('POST','/hr/employees',H,{email:`self.${R}@verify.test`,password:'Verify#12345',firstName:'Self',lastName:'Approver',role:'MANAGER'})).d;
await c('POST','/hr/balances/bulk-assign?year=2026',H);
const upd=await c('PUT',`/hr/employees/${m.id}`,H,{managerId:m.id});
console.log('set own manager ->',upd.s,'managerId',upd.d&&upd.d.managerId,'id',m.id);
const T=(await c('POST','/auth/login',null,{email:m.email,password:'Verify#12345'})).d.token;
const days=['2026-11-16','2026-11-17','2026-11-18','2026-11-19','2026-11-20','2026-11-23','2026-11-24','2026-11-25','2026-11-26','2026-11-27'];
for(const d of days){const s=await c('POST','/leave-requests',T,{leaveTypeId:1,startDate:d,endDate:d});if(!s.d||!s.d.id)continue;const a=await c('PATCH',`/manager/requests/${s.d.id}/review`,T,{action:'APPROVE',comment:'self'});console.log(d,'approve own ->',a.s,a.d&&a.d.status,a.d&&a.d.reviewedByName);if(a.s===200)break;}
const mine=(await c('GET','/leave-requests/my',T)).d;console.log('persisted:',mine.map(r=>r.status+(r.reviewedByName?' by '+r.reviewedByName:'')).join(', '));
