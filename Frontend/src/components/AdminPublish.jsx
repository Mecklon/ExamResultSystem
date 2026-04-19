import React, { useState, useEffect, useRef, forwardRef, useImperativeHandle } from 'react';
import useGetFetch from '../Hooks/useGetFetch';
import usePostFetch from '../Hooks/usePostFetch';
import useDebounce from '../Hooks/useDebounce';
import { FiSearch, FiChevronDown, FiChevronUp, FiX, FiCheckCircle, FiAlertCircle, FiLoader } from 'react-icons/fi';

import PublishStudentCard from './PublishStudentCard';

/* ──────────────────────────────────────────────────────────────
   AdminPublish
────────────────────────────────────────────────────────────── */
const AdminPublish = () => {
  const [regInput, setRegInput] = useState('');
  const [predictions, setPredictions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const debouncedReg = useDebounce(regInput, 300);

  const { fetch, loading: searchLoading } = useGetFetch();
  const { fetch: fetchDeptInfo } = useGetFetch();
  const { fetch: postStudents, loading: saving } = usePostFetch();

  const [departments, setDepartments] = useState([]);
  const [subjectMap, setSubjectMap] = useState({});
  const [studentList, setStudentList] = useState([]);
  const [saveStatuses, setSaveStatuses] = useState({}); // { [regNum]: ResultSaveResponse[] }

  // Accordion: only one expanded at a time
  const [expandedRegNum, setExpandedRegNum] = useState(null);

  const dropdownRef = useRef(null);
  const cardRefs = useRef({}); // { [regNum]: ref }

  // Prefetch dept + subject map
  useEffect(() => {
    const load = async () => {
      const data = await fetchDeptInfo('/getDepartmentInfo');
      if (data) {
        setDepartments(data.departments || []);
        const map = {};
        (data.subjects || []).forEach(s => { map[s.subjectCode || s.code || s._id] = s; });
        setSubjectMap(map);
      }
    };
    load();
  }, []);

  // Debounced predictions
  useEffect(() => {
    const get = async () => {
      if (!debouncedReg.trim()) { setPredictions([]); setShowDropdown(false); return; }
      const data = await fetch(`/admin/getPredictions/${debouncedReg.trim()}`);
      if (Array.isArray(data) && data.length > 0) { setPredictions(data); setShowDropdown(true); }
      else { setPredictions([]); setShowDropdown(false); }
    };
    get();
  }, [debouncedReg]);

  useEffect(() => {
    const handler = (e) => { if (dropdownRef.current && !dropdownRef.current.contains(e.target)) setShowDropdown(false); };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleSelectStudent = (student) => {
    if (!studentList.find(s => s.RegistrationNumber === student.RegistrationNumber)) {
      setStudentList(prev => [...prev, student]);
      setExpandedRegNum(student.RegistrationNumber); // auto-expand newly added
    }
    setRegInput('');
    setPredictions([]);
    setShowDropdown(false);
    setSaveStatuses(prev => ({ ...prev, [student.RegistrationNumber]: null }));
  };

  const removeStudent = (regNum) => {
    setStudentList(prev => prev.filter(s => s.RegistrationNumber !== regNum));
    delete cardRefs.current[regNum];
    setSaveStatuses(prev => {
      const next = { ...prev };
      delete next[regNum];
      return next;
    });
    if (expandedRegNum === regNum) setExpandedRegNum(null);
  };

  const handleBulkSave = async () => {
    if (studentList.length === 0) return;

    // Collect and validate each card's payload (each card returns an array of ResultDTOs)
    let fullPayload = [];
    for (const student of studentList) {
      const cardRef = cardRefs.current[student.RegistrationNumber];
      const validatedList = cardRef?.getValidatedPayload();
      if (validatedList === null) return; // validation failure on a card
      if (validatedList && validatedList.length > 0) {
        fullPayload = [...fullPayload, ...validatedList];
      }
    }

    if (fullPayload.length === 0) return;
    
    const res = await postStudents('/admin/publish-results', fullPayload);
    
    if (Array.isArray(res)) {
      // Group responses by registrationNumber to update each card's status
      const grouped = {};
      res.forEach(item => {
        const reg = item.registrationNumber;
        if (!grouped[reg]) grouped[reg] = [];
        grouped[reg].push(item);
      });
      setSaveStatuses(grouped);
    }
  };

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-800">Publish Results</h1>
        <p className="text-slate-500 mt-2">Search for students, enter their marks, and publish.</p>
      </div>

      {/* Search */}
      <div className="relative mb-8" ref={dropdownRef}>
        <label className="block text-sm font-semibold text-slate-700 mb-2">Add Student by Registration Number</label>
        <div className={`flex items-center gap-3 px-4 py-3 bg-white border rounded-2xl shadow-sm transition-all ${showDropdown ? 'border-violet-500 ring-2 ring-violet-500/20' : 'border-slate-200'}`}>
          <FiSearch className={`shrink-0 ${searchLoading ? 'text-violet-500 animate-pulse' : 'text-slate-400'}`} size={20} />
          <input type="text" placeholder="e.g. 2301000" value={regInput}
            onChange={e => setRegInput(e.target.value)}
            onFocus={() => predictions.length > 0 && setShowDropdown(true)}
            className="flex-1 bg-transparent text-slate-800 placeholder-slate-400 text-base focus:outline-none" />
          {searchLoading && <div className="w-4 h-4 border-2 border-violet-500/30 border-t-violet-500 rounded-full animate-spin shrink-0" />}
        </div>

        {showDropdown && predictions.length > 0 && (
          <div className="absolute z-20 top-full left-0 right-0 mt-2 bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
            {predictions.map((student, i) => (
              <div key={i} className="flex items-center gap-6 px-5 py-4 hover:bg-violet-50 cursor-pointer border-b border-slate-100 last:border-0 transition-colors"
                onMouseDown={() => handleSelectStudent(student)}>
                <p className="font-bold text-slate-800 text-base">{student.name}</p>
                <span className="text-slate-400">•</span>
                <p className="text-slate-600 text-base font-mono">{student.RegistrationNumber}</p>
                <span className="text-slate-400">•</span>
                <span className="text-sm font-bold bg-violet-100 text-violet-700 px-2.5 py-1 rounded-md">{student.departmentCode}</span>
              </div>
            ))}
          </div>
        )}

        {showDropdown && predictions.length === 0 && !searchLoading && debouncedReg && (
          <div className="absolute z-20 top-full left-0 right-0 mt-2 bg-white rounded-2xl shadow-lg border border-slate-200 px-4 py-6 text-center">
            <p className="text-slate-500 text-sm">No students found for <span className="font-semibold text-slate-700">"{debouncedReg}"</span></p>
          </div>
        )}
      </div>

      {/* Student Cards */}
      {studentList.length > 0 && (
        <div className="space-y-4 mb-8">
          {studentList.map(student => (
            <PublishStudentCard
              key={student.RegistrationNumber}
              ref={el => cardRefs.current[student.RegistrationNumber] = el}
              student={student}
              departments={departments}
              subjectMap={subjectMap}
              onRemove={() => removeStudent(student.RegistrationNumber)}
              isExpanded={expandedRegNum === student.RegistrationNumber}
              onToggle={() => setExpandedRegNum(prev => prev === student.RegistrationNumber ? null : student.RegistrationNumber)}
              statuses={saveStatuses[student.RegistrationNumber]}
            />
          ))}
        </div>
      )}

      {/* Bulk Save */}
      {studentList.length > 0 && (
        <div className="flex justify-start">
          <button onClick={handleBulkSave} disabled={saving}
            className="flex items-center gap-2 px-6 py-2.5 bg-slate-800 hover:bg-slate-900 disabled:opacity-60 text-white font-medium rounded-xl shadow-sm transition-colors">
            {saving
              ? <><div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Saving...</>
              : `Bulk Update (${studentList.length} student${studentList.length > 1 ? 's' : ''})`}
          </button>
        </div>
      )}
    </div>
  );
};

export default AdminPublish;
