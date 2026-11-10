const B='http://localhost:8082/api';
const j=async(m,p,t,b)=>{const r=await fetch(B+p,{method:m,headers:{'Content-Type':'application/json',...(t?{Authorization:'Bearer '+t}:{})},body:b?JSON.stringify(b):undefined});const x=await r.text();let d;try{d=JSON.parse(x)}catch{d=x}return{s:r.status,d}};
const tok=async e=>(await j('POST','/auth/login',null,{email:e,password:'Demo@12345'})).d.token;
const M=await tok('demo.manager@elms.com'),E=await tok('demo.employee@elms.com');
const wd=[];for(let d=new Date('2026-12-09');wd.length<30 && d<new Date('2027-01-01');d.setDate(d.getDate()+1)){if(d.getDay()%6)wd.push(d.toISOString().slice(0,10))}
let tries={APPROVE:0,REJECT:0},submitFail=0,done={};
for(const action of ['APPROVE','REJECT']){for(const day of wd.splice(0,8)){tries[action]++;const r=await j('POST','/leave-requests',E,{leaveTypeId:action==='APPROVE'?1:2,startDate:day,endDate:day,reason:action==='APPROVE'?'Family trip':'Medical check-up'});if(!r.d?.id){submitFail++;tries[action]--;continue}const v=await j('PATCH',`/manager/requests/${r.d.id}/review`,M,{action,comment:action==='APPROVE'?'Enjoy':'Release week - please move'});if(v.s===200){done[action]=day;break}await j('PATCH',`/leave-requests/${r.d.id}/cancel`,E)}}
console.log(JSON.stringify({decisionAttempts:tries,submitFail,succeededOn:done}));
