import React, { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import useGetFetch from '../Hooks/useGetFetch';
import { FiChevronDown, FiChevronUp, FiX, FiCheckCircle, FiAlertCircle, FiLoader } from 'react-icons/fi';

const PublishStudentCard = forwardRef(({ student, departments, subjectMap, onRemove, isExpanded, onToggle, statuses }, ref) => {
  useEffect(() => {
    if (isExpanded) loadResults();
  }, [isExpanded])
  const { fetch: fetchResult, loading: resultLoading } = useGetFetch();
  // null = not loaded, [] = empty, [...] = loaded
  const [results, setResults] = useState(null);

  // editableMarks[semester] = { [subjectCode]: { internal, external } }
  const [editableMarks, setEditableMarks] = useState({});

  // Section and JoiningYear from ResultOutputDTO
  const [studentInfo, setStudentInfo] = useState({ section: 'N/A', joiningYear: 'N/A' });

  const dept = departments.find(d => d.code === student.departmentCode) || null;

  // Latest semester = results already saved + 1
  const latestSemNumber = results !== null ? results.length + 1 : null;

  // Subject codes for the latest (new) semester from dept.subjectCodes (0-indexed)
  const latestSemSubjectCodes = (dept && latestSemNumber !== null)
    ? (dept.subjectCodes?.[latestSemNumber - 1] || [])
    : [];

  const loadResults = async () => {
    if (results !== null) return;
    const data = await fetchResult(`/result/${student.RegistrationNumber}`);
    if (Array.isArray(data)) {
      setResults(data);

      // Extract section/joiningYear from any result if available
      if (data.length > 0) {
        setStudentInfo({
          section: data[0].section || 'N/A',
          joiningYear: data[0].joiningYear || 'N/A'
        });
      }

      // Seed editableMarks for each past semester from fetched data
      const initialMarks = {};
      data.forEach(semResult => {
        const semMarks = {};
        semResult.marksList?.forEach(mark => {
          const code = mark.subjectSnapshot?.code;
          if (code) {
            semMarks[code] = {
              internal: mark.internalMarks ?? '',
              external: mark.externalMarks ?? '',
            };
          }
        });
        initialMarks[semResult.semester] = semMarks;
      });

      // Also seed latest semester as empty
      setEditableMarks(initialMarks);
    }
  };

  // Seed latest semester marks whenever subject codes become available
  useEffect(() => {
    if (latestSemNumber !== null && latestSemSubjectCodes.length > 0) {
      setEditableMarks(prev => {
        if (prev[latestSemNumber]) return prev; // already seeded
        const semMarks = {};
        latestSemSubjectCodes.forEach(code => { semMarks[code] = { internal: '', external: '' }; });
        return { ...prev, [latestSemNumber]: semMarks };
      });
    }
  }, [latestSemNumber, latestSemSubjectCodes.join(',')]);

  const handleToggle = () => {
    onToggle();
  };

  const [validationError, setValidationError] = useState(null);

  const updateMark = (sem, code, field, value, max) => {
    setValidationError(null);
    let val = value === '' ? '' : parseInt(value);
    
    // Cap at max if provided
    if (val !== '' && max !== undefined && val > max) {
      val = max;
    }

    setEditableMarks(prev => ({
      ...prev,
      [sem]: { ...prev[sem], [code]: { ...prev[sem]?.[code], [field]: val } },
    }));
  };

  // ── Exposed to parent via ref ──────────────────────────────
  useImperativeHandle(ref, () => ({
    getValidatedPayload() {
      const semesterKeys = Object.keys(editableMarks).map(Number);
      const latestKey = latestSemNumber;
      const studentResultDTOs = [];

      for (const semNum of semesterKeys) {
        const subjectMarks = editableMarks[semNum];
        const codes = Object.keys(subjectMarks);

        const anyFilled = codes.some(c => subjectMarks[c].internal !== '' || subjectMarks[c].external !== '');
        const allFilled = codes.every(c => subjectMarks[c].internal !== '' && subjectMarks[c].external !== '');

        // If this is the latest (new) semester and completely empty — skip it
        if (semNum === latestKey && !anyFilled) continue;

        // If partially filled, that's an error
        if (anyFilled && !allFilled) {
          setValidationError(`Semester ${semNum}: all subject fields must be filled if any are entered.`);
          return null; // signal validation failure
        }

        if (allFilled) {
          studentResultDTOs.push({
            studentId: student.RegistrationNumber.toString(), // Using RegNum as studentId string
            registrationNumber: student.RegistrationNumber,
            semester: semNum,
            marksList: codes.map(code => ({
              code: code,
              internalMarks: subjectMarks[code].internal === '' ? 0 : parseInt(subjectMarks[code].internal),
              externalMarks: subjectMarks[code].external === '' ? 0 : parseInt(subjectMarks[code].external)
            }))
          });
        }
      }

      setValidationError(null);
      return studentResultDTOs;
    }
  }));

  const sortedPast = results ? [...results].sort((a, b) => b.semester - a.semester) : [];

  const renderSubjectRow = (code, semNum, subDetails, marksData) => {
    const maxInt = subDetails?.totalInternalMarks;
    const maxExt = subDetails?.totalExternalMarks;

    return (
      <div key={code} className="bg-white border border-slate-200 rounded-xl p-4 grid grid-cols-1 md:grid-cols-12 gap-4 items-center shadow-sm">
        <div className="md:col-span-6">
          <p className="font-semibold text-slate-800 text-sm">{subDetails?.name || subDetails?.subjectName || code}</p>
          <div className="flex gap-2 mt-1">
            <span className="text-[11px] font-mono bg-slate-100 text-slate-500 px-2 py-0.5 rounded">{code}</span>
            {subDetails?.credits && <span className="text-[11px] bg-green-50 text-green-700 font-medium px-2 py-0.5 rounded">{subDetails.credits} Cr</span>}
          </div>
        </div>
        <div className="md:col-span-3">
          <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">
            Internal {maxInt ? `/ ${maxInt}` : ''}
          </label>
          <input type="number" min={0} value={marksData?.internal ?? ''}
            onChange={e => updateMark(semNum, code, 'internal', e.target.value, maxInt)}
            className="w-full px-3 py-2 text-sm border border-slate-200 bg-slate-50 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-400/30" />
        </div>
        <div className="md:col-span-3">
          <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">
            External {maxExt ? `/ ${maxExt}` : ''}
          </label>
          <input type="number" min={0} value={marksData?.external ?? ''}
            onChange={e => updateMark(semNum, code, 'external', e.target.value, maxExt)}
            className="w-full px-3 py-2 text-sm border border-slate-200 bg-slate-50 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-400/30" />
        </div>
      </div>
    );
  };

  return (
    <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden pb-4">
      {/* ── Header ── */}
      <div className="flex items-center justify-between px-5 py-4 cursor-pointer hover:bg-slate-50 transition-colors" onClick={handleToggle}>
        <div className="flex items-center gap-6 flex-wrap">
          <p className="text-base font-bold text-slate-800">{student.name}</p>
          <span className="text-slate-300">|</span>
          <p className="text-base font-mono text-slate-600">{student.RegistrationNumber}</p>
          <span className="text-slate-300">|</span>
          <span className="text-xs font-bold bg-violet-100 text-violet-700 px-2.5 py-1 rounded-md">{student.departmentCode}</span>
        </div>
        <div className="flex items-center gap-3">
          {resultLoading && <FiLoader className="animate-spin text-violet-500" />}
          <button onClick={(e) => { e.stopPropagation(); onRemove(); }} className="p-1.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors">
            <FiX size={18} />
          </button>
          {isExpanded ? <FiChevronUp size={20} className="text-slate-400" /> : <FiChevronDown size={20} className="text-slate-400" />}
        </div>
      </div>
      {/* Status Indicators */}
      {statuses && statuses.length > 0 && (
        <div className="mx-5 mt-3 space-y-2">
          
          {statuses.some(res => res.status !== 'SUCCESS') ? (
            statuses.filter(res => res.status !== 'SUCCESS').map((res, i) => (
              <div key={i} className="flex items-center gap-3 p-3 rounded-xl border text-sm bg-red-50 border-red-200 text-red-700">
                <FiAlertCircle className="shrink-0" />
                <p className="font-medium">{res.message || 'An error occurred'}</p>
              </div>
            ))
          ) : (
            <div className="flex items-center gap-3 p-3 rounded-xl border text-sm bg-green-50 border-green-200 text-green-700">
              <FiCheckCircle className="shrink-0" />
              <p className="font-medium">Saved</p>
            </div>
          )}
        </div>
      )}
     
      {/* Validation error banner */}
      {validationError && (
        <div className="mx-5 mb-4 p-3 bg-red-50 border border-red-200 rounded-xl flex items-center gap-2 text-sm text-red-700">
          <FiAlertCircle size={16} className="shrink-0" />
          {validationError}
        </div>
      )}

      {/* ── Body ── */}
      {isExpanded && (
        <div className="border-t border-slate-100 p-6 space-y-8 bg-slate-50/30">

          {resultLoading && (
            <p className="text-sm text-slate-400 flex items-center gap-2"><FiLoader className="animate-spin" /> Loading results...</p>
          )}

          {/* Student metadata (Read-only) */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white border border-slate-200 rounded-xl p-3 shadow-sm">
              <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Section</label>
              <p className="text-sm font-semibold text-slate-700">{studentInfo.section}</p>
            </div>
            <div className="bg-white border border-slate-200 rounded-xl p-3 shadow-sm">
              <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Joining Year</label>
              <p className="text-sm font-semibold text-slate-700">{studentInfo.joiningYear}</p>
            </div>
          </div>

          {/* ── New (latest) semester ── */}
          {latestSemNumber !== null && latestSemSubjectCodes.length > 0 && (
            <div>
              <div className="mb-3">
                <span className="text-xs font-bold text-violet-700 uppercase tracking-wider bg-violet-100 px-2.5 py-1 rounded-md">
                  Semester {latestSemNumber} — New Entry
                </span>
              </div>
              <div className="space-y-3">
                {latestSemSubjectCodes.map(code =>
                  renderSubjectRow(code, latestSemNumber, subjectMap[code], editableMarks[latestSemNumber]?.[code])
                )}
              </div>
            </div>
          )}

          {/* ── Past semesters (editable, reverse order) ── */}
          {sortedPast.map(semResult => (
            <div key={semResult.semester}>
              <div className="mb-3">
                <span className="text-xs font-bold text-slate-600 uppercase tracking-wider bg-slate-200 px-2.5 py-1 rounded-md">
                  Semester {semResult.semester}
                </span>
              </div>
              <div className="space-y-3">
                {semResult.marksList?.map(mark => {
                  const code = mark.subjectSnapshot?.code;
                  const subDetails = subjectMap[code] || mark.subjectSnapshot;
                  return renderSubjectRow(code, semResult.semester, subDetails, editableMarks[semResult.semester]?.[code]);
                })}
              </div>
            </div>
          ))}

          {results !== null && results.length === 0 && latestSemSubjectCodes.length === 0 && (
            <p className="text-sm text-slate-400 italic">No results and no department subjects found.</p>
          )}
        </div>
      )}
    </div>
  );
});

export default PublishStudentCard;
