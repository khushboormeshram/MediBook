import {createContext,useContext,useState} from 'react'
const AuthContext=createContext(null)
function storedUser(){try{return JSON.parse(localStorage.getItem('medibook_user')||'null')}catch{return null}}
export function AuthProvider({children}){const [user,setUser]=useState(storedUser);return <AuthContext.Provider value={{user,setUser}}>{children}</AuthContext.Provider>}
export function useAuth(){const value=useContext(AuthContext);if(!value)throw new Error('useAuth must be used inside AuthProvider');return value}
