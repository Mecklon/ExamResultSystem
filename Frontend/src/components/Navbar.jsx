import React from 'react';
import { Link } from 'react-router-dom';
import { useUser } from '../context/UserContext';
import { FiLogOut, FiAward, FiBarChart2, FiLayers, FiUploadCloud, FiBookOpen } from 'react-icons/fi';
import { MdOutlineLibraryBooks } from "react-icons/md";

const Navbar = () => {
  const { user, logout } = useUser();

  if (!user) return null;

  const isAdmin = user.role === 'ROLE_ADMIN';

  return (
    <nav className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-50 shadow-sm">
      <div className="flex items-center gap-2 text-violet-600">
        <MdOutlineLibraryBooks className="text-2xl" />
        <span className="text-xl font-bold text-slate-800 tracking-tight">RapidResult</span>
      </div>

      <div className="flex items-center gap-6">
        {!isAdmin ? (
          <>
            <Link to="/student" className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-violet-600 font-medium transition-colors">
              <FiBookOpen /> My Result
            </Link>
            <Link to="/student/leaderboard" className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-violet-600 font-medium transition-colors">
              <FiAward /> Leaderboards
            </Link>
          </>
        ) : (
          <>

            <Link to="/admin/statistics" className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-violet-600 font-medium transition-colors">
              <FiBarChart2 /> Statistics
            </Link>
            <Link to="/admin/departments" className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-violet-600 font-medium transition-colors">
              <FiLayers /> Departments
            </Link>
            <Link to="/admin/publish" className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-violet-600 font-medium transition-colors">
              <FiUploadCloud /> Publish Results
            </Link>
          </>
        )}
        
        <div className="w-px h-6 bg-slate-200 mx-2"></div>
        
        <button 
          onClick={logout} 
          className="flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-red-600 bg-red-50 hover:bg-red-100 rounded-lg transition-colors"
        >
          <FiLogOut /> Logout
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
