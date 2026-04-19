import React, { useState, useEffect } from 'react';
import useGetFetch from '../Hooks/useGetFetch';
import { FiFilter, FiLoader, FiAlertCircle, FiBarChart2, FiPieChart, FiTrendingUp } from 'react-icons/fi';

const AdminStatistics = () => {
  const { fetch: fetchDeptInfo, loading: deptLoading, error: deptError } = useGetFetch();
  const { fetch: fetchAnalytics, loading: analyticsLoading, error: analyticsError } = useGetFetch();
  
  const [departments, setDepartments] = useState([]);
  const [subjectMap, setSubjectMap] = useState({});
  const [analyticsData, setAnalyticsData] = useState(null);
  
  // Filter states
  const [selectedDept, setSelectedDept] = useState('');
  const [selectedYear, setSelectedYear] = useState('');

  useEffect(() => {
    const loadDeptInfo = async () => {
      const data = await fetchDeptInfo('/getDepartmentInfo');
      if (data) {
        if (data.departments) setDepartments(data.departments);
        if (data.subjects) {
          const map = {};
          data.subjects.forEach(sub => {
            map[sub.code] = sub;
          });
          setSubjectMap(map);
        }
      }
    };
    loadDeptInfo();
  }, []);

  const handleSearch = async () => {
    const data = await fetchAnalytics(`/stats/department/${selectedDept}/${selectedYear}`);
    if (data) {
      setAnalyticsData(Array.isArray(data) ? data : []);
    }
  };

  return (
    <div className="p-4 md:p-8 max-w-6xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-800 tracking-tight">Department Statistics</h1>
        <p className="text-slate-500 mt-2 text-lg">Analyze subject-wise performance metrics for specific cohorts.</p>
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
          <div className="bg-blue-100 text-blue-600 p-2 rounded-xl">
            <FiFilter size={20} />
          </div>
          <h2 className="text-xl font-bold text-slate-800">Filter Analytics</h2>
        </div>
        
        {deptLoading ? (
          <div className="flex items-center gap-3 text-slate-500 py-4 justify-center">
            <FiLoader className="animate-spin text-blue-500" size={24} />
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
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-slate-700 font-medium transition-all whitespace-normal break-words"
                >
                  <option value="" className="whitespace-normal break-words">Select Department</option>
                  {departments.map(dept => (
                    <option key={dept.code} value={dept.code} className="whitespace-normal break-words">
                      {dept.name} ({dept.code})
                    </option>
                  ))}
                </select>
              </div>

              {/* Joining Year */}
              <div className="md:col-span-4">
                <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-2">Joining Year</label>
                <select 
                  value={selectedYear}
                  onChange={(e) => setSelectedYear(e.target.value)}
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 text-slate-700 font-medium transition-all"
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
                className="w-full justify-center px-8 py-4 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 text-lg"
              >
                Generate Statistics
              </button>
            </div>
          </>
        )}
      </div>

      {/* Analytics Results */}
      {analyticsLoading ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-16 flex flex-col items-center justify-center">
          <div className="w-12 h-12 border-4 border-blue-100 border-t-blue-500 rounded-full animate-spin mb-4"></div>
          <p className="text-slate-500 font-medium animate-pulse">Computing analytics...</p>
        </div>
      ) : analyticsError ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-12 text-center">
          <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <FiAlertCircle className="w-8 h-8 text-red-500" />
          </div>
          <h3 className="text-xl font-bold text-slate-800 mb-2">Error Loading Statistics</h3>
          <p className="text-slate-500 max-w-md mx-auto">{analyticsError}</p>
        </div>
      ) : analyticsData === null ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-16 text-center">
          <div className="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6 border border-blue-100">
            <FiBarChart2 className="w-10 h-10 text-blue-400" />
          </div>
          <h3 className="text-2xl font-bold text-slate-800 mb-3">Ready to analyze</h3>
          <p className="text-slate-500 max-w-md mx-auto text-lg">
            {selectedDept && selectedYear 
              ? "Click Generate Statistics to load the subject-wise reports." 
              : "Select a department and joining year to view analytics."}
          </p>
        </div>
      ) : analyticsData.length === 0 ? (
        <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-16 text-center">
          <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-100">
            <FiPieChart className="w-8 h-8 text-slate-400" />
          </div>
          <h3 className="text-xl font-bold text-slate-800 mb-2">No Statistics Found</h3>
          <p className="text-slate-500 max-w-md mx-auto">There is no analytics data available for this cohort yet.</p>
        </div>
      ) : (
        <div className="space-y-6">
          <div className="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="bg-slate-50 border-b border-slate-100 px-8 py-5 flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2">
                <FiTrendingUp className="text-blue-500" />
                Subject Analytics Report
              </h3>
            </div>
            <div className="p-0 overflow-x-auto">
              <table className="w-full text-left border-collapse min-w-[800px]">
                <thead>
                  <tr className="border-b border-slate-100 bg-white">
                    <th className="py-4 px-6 text-xs font-bold text-slate-400 uppercase tracking-wider">Subject Code</th>
                    <th className="py-4 px-6 text-xs font-bold text-slate-400 uppercase tracking-wider">Subject Name</th>
                    {/* Render dynamic headers based on the first item's keys (excluding subjectCode) */}
                    {Object.keys(analyticsData[0])
                      .filter(key => key !== 'subjectCode')
                      .map(key => (
                        <th key={key} className="py-4 px-6 text-xs font-bold text-slate-400 uppercase tracking-wider text-right capitalize">
                          {key.replace(/([A-Z])/g, ' $1').trim()}
                        </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {analyticsData.map((item, index) => {
                    const subject = subjectMap[item.subjectCode] || {};
                    return (
                      <tr key={index} className="hover:bg-slate-50/50 transition-colors">
                        <td className="py-4 px-6">
                          <span className="font-mono text-xs font-bold bg-slate-100 text-slate-600 px-2.5 py-1 rounded-md">
                            {item.subjectCode || 'N/A'}
                          </span>
                        </td>
                        <td className="py-4 px-6 font-semibold text-slate-800 text-sm">
                          {subject.name || 'Unknown Subject'}
                        </td>
                        {Object.keys(item)
                          .filter(key => key !== 'subjectCode')
                          .map(key => {
                            const val = item[key];
                            const isNumber = typeof val === 'number';
                            return (
                              <td key={key} className="py-4 px-6 text-right">
                                <span className={`font-bold text-sm ${isNumber ? 'text-blue-700' : 'text-slate-600'}`}>
                                  {isNumber ? (val % 1 !== 0 ? val.toFixed(2) : val) : (val ?? '-')}
                                </span>
                              </td>
                            );
                        })}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminStatistics;
