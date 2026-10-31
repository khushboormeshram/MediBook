import {Activity} from 'lucide-react'
import {Empty} from '../components/Shared.jsx'
export default function NotFoundPage({onHome}){return <main className="page-main"><Empty icon={<Activity/>} title="This page is taking a breather." text="Let’s get you back to the care you need." action={<button onClick={onHome} className="btn btn-primary">Back to home</button>}/></main>}
