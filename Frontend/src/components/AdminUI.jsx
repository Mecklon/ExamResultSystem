import React from 'react';
import { useUser } from '../context/UserContext';

const AdminUI = () => {
  const { user } = useUser();
  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold text-slate-800 mb-4">Admin Dashboard</h1>
      <p className="text-slate-600 mb-8">Welcome, {user?.name || "Admin"}!</p>
    </div>
  );
};

export default AdminUI;
