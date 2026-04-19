import React, { useEffect, useState } from 'react';
import { useUser } from '../context/UserContext';
import useGetFetch from '../Hooks/useGetFetch';
import { FiBookOpen, FiAward, FiAlertCircle, FiLoader } from 'react-icons/fi';

const StudentUI = () => {
  const { user } = useUser();
  const { fetch: fetchResult, loading, error } = useGetFetch();
  const [results, setResults] = useState(null);

  useEffect(() => {
    const loadResults = async () => {
      const regNum = user?.username || user?.RegistrationNumber || user?.registrationNumber;
      if (!regNum) return;
      
      const data = await fetchResult(`/result/${regNum}`);
      if (Array.isArray(data)) {
        // Sort descending so the latest semester is shown first
        const sorted = [...data].sort((a, b) => b.semester - a.semester);
        setResults(sorted);
      }
    };
    loadResults();
  }, [user]);

  // Extract metadata from the first result block if available
  const studentInfo = {
    section: results?.[0]?.section || 'N/A',
    joiningYear: results?.[0]?.joiningYear || 'N/A'
  };

  return (
    <div className="p-4 md:p-8 max-w-5xl mx-auto">
      {/* Header Profile Section */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-100 mb-8 flex flex-col md:flex-row gap-6 items-start md:items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-800 tracking-tight">Student Dashboard</h1>
          <p className="text-slate-500 mt-2 text-lg">Welcome back, <span className="font-semibold text-slate-700">{user?.name || "Student"}</span>!</p>
        </div>
        <div className="flex gap-4 w-full md:w-auto">
          <div className="bg-violet-50 text-violet-700 px-5 py-3 rounded-2xl flex flex-col items-center justify-center flex-1 md:flex-none">
            <span className="text-[10px] font-bold uppercase tracking-wider opacity-70 mb-1">Reg Number</span>
            <span className="font-mono font-bold text-sm md:text-base">{user?.username || 'N/A'}</span>
          </div>
          <div className="bg-blue-50 text-blue-700 px-5 py-3 rounded-2xl flex flex-col items-center justify-center flex-1 md:flex-none">
            <span className="text-[10px] font-bold uppercase tracking-wider opacity-70 mb-1">Section</span>
            <span className="font-bold text-sm md:text-base">{studentInfo.section}</span>
          </div>
        </div>
      </div>

      {error && (
        <div className="mb-8 p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center gap-3 text-red-700">
          <FiAlertCircle size={20} className="shrink-0" />
          <p className="font-medium">{error}</p>
        </div>
      )}

      {loading ? (
        <div className="flex flex-col items-center justify-center py-20">
          <div className="w-12 h-12 border-4 border-violet-100 border-t-violet-500 rounded-full animate-spin mb-4"></div>
          <p className="text-slate-500 font-medium animate-pulse">Fetching your results...</p>
        </div>
      ) : results !== null && results.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-100 shadow-sm">
          <div className="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-6">
            <FiBookOpen size={32} className="text-slate-400" />
          </div>
          <h3 className="text-xl font-bold text-slate-800 mb-2">No Results Found</h3>
          <p className="text-slate-500 max-w-md mx-auto">Your results have not been published yet. Please check back later or contact administration.</p>
        </div>
      ) : (
        <div className="space-y-8">
          {results?.map((res) => (
            <div key={res.semester} className="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
              <div className="bg-slate-50 border-b border-slate-100 px-6 py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="bg-violet-100 text-violet-600 w-12 h-12 rounded-xl flex items-center justify-center font-bold text-xl">
                    {res.semester}
                  </div>
                  <div>
                    <h2 className="text-lg font-bold text-slate-800">Semester {res.semester}</h2>
                    <p className="text-sm text-slate-500">Academic Results</p>
                  </div>
                </div>
                <div className="hidden md:flex items-center gap-2 bg-white px-4 py-2 rounded-xl border border-slate-200 shadow-sm">
                  <FiAward className="text-amber-500" />
                  <span className="text-sm font-bold text-slate-700">{res.marksList?.length || 0} Subjects</span>
                </div>
              </div>
              
              <div className="p-6">
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse min-w-[600px]">
                    <thead>
                      <tr className="border-b-2 border-slate-100">
                        <th className="pb-3 px-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Subject Code</th>
                        <th className="pb-3 px-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Subject Name</th>
                        <th className="pb-3 px-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-center">Credits</th>
                        <th className="pb-3 px-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-center">Internal</th>
                        <th className="pb-3 px-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-center">External</th>
                        <th className="pb-3 px-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-center">Total</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-50">
                      {res.marksList?.map((mark, i) => {
                        const sub = mark.subjectSnapshot;
                        const intMax = sub?.totalInternalMarks;
                        const extMax = sub?.totalExternalMarks;
                        const totalObtained = (mark.internalMarks || 0) + (mark.externalMarks || 0);
                        const totalMax = (intMax || 0) + (extMax || 0);

                        return (
                          <tr key={i} className="hover:bg-slate-50/50 transition-colors">
                            <td className="py-4 px-4">
                              <span className="font-mono text-xs font-bold bg-slate-100 text-slate-600 px-2.5 py-1 rounded-md">
                                {sub?.code || 'N/A'}
                              </span>
                            </td>
                            <td className="py-4 px-4 font-semibold text-slate-800 text-sm">
                              {sub?.name || 'Unknown Subject'}
                            </td>
                            <td className="py-4 px-4 text-center">
                              {sub?.credits ? (
                                <span className="bg-blue-50 text-blue-600 text-xs font-bold px-2.5 py-1 rounded-md">
                                  {sub.credits}
                                </span>
                              ) : '-'}
                            </td>
                            <td className="py-4 px-4 text-center">
                              <span className="text-sm font-bold text-slate-700">{mark.internalMarks ?? '-'}</span>
                              {intMax && <span className="text-xs text-slate-400 ml-1">/ {intMax}</span>}
                            </td>
                            <td className="py-4 px-4 text-center">
                              <span className="text-sm font-bold text-slate-700">{mark.externalMarks ?? '-'}</span>
                              {extMax && <span className="text-xs text-slate-400 ml-1">/ {extMax}</span>}
                            </td>
                            <td className="py-4 px-4 text-center">
                              <span className="text-sm font-bold text-violet-700">{totalObtained}</span>
                              {totalMax > 0 && <span className="text-xs text-slate-400 ml-1">/ {totalMax}</span>}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default StudentUI;
