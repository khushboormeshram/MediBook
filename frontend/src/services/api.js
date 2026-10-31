const BASE = import.meta.env.VITE_API_URL || '/api'
export function token(){return localStorage.getItem('medibook_token')}
export async function api(path, options={}){
  const headers={'Content-Type':'application/json',...(token()?{Authorization:`Bearer ${token()}`}:{ }),...options.headers}
  const res=await fetch(`${BASE}${path}`,{...options,headers,body:options.body?JSON.stringify(options.body):undefined})
  if(!res.ok){let message=`Request failed (${res.status})`;try{const data=await res.json();message=data.message||data.error||message}catch{}throw new Error(message)}
  if(res.status===204)return null
  return res.json()
}
