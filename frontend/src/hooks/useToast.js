import {useEffect,useRef,useState} from 'react'
export default function useToast(){const [message,setMessage]=useState('');const timer=useRef();const notify=value=>{setMessage(value);clearTimeout(timer.current);timer.current=setTimeout(()=>setMessage(''),3500)};useEffect(()=>()=>clearTimeout(timer.current),[]);return [message,notify]}
