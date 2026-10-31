import {Link} from 'react-router-dom'
import {ArrowRight,HeartPulse,LayoutDashboard,Menu,X} from 'lucide-react'
import {Footer} from '../components/Shared.jsx'

export default function AppLayout({user,menu,onMenu,onLogin,onRegister,onSignOut,children}){
 return <div className="app-shell">
  <header className="topbar"><div className="nav-wrap"><Link to="/" className="brand"><span className="brand-mark"><HeartPulse size={20}/></span><span>Medi<span>Book</span></span></Link><nav className={menu?'nav-links open':'nav-links'}><Link to="/doctors" onClick={()=>onMenu(false)}>Find a doctor</Link><a href="/#specialties" onClick={()=>onMenu(false)}>Specialties</a><a href="/#how-it-works" onClick={()=>onMenu(false)}>How it works</a></nav><div className="nav-actions">{user?<><Link className="nav-dashboard" to="/dashboard"><LayoutDashboard size={16}/><span>{user.role==='PATIENT'?'My appointments':'Dashboard'}</span></Link><button className="avatar-button" title="Sign out" onClick={onSignOut}>{user.fullName?.split(' ').map(x=>x[0]).join('').slice(0,2)}</button></>:<><button className="btn btn-plain" onClick={onLogin}>Log in</button><button className="btn btn-primary nav-cta" onClick={onRegister}>Create account <ArrowRight size={15}/></button></>}<button className="mobile-menu" onClick={()=>onMenu(!menu)} aria-label="Toggle navigation">{menu?<X/>:<Menu/>}</button></div></div></header>
  {children}<Footer/>
 </div>
}
