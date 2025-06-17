import { Routes, Route, Navigate } from 'react-router-dom'
import { Items } from './pages/Items'
import { Checklists } from './pages/Checklists'
import Login from './pages/Login'
import Register from './pages/Register'
import { Uids } from './pages/Uids'
import Home from './pages/Home'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/items" element={<Items />} />
      <Route path="/checklists" element={<Checklists />} />
      <Route path="/uids" element={<Uids />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
