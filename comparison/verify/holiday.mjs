const B='http://localhost:8082/api', R=Date.now().toString(36);
const c=async(m,p,t,b)=>{const r=await fetch(B+p,{method:m,headers:{'Content-Type':'application/json',...(t?{Authorization:'Bearer '+t}:{})},body:b?JSON.stringify(b):undefined});return {s:r.status,d:await r.json().catch(()=>null)}};
const H=(await c('POST','/auth/login',null,{email:'hr@elms.com',password:'Admin@123'})).d.token;
const e=(await c('POST','/hr/employees',H,{email:`hol.${R}@verify.test`,password:'Verify#12345',firstName:'hol',lastName:'T',role:'EMPLOYEE'})).d;
await c('POST','/hr/balances/bulk-assign?year=2026',H);
const T=(await c('POST','/auth/login',null,{email:e.email,password:'Verify#12345'})).d.token;
const s=await c('POST','/leave-requests',T,{leaveTypeId:1,startDate:'2026-12-21',endDate:'2026-12-25'});
console.log('status',s.s,'workingDays',s.d&&s.d.workingDays);
if(s.d&&s.d.id) await c('PATCH',`/leave-requests/${s.d.id}/cancel`,T);
