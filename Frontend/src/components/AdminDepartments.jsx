import React, { useEffect, useState } from 'react';
import useGetFetch from '../Hooks/useGetFetch';
import useDeleteFetch from '../Hooks/useDeleteFetch';
import { FiAlertCircle, FiPlus } from "react-icons/fi";
import AddDepartmentForm from './AddDepartmentForm';
import DepartmentAccordionItem from './DepartmentAccordionItem';

const AdminDepartments = () => {
  const { fetch, loading, error } = useGetFetch();
  const { fetch: deleteDept, loading: deleteLoading, error: deleteError } = useDeleteFetch();
  const [departments, setDepartments] = useState([]);
  const [subjectMap, setSubjectMap] = useState({});
  const [expandedDeptId, setExpandedDeptId] = useState(null);
  const [deptToDelete, setDeptToDelete] = useState(null);
  const [isAddingDept, setIsAddingDept] = useState(false);

  const loadDepartments = async () => {
    const data = await fetch('/getDepartmentInfo');
    if (data) {
      const deptList = Array.isArray(data) ? data : (data.departments || []);
      setDepartments(deptList);
      const newSubjectMap = {};
      if (data.subjects && Array.isArray(data.subjects)) {
        data.subjects.forEach(s => newSubjectMap[s.subjectCode || s.code || s._id] = s);
      }
      setSubjectMap(newSubjectMap);
    }
  };

  useEffect(() => {
    loadDepartments();
  }, []); 

  const toggleExpand = (id) => {
    setExpandedDeptId(expandedDeptId === id ? null : id);
  };

  const confirmDelete = async () => {
    if (!deptToDelete) return;
    const deptName = deptToDelete.departmentName || deptToDelete.name;
    const res = await deleteDept('/admin/deleteDepartment', { departmentName: deptName });
    if (res !== undefined) {
       // Success, remove from state locally
       setDepartments(prev => prev.filter(d => d !== deptToDelete));
       setDeptToDelete(null);
       if (expandedDeptId === (deptToDelete._id || String(departments.indexOf(deptToDelete)))) {
         setExpandedDeptId(null);
       }
    }
  };

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <div className="mb-8 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-800">Departments</h1>
          <p className="text-slate-500 mt-2">Manage college departments and subjects by semester.</p>
        </div>
        {!isAddingDept && (
          <button 
            onClick={() => setIsAddingDept(true)}
            className="flex items-center justify-center gap-2 px-5 py-2.5 bg-slate-800 hover:bg-slate-900 text-white font-medium rounded-xl shadow-sm transition-colors"
          >
            <FiPlus size={18} /> Add Department
          </button>
        )}
      </div>

      {isAddingDept && <AddDepartmentForm onCancel={() => setIsAddingDept(false)} onSuccess={loadDepartments} subjectMap={subjectMap} />}

      {loading && (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-violet-600"></div>
        </div>
      )}

      {error && (
        <div className="p-4 bg-red-50 text-red-600 rounded-xl border border-red-100 mb-6">
          <p className="font-medium">Failed to load departments</p>
          <p className="text-sm opacity-80">{error}</p>
        </div>
      )}

      {!loading && !error && departments.length === 0 && !isAddingDept && (
        <div className="text-center py-16 bg-white rounded-2xl border border-slate-200 border-dashed">
          <p className="text-slate-500">No departments found or API is empty.</p>
        </div>
      )}

      {!loading && departments.length > 0 && (
        <div className="flex flex-col gap-4">
          {departments.map((dept, index) => {
            const deptId = dept._id || String(index);
            const isExpanded = expandedDeptId === deptId;
            
            return (
              <DepartmentAccordionItem
                key={deptId}
                dept={dept}
                index={index}
                subjectMap={subjectMap}
                isExpanded={isExpanded}
                toggleExpand={toggleExpand}
                setDeptToDelete={setDeptToDelete}
              />
            );
          })}
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deptToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6 animate-in fade-in zoom-in duration-200">
            <div className="flex items-center gap-4 mb-4 text-red-600">
              <div className="p-3 bg-red-50 rounded-full">
                <FiAlertCircle size={24} />
              </div>
              <h2 className="text-xl font-bold text-slate-800">Delete Department</h2>
            </div>
            
            <p className="text-slate-600 mb-6 leading-relaxed">
              Are you sure you want to delete the <span className="font-bold text-slate-800">{deptToDelete.departmentName || deptToDelete.name}</span> department? This action cannot be undone and will remove all associated subjects.
            </p>

            {deleteError && (
              <div className="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg border border-red-100">
                {deleteError}
              </div>
            )}

            <div className="flex items-center justify-end gap-3 mt-6">
              <button 
                onClick={() => setDeptToDelete(null)}
                disabled={deleteLoading}
                className="px-4 py-2 font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors disabled:opacity-50"
              >
                Cancel
              </button>
              <button 
                onClick={confirmDelete}
                disabled={deleteLoading}
                className="px-4 py-2 font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors disabled:opacity-50 flex items-center gap-2"
              >
                {deleteLoading ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                    Deleting...
                  </>
                ) : 'Delete Department'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDepartments;
