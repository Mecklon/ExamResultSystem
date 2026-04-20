import React, { useState, useEffect, useRef } from 'react';
import useGetFetch from '../Hooks/useGetFetch';
import { FiFilter, FiLoader, FiAlertCircle, FiAward, FiUsers } from 'react-icons/fi';
import api from '../api/api';

const Leaderboard = () => {
  const { fetch: fetchDeptInfo, loading: deptLoading, error: deptError } = useGetFetch();
  const { fetch: fetchLeaderboard, loading: leaderboardLoading, error: leaderboardError } = useGetFetch();
  
  const [departments, setDepartments] = useState([]);
  const [leaderboardData, setLeaderboardData] = useState(null);
  
  const { fetch: fetchLiveCount, state: liveCount } = useGetFetch(null);
  const currentViewRef = useRef(null);

  // Filter states
  const [selectedDept, setSelectedDept] = useState('');
  const [selectedSem, setSelectedSem] = useState('');
  const [selectedYear, setSelectedYear] = useState('');

  const decrementLiveCount = async (dept, year, sem) => {
    try {
      let url = `/decrementLiveCount/${dept}/${year}`;
      if (sem) {
        url += `?semester=${sem}`;
      }
      await api.get(url);
    } catch (err) {
      console.error("Failed to decrement live count", err);
    }
  };

  useEffect(() => {
    return () => {
      if (currentViewRef.current) {
        const { dept, year, sem } = currentViewRef.current;
        decrementLiveCount(dept, year, sem);
      }
    };
  }, []);

  const handleSearch = async () => {
    if (currentViewRef.current) {
      const { dept, year, sem } = currentViewRef.current;
      decrementLiveCount(dept, year, sem);
    }

    const dept = selectedDept;
    const year = selectedYear;
    const sem = selectedSem;

    currentViewRef.current = { dept, year, sem };

    let url = `/leaderboard/${dept}/${year}?limit=10`;
    let countUrl = `/getLiveCount/${dept}/${year}`;
    
    if (sem) {
      url += `&semester=${sem}`;
      countUrl += `?semester=${sem}`;
    }

    fetchLiveCount(countUrl);

    const data = await fetchLeaderboard(url);
    if (data) {
      setLeaderboardData(data);
    }
  };

  useEffect(() => {
    const loadDeptInfo = async () => {
      const data = await fetchDeptInfo('/getDepartmentInfo');

      if (data && data.departments) {
        setDepartments(data.departments);
      }
    };
    loadDeptInfo();
  }, []);

  // Compute maximum semesters for the currently selected department
  const activeDept = departments.find(d => d.code === selectedDept);
  const maxSemesters = activeDept?.subjectCodes?.length || 0;

  // Whenever department changes, reset semester if it exceeds the new max
  useEffect(() => {
    if (selectedSem && parseInt(selectedSem) > maxSemesters) {
      setSelectedSem('');
    }
  }, [selectedDept, maxSemesters, selectedSem]);

  return (
    <div className="p-4 md:p-8 max-w-5xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-800 tracking-tight">Top 10 Leaderboard</h1>
        <p className="text-slate-500 mt-2 text-lg">Filter to view the top-performing students across departments.</p>
      </div>

      {deptError && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-center gap-3 text-red-700">
          <FiAlertCircle size={20} className="shrink-0" />
          <p className="font-medium">Failed to load department info. Please refresh the page.</p>
        </div>
      )}

      {/* Filter Panel */}
      <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-6 md:p-8 mb-8 relative z-10">
        <div className="flex items-center gap-3 mb-6 pb-4 border-b border-slate-100">
          <div className="bg-violet-100 text-violet-600 p-2 rounded-xl">
            <FiFilter size={20} />
          </div>
          <h2 className="text-xl font-bold text-slate-800">Filter Rankings</h2>
        </div>
        
        {deptLoading ? (
          <div className="flex items-center gap-3 text-slate-500 py-4 justify-center">
            <FiLoader className="animate-spin text-violet-500" size={24} />
            <p className="font-medium">Loading departments...</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
            {/* Department */}
            <div className="md:col-span-8">
              <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-2">Department</label>
              <select 
                value={selectedDept}
                onChange={(e) => setSelectedDept(e.target.value)}
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500 text-slate-700 font-medium transition-all"
              >
                <option value="">Select Department</option>
                {departments.map(dept => (
                  <option key={dept.code} value={dept.code}>
                    {dept.name} ({dept.code})
                  </option>
                ))}
              </select>
            </div>

            {/* Semester */}
            <div className="md:col-span-2">
              <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-2">Semester</label>
              <select 
                value={selectedSem}
                onChange={(e) => setSelectedSem(e.target.value)}
                disabled={!selectedDept || maxSemesters === 0}
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500 text-slate-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                <option value="">All Semesters (CGPA)</option>
                {Array.from({ length: maxSemesters }, (_, i) => i + 1).map(sem => (
                  <option key={sem} value={sem}>
                    Semester {sem}
                  </option>
                ))}
              </select>
            </div>

            {/* Joining Year */}
            <div className="md:col-span-2">
              <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-2">Joining Year</label>
              <select 
                value={selectedYear}
                onChange={(e) => setSelectedYear(e.target.value)}
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500 text-slate-700 font-medium transition-all"
              >
                <option value="">Select Year</option>
                <option value="2024">2024</option>
                <option value="2025">2025</option>
              </select>
            </div>
          </div>

          <div className="mt-8 w-full border-t border-slate-100 pt-6">
            <button 
              onClick={handleSearch}
              disabled={!selectedDept || !selectedYear}
              className="w-full justify-center px-8 py-4 bg-violet-600 hover:bg-violet-700 text-white font-bold rounded-xl shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 text-lg"
            >
              Search Leaderboard
            </button>
          </div>
          </>
        )}
      </div>

      {/* Leaderboard Results */}
      {leaderboardLoading ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-16 flex flex-col items-center justify-center">
          <div className="w-12 h-12 border-4 border-violet-100 border-t-violet-500 rounded-full animate-spin mb-4"></div>
          <p className="text-slate-500 font-medium animate-pulse">Calculating rankings...</p>
        </div>
      ) : leaderboardError ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-12 text-center">
          <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <FiAlertCircle className="w-8 h-8 text-red-500" />
          </div>
          <h3 className="text-xl font-bold text-slate-800 mb-2">Error Loading Leaderboard</h3>
          <p className="text-slate-500 max-w-md mx-auto">{leaderboardError}</p>
        </div>
      ) : leaderboardData === null ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-16 text-center">
          <div className="w-20 h-20 bg-amber-50 rounded-full flex items-center justify-center mx-auto mb-6 border border-amber-100">
            <FiAward className="w-10 h-10 text-amber-400" />
          </div>
          <h3 className="text-2xl font-bold text-slate-800 mb-3">Ready to fetch leaderboard</h3>
          <p className="text-slate-500 max-w-md mx-auto text-lg">
            {selectedDept && selectedYear 
              ? "Click Search to load the top students for your selected filters." 
              : "Select a department and joining year to view the rankings."}
          </p>
        </div>
      ) : leaderboardData.length === 0 ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-16 text-center">
          <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-100">
            <svg className="w-8 h-8 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4"></path>
            </svg>
          </div>
          <h3 className="text-xl font-bold text-slate-800 mb-2">No Students Found</h3>
          <p className="text-slate-500 max-w-md mx-auto">There is no ranking data available for these filters yet.</p>
        </div>
      ) : (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
          <div className="bg-slate-50 border-b border-slate-100 px-8 py-5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <FiAward className="text-amber-500" />
              Top {leaderboardData.length} Students
              <span className="text-sm font-medium text-slate-500 ml-2">
                ({selectedSem ? `Semester ${selectedSem} SGPA` : 'Overall CGPA'})
              </span>
            </h3>
            {liveCount !== null && (
              <div className="flex items-center gap-2 bg-violet-100 text-violet-700 px-4 py-1.5 rounded-full text-sm font-bold shadow-sm border border-violet-200">
                <FiUsers className="animate-pulse" />
                <span>{liveCount} Live {liveCount === 1 ? 'Viewer' : 'Viewers'}</span>
              </div>
            )}
          </div>
          <div className="p-0 overflow-x-auto">
            <table className="w-full text-left border-collapse min-w-[500px]">
              <thead>
                <tr className="border-b border-slate-100 bg-white">
                  <th className="py-4 px-8 text-xs font-bold text-slate-400 uppercase tracking-wider w-24 text-center">Rank</th>
                  <th className="py-4 px-8 text-xs font-bold text-slate-400 uppercase tracking-wider">Registration Number</th>
                  <th className="py-4 px-8 text-xs font-bold text-slate-400 uppercase tracking-wider">Name</th>
                  <th className="py-4 px-8 text-xs font-bold text-slate-400 uppercase tracking-wider text-right">{selectedSem ? 'SGPA' : 'CGPA'}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {leaderboardData.map((entry, index) => (
                  <tr key={index} className="hover:bg-slate-50/50 transition-colors group">
                    <td className="py-4 px-8 text-center">
                      {index === 0 ? (
                        <div className="w-8 h-8 rounded-full bg-amber-100 text-amber-600 flex items-center justify-center font-bold mx-auto shadow-sm">1</div>
                      ) : index === 1 ? (
                        <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-600 flex items-center justify-center font-bold mx-auto shadow-sm">2</div>
                      ) : index === 2 ? (
                        <div className="w-8 h-8 rounded-full bg-amber-50 text-amber-700 flex items-center justify-center font-bold mx-auto shadow-sm">3</div>
                      ) : (
                        <div className="w-8 h-8 flex items-center justify-center font-medium text-slate-500 mx-auto">{index + 1}</div>
                      )}
                    </td>
                    <td className="py-4 px-8">
                      <span className="font-mono font-semibold text-slate-800">{entry.registrationNumber || entry.studentId}</span>
                    </td>
                    <td className="py-4 px-8">
                      <span className="font-semibold text-slate-700">{entry.name || 'Unknown'}</span>
                    </td>
                    <td className="py-4 px-8 text-right">
                      <span className="font-bold text-lg text-slate-700">
                        {entry.score !== undefined && entry.score !== null ? parseFloat(entry.score).toFixed(2) : 'N/A'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default Leaderboard;
