import {useEffect, useState} from 'react'

export default function AdminManagement({api, notify}) {
  const [role, setRole] = useState('PATIENT')
  const [users, setUsers] = useState([])
  const [specialties, setSpecialties] = useState([])
  const [audit, setAudit] = useState([])
  const [newSpecialty, setNewSpecialty] = useState('')
  const [editing, setEditing] = useState(null)

  const loadUsers = async (selectedRole = role) => {
    try {
      const query = selectedRole ? `?role=${selectedRole}` : ''
      setUsers(await api(`/admin/users${query}`))
    } catch (error) { notify(error.message) }
  }
  const loadSpecialties = async () => {
    try { setSpecialties(await api('/specialties')) } catch (error) { notify(error.message) }
  }
  const loadAudit = async () => {
    try {
      const page = await api('/admin/audit?page=0&size=25&sort=createdAt,desc')
      setAudit(page.content || [])
    } catch (error) { notify(error.message) }
  }

  useEffect(() => { loadUsers(); loadSpecialties(); loadAudit() }, [])

  const updateUser = async (user) => {
    try {
      await api(`/admin/users/${user.id}/active?active=${!user.active}`, {method: 'PATCH'})
      notify(`Account ${user.active ? 'deactivated' : 'activated'}.`)
      await loadUsers()
      await loadAudit()
    } catch (error) { notify(error.message) }
  }
  const createSpecialty = async (event) => {
    event.preventDefault()
    try {
      await api('/admin/specialties', {method: 'POST', body: {name: newSpecialty.trim()}})
      setNewSpecialty('')
      notify('Specialty added.')
      await loadSpecialties()
      await loadAudit()
    } catch (error) { notify(error.message) }
  }
  const saveSpecialty = async (item) => {
    try {
      await api(`/admin/specialties/${item.id}`, {method: 'PATCH', body: {name: editing.name.trim()}})
      setEditing(null)
      notify('Specialty updated.')
      await loadSpecialties()
      await loadAudit()
    } catch (error) { notify(error.message) }
  }

  return <main className="dashboard-main admin-management">
    <section className="dashboard-panel">
      <div className="panel-heading"><div><h2>Account management</h2><p>Review patient, doctor, and administrator account status.</p></div>
        <select aria-label="Account role" value={role} onChange={event => {setRole(event.target.value); loadUsers(event.target.value)}}>
          <option value="PATIENT">Patients</option><option value="DOCTOR">Doctors</option><option value="ADMIN">Administrators</option><option value="">All roles</option>
        </select>
      </div>
      <div className="admin-doctors">{users.map(user => <div className="admin-doctor" key={user.id}>
        <div><b>{user.fullName}</b><span>{user.email} · {user.role}</span></div>
        <span className={`status ${user.active ? 'confirmed' : 'pending'}`}>{user.active ? 'Active' : 'Inactive'}</span>
        <button className="btn btn-outline small-btn" onClick={() => updateUser(user)}>{user.active ? 'Deactivate' : 'Activate'}</button>
      </div>)}</div>
      {users.length === 0 && <p className="admin-empty">No accounts found for this role.</p>}
    </section>

    <section className="dashboard-panel">
      <div className="panel-heading"><div><h2>Specialties</h2><p>Manage the specialties shown in doctor search.</p></div></div>
      <form className="admin-create-form" onSubmit={createSpecialty}>
        <label>New specialty<input value={newSpecialty} maxLength="80" required onChange={event => setNewSpecialty(event.target.value)}/></label>
        <button className="btn btn-primary small-btn">Add specialty</button>
      </form>
      <div className="admin-doctors">{specialties.map(item => <div className="admin-doctor" key={item.id}>
        {editing?.id === item.id ? <input aria-label="Specialty name" maxLength="80" value={editing.name} onChange={event => setEditing({...editing, name: event.target.value})}/> : <b>{item.name}</b>}
        {editing?.id === item.id ? <button className="btn btn-outline small-btn" onClick={() => saveSpecialty(item)}>Save</button> : <button className="btn btn-outline small-btn" onClick={() => setEditing({id: item.id, name: item.name})}>Rename</button>}
      </div>)}</div>
    </section>

    <section className="dashboard-panel">
      <div className="panel-heading"><div><h2>Audit log</h2><p>Recent administrator actions.</p></div></div>
      {audit.length === 0 ? <p className="admin-empty">No audit entries are available.</p> : <div className="admin-appointments">{audit.map(entry => <div className="admin-appointment" key={entry.id}>
        <div><b>{entry.action}</b><span>{entry.details || '—'}</span></div>
        <span>Actor {entry.actorId ?? 'system'} · {new Date(entry.createdAt).toLocaleString()}</span>
      </div>)}</div>}
    </section>
  </main>
}
