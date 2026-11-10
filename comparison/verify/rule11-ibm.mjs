// IBM: cancel an APPROVED request and confirm used days are restored.
const B='http://localhost:8082/api';
const c=async(m,p,t,b)=>{const r=await fetch(B+p,{method:m,headers:{'Content-Type':'application/json',...(t?{Authorization:'Bearer '+t}:{})},body:b?JSON.stringify(b):undefined});return{s:r.status,d:await r.json().catch(()=>null)}};
const T=(await c('POST','/auth/login',null,{email:'demo.employee@elms.com',password:'Demo@12345'})).d.token;
const bal=async()=>{const b=(await c('GET','/employee/balances',T)).d.find(x=>x.leaveTypeId===1);return {used:+b.usedDays,pending:+b.pendingDays,available:+b.availableDays}};
const appr=(await c('GET','/leave-requests/my',T)).d.find(r=>r.status==='APPROVED');
console.log('approved request',appr.id,appr.startDate,appr.workingDays);
const before=await bal(); const x=await c('PATCH',`/leave-requests/${appr.id}/cancel`,T); const after=await bal();
console.log(JSON.stringify({before,cancelStatus:x.s,newStatus:x.d&&x.d.status,after}));
