import React, { useState, useRef, useEffect } from 'react';
import { FiPlus, FiX, FiTrash2, FiAlertCircle } from "react-icons/fi";
import usePostFetch from '../Hooks/usePostFetch';

const AddDepartmentForm = ({ onCancel, onSuccess, subjectMap = {} }) => {
  const defaultSubject = { code: '', name: '', internal: '', external: '', credits: '' };

  const [deptName, setDeptName] = useState('');
  const [deptCode, setDeptCode] = useState('');
  const [semesters, setSemesters] = useState([
    { subjects: [{ ...defaultSubject }] },
    { subjects: [{ ...defaultSubject }] }
  ]);
  const [errors, setErrors] = useState({});
  const [activeDropdown, setActiveDropdown] = useState({ semIndex: null, subIndex: null });
  const dropdownRef = useRef(null);
  const { fetch: postDepartment, loading: saving } = usePostFetch();

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setActiveDropdown({ semIndex: null, subIndex: null });
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const addSemester = () => {
    setSemesters([
      ...semesters,
      { subjects: [{ ...defaultSubject }] },
      { subjects: [{ ...defaultSubject }] }
    ]);
  };

  const addSubject = (semIndex) => {
    const updated = semesters.map((sem, i) =>
      i === semIndex ? { ...sem, subjects: [...sem.subjects, { ...defaultSubject }] } : sem
    );
    setSemesters(updated);
  };

  const removeSubject = (semIndex, subIndex) => {
    const updated = semesters.map((sem, i) =>
      i === semIndex ? { ...sem, subjects: sem.subjects.filter((_, si) => si !== subIndex) } : sem
    );
    setSemesters(updated);
  };

  const removeSemester = (semIndex) => {
    setSemesters(semesters.filter((_, i) => i !== semIndex));
  };

  const handleSubjectChange = (semIndex, subIndex, field, value) => {
    const updated = semesters.map((sem, i) =>
      i === semIndex
        ? { ...sem, subjects: sem.subjects.map((sub, si) => si === subIndex ? { ...sub, [field]: value } : sub) }
        : sem
    );
    setSemesters(updated);
    if (field === 'code') setActiveDropdown({ semIndex, subIndex });
  };

  const autofillSubject = (semIndex, subIndex, subjectCode) => {
    const details = subjectMap[subjectCode];
    if (details) {
      const updated = semesters.map((sem, i) =>
        i === semIndex
          ? {
              ...sem,
              subjects: sem.subjects.map((sub, si) =>
                si === subIndex
                  ? {
                      code: subjectCode,
                      name: details.name || details.subjectName || '',
                      internal: String(details.totalInternalMarks ?? details.internal ?? ''),
                      external: String(details.totalExternalMarks ?? details.external ?? ''),
                      credits: String(details.credits ?? ''),
                    }
                  : sub
              ),
            }
          : sem
      );
      setSemesters(updated);
    }
    setActiveDropdown({ semIndex: null, subIndex: null });
  };

  const getMatchedSubjects = (input) => {
    if (!input) return [];
    const lower = input.toLowerCase();
    return Object.values(subjectMap)
      .filter(s => (s.subjectCode || s.code || s._id || '').toLowerCase().includes(lower))
      .slice(0, 5);
  };

  // ─── Validation ───────────────────────────────────────────────────
  const validate = () => {
    const newErrors = {};

    if (!deptName.trim()) newErrors.deptName = 'Department name is required.';
    if (!deptCode.trim()) newErrors.deptCode = 'Department code is required.';

    if (semesters.length === 0) {
      newErrors.semesters = 'At least one semester is required.';
    } else {
      semesters.forEach((sem, semIndex) => {
        if (sem.subjects.length === 0) {
          newErrors[`sem_${semIndex}`] = 'Each semester must have at least one subject.';
        } else {
          sem.subjects.forEach((sub, subIndex) => {
            const allFilled = sub.code.trim() && sub.name.trim() && sub.internal !== '' && sub.external !== '' && sub.credits !== '';
            if (!allFilled) {
              newErrors[`sub_${semIndex}_${subIndex}`] = 'All subject fields are required.';
            }
          });
        }
      });
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // ─── Submit ───────────────────────────────────────────────────────
  const handleSubmit = async () => {
    if (!validate()) return;

    // Build DepartmentSaveRequest DTO
    const payload = {
      name: deptName.trim(),
      code: deptCode.trim(),
      duration: semesters.length,
      subjectList: semesters.map(sem =>
        sem.subjects.map(sub => ({
          code: sub.code.trim(),
          name: sub.name.trim(),
          totalInternalMarks: parseInt(sub.internal, 10),
          totalExternalMarks: parseInt(sub.external, 10),
          credits: parseInt(sub.credits, 10),
          isNew: !subjectMap[sub.code.trim()], // isNew = true if code not in existing map
        }))
      ),
    };

    const res = await postDepartment('/admin/addDepartment', payload);
    if (res !== undefined) {
      onSuccess && onSuccess();
      onCancel();
    }
  };

  // ─── UI ───────────────────────────────────────────────────────────
  return (
    <div className="bg-white p-6 md:p-8 rounded-2xl border border-slate-200 shadow-sm mb-8">
      <div className="flex items-center justify-between mb-8 pb-4 border-b border-slate-100">
        <div>
          <h2 className="text-xl font-bold text-slate-800">Add New Department</h2>
          <p className="text-sm text-slate-500 mt-1">Configure department details and subjects.</p>
        </div>
        <button onClick={onCancel} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
          <FiX size={24} />
        </button>
      </div>

      <div className="space-y-8">
        {/* ── Department Info ── */}
        <div>
          <h3 className="text-sm font-bold text-violet-700 uppercase tracking-wider mb-4">1. Department Info</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Department Name</label>
              <input
                type="text"
                placeholder="e.g. Computer Science"
                value={deptName}
                onChange={e => { setDeptName(e.target.value); setErrors(prev => ({ ...prev, deptName: null })); }}
                className={`w-full px-4 py-2.5 bg-slate-50 border rounded-xl focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500 transition-all ${errors.deptName ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
              />
              {errors.deptName && <p className="mt-1.5 text-xs text-red-600 flex items-center gap-1"><FiAlertCircle size={12}/> {errors.deptName}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Department Code</label>
              <input
                type="text"
                placeholder="e.g. CS"
                value={deptCode}
                onChange={e => { setDeptCode(e.target.value); setErrors(prev => ({ ...prev, deptCode: null })); }}
                className={`w-full px-4 py-2.5 bg-slate-50 border rounded-xl focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500 transition-all ${errors.deptCode ? 'border-red-400 bg-red-50' : 'border-slate-200'}`}
              />
              {errors.deptCode && <p className="mt-1.5 text-xs text-red-600 flex items-center gap-1"><FiAlertCircle size={12}/> {errors.deptCode}</p>}
            </div>
          </div>
        </div>

        {/* ── Semesters & Subjects ── */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold text-violet-700 uppercase tracking-wider">2. Semesters & Subjects</h3>
            <button onClick={addSemester} className="text-sm font-semibold text-violet-600 bg-violet-50 hover:bg-violet-100 px-3.5 py-2 rounded-lg transition-colors flex items-center gap-1.5">
              <FiPlus /> Add Semesters
            </button>
          </div>

          {errors.semesters && (
            <p className="mb-3 text-sm text-red-600 flex items-center gap-1"><FiAlertCircle size={14}/> {errors.semesters}</p>
          )}

          <div className="space-y-6">
            {semesters.map((sem, semIndex) => (
              <div key={semIndex} className={`p-5 bg-slate-50/50 border rounded-2xl ${errors[`sem_${semIndex}`] ? 'border-red-300' : 'border-slate-200'}`}>
                <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-200/60">
                  <h4 className="font-bold text-slate-800">Semester {semIndex + 1}</h4>
                  <div className="flex items-center gap-2">
                    <button onClick={() => addSubject(semIndex)} className="text-xs font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg transition-colors">
                      + Add Subject
                    </button>
                    {semesters.length > 1 && (
                      <button onClick={() => removeSemester(semIndex)} className="text-xs font-semibold text-red-600 bg-red-50 hover:bg-red-100 px-3 py-1.5 rounded-lg transition-colors">
                        Remove
                      </button>
                    )}
                  </div>
                </div>

                {errors[`sem_${semIndex}`] && (
                  <p className="mb-3 text-xs text-red-600 flex items-center gap-1"><FiAlertCircle size={12}/> {errors[`sem_${semIndex}`]}</p>
                )}

                <div className="space-y-3">
                  {sem.subjects.map((sub, subIndex) => {
                    const showDropdown = activeDropdown.semIndex === semIndex && activeDropdown.subIndex === subIndex;
                    const matches = getMatchedSubjects(sub.code);
                    const hasSubError = !!errors[`sub_${semIndex}_${subIndex}`];

                    return (
                      <div key={subIndex} className={`grid grid-cols-1 md:grid-cols-12 gap-3 bg-white p-3 rounded-xl border items-start shadow-sm ${hasSubError ? 'border-red-300' : 'border-slate-200'}`}>
                        {/* Code with autocomplete */}
                        <div className="md:col-span-2 relative" ref={showDropdown ? dropdownRef : null}>
                          <input
                            type="text"
                            placeholder="Code"
                            value={sub.code}
                            onChange={e => { handleSubjectChange(semIndex, subIndex, 'code', e.target.value); setErrors(prev => ({ ...prev, [`sub_${semIndex}_${subIndex}`]: null })); }}
                            onFocus={() => setActiveDropdown({ semIndex, subIndex })}
                            className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/50 ${hasSubError ? 'bg-red-50 border-red-300' : 'bg-slate-50 border-slate-200'}`}
                          />
                          {showDropdown && matches.length > 0 && (
                            <div className="absolute z-10 top-full left-0 mt-1 w-64 bg-white rounded-lg shadow-lg border border-slate-200 overflow-hidden">
                              {matches.map((match, i) => {
                                const code = match.subjectCode || match.code || match._id;
                                const name = match.name || match.subjectName || '';
                                return (
                                  <div
                                    key={i}
                                    className="px-4 py-2.5 hover:bg-slate-50 cursor-pointer border-b border-slate-100 last:border-0"
                                    onMouseDown={() => autofillSubject(semIndex, subIndex, code)}
                                  >
                                    <p className="text-sm font-semibold text-slate-800">{code}</p>
                                    <p className="text-xs text-slate-500 truncate">{name}</p>
                                  </div>
                                );
                              })}
                            </div>
                          )}
                        </div>

                        <input type="text" placeholder="Subject Name" value={sub.name}
                          onChange={e => { handleSubjectChange(semIndex, subIndex, 'name', e.target.value); setErrors(prev => ({ ...prev, [`sub_${semIndex}_${subIndex}`]: null })); }}
                          className={`md:col-span-4 px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/50 ${hasSubError ? 'bg-red-50 border-red-300' : 'bg-slate-50 border-slate-200'}`}
                        />
                        <input type="number" placeholder="Internal" value={sub.internal}
                          onChange={e => { handleSubjectChange(semIndex, subIndex, 'internal', e.target.value); setErrors(prev => ({ ...prev, [`sub_${semIndex}_${subIndex}`]: null })); }}
                          className={`md:col-span-2 px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/50 ${hasSubError ? 'bg-red-50 border-red-300' : 'bg-slate-50 border-slate-200'}`}
                        />
                        <input type="number" placeholder="External" value={sub.external}
                          onChange={e => { handleSubjectChange(semIndex, subIndex, 'external', e.target.value); setErrors(prev => ({ ...prev, [`sub_${semIndex}_${subIndex}`]: null })); }}
                          className={`md:col-span-2 px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/50 ${hasSubError ? 'bg-red-50 border-red-300' : 'bg-slate-50 border-slate-200'}`}
                        />
                        <div className="md:col-span-2 flex gap-2">
                          <input type="number" placeholder="Credits" value={sub.credits}
                            onChange={e => { handleSubjectChange(semIndex, subIndex, 'credits', e.target.value); setErrors(prev => ({ ...prev, [`sub_${semIndex}_${subIndex}`]: null })); }}
                            className={`w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/50 ${hasSubError ? 'bg-red-50 border-red-300' : 'bg-slate-50 border-slate-200'}`}
                          />
                          <button onClick={() => removeSubject(semIndex, subIndex)} className="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors" title="Remove Subject">
                            <FiTrash2 />
                          </button>
                        </div>

                        {hasSubError && (
                          <div className="md:col-span-12">
                            <p className="text-xs text-red-600 flex items-center gap-1"><FiAlertCircle size={12}/> {errors[`sub_${semIndex}_${subIndex}`]}</p>
                          </div>
                        )}
                      </div>
                    );
                  })}
                  {sem.subjects.length === 0 && (
                    <p className="text-sm text-slate-500 italic text-center py-4">No subjects added to this semester yet.</p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="flex items-center justify-end pt-6 border-t border-slate-100 gap-3">
          <button onClick={onCancel} className="px-5 py-2.5 text-slate-600 font-medium hover:bg-slate-100 rounded-xl transition-colors">
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={saving}
            className="px-6 py-2.5 bg-violet-600 hover:bg-violet-700 disabled:opacity-60 text-white font-medium rounded-xl shadow-sm transition-colors flex items-center gap-2"
          >
            {saving ? (
              <>
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                Saving...
              </>
            ) : (
              <><FiPlus /> Save Department</>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AddDepartmentForm;
