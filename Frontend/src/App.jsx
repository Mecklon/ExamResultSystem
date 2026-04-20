import { Routes, Route, Navigate } from 'react-router-dom';
import { useUser } from './context/UserContext';
import Login from './components/Login';
import StudentUI from './components/StudentUI';
import AdminUI from './components/AdminUI';
import Navbar from './components/Navbar';
import Leaderboard from './components/Leaderboard';
import AdminStatistics from './components/AdminStatistics';
import AdminDepartments from './components/AdminDepartments';
import AdminPublish from './components/AdminPublish';

function App() {
  const { user } = useUser();
  console.log(user)
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />
      <div className="flex-1 overflow-auto">
        <Routes>
          <Route path="/login" element={!user ? <Login /> : <Navigate to={user.role === 'ROLE_ADMIN' ? '/admin/departments' : '/student'} replace />} />
          
          <Route path="/student" element={user ? <StudentUI /> : <Navigate to="/login" replace />} />
          <Route path="/student/leaderboard" element={user ? <Leaderboard /> : <Navigate to="/login" replace />} />
          
          <Route path="/admin" element={user && user.role === 'ROLE_ADMIN' ? <Navigate to="/admin/departments" replace /> : <Navigate to="/login" replace />} />
          <Route path="/admin/leaderboard" element={user && user.role === 'ROLE_ADMIN' ? <Leaderboard /> : <Navigate to="/login" replace />} />
          <Route path="/admin/statistics" element={user && user.role === 'ROLE_ADMIN' ? <AdminStatistics /> : <Navigate to="/login" replace />} />
          <Route path="/admin/departments" element={user && user.role === 'ROLE_ADMIN' ? <AdminDepartments /> : <Navigate to="/login" replace />} />
          <Route path="/admin/publish" element={user && user.role === 'ROLE_ADMIN' ? <AdminPublish /> : <Navigate to="/login" replace />} />

          <Route path="*" element={<Navigate to={user ? (user.role === 'ROLE_ADMIN' ? '/admin/departments' : '/student') : "/login"} replace />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
