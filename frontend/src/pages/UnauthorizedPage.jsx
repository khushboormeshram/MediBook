import {ShieldCheck} from 'lucide-react'
import {Empty} from '../components/Shared.jsx'
export default function UnauthorizedPage({onLogin}){return <main className="page-main"><Empty icon={<ShieldCheck/>} title="Sign in to see your MediBook space." text="Your appointments and account details are only available after you log in." action={<button className="btn btn-primary" onClick={onLogin}>Log in securely</button>}/></main>}
