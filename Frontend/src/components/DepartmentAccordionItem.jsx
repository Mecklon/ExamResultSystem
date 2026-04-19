import React from 'react';
import { FiChevronDown, FiChevronUp, FiBook, FiTrash2 } from "react-icons/fi";

const DepartmentAccordionItem = ({ dept, index, subjectMap, isExpanded, toggleExpand, setDeptToDelete }) => {
  const deptId = dept._id || String(index);
  const semesters = Array.isArray(dept.subjectCodes) ? dept.subjectCodes : [];
  
  // Calculate total subjects (handling list of lists)
  let totalSubjects = 0;
  if (semesters.length > 0 && Array.isArray(semesters[0])) {
     totalSubjects = semesters.reduce((acc, sem) => acc + (Array.isArray(sem) ? sem.length : 0), 0);
  } else {
     totalSubjects = semesters.length;
  }

  return (
    <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden transition-all duration-200">
      {/* Header Section (Always Visible) */}
      <div 
        className="p-5 flex flex-col sm:flex-row sm:items-center justify-between cursor-pointer hover:bg-slate-50 transition-colors gap-3"
        onClick={() => toggleExpand(deptId)}
      >
        <div>
          <div className="flex items-center gap-3 flex-wrap">
            <h3 className="text-xl font-bold text-slate-800">{dept.name || dept.departmentName || `Department ${index + 1}`}</h3>
            {dept.code && (
              <span className="text-xs font-bold font-mono bg-violet-100 text-violet-700 px-2.5 py-1 rounded-md">{dept.code}</span>
            )}
          </div>
          <p className="text-slate-500 text-sm mt-1">{totalSubjects} Total Subjects • {semesters.length} Semesters</p>
        </div>
        <div className="flex items-center justify-between sm:justify-end gap-4 text-slate-400 w-full sm:w-auto">
          {isExpanded && (
            <button 
              onClick={(e) => { e.stopPropagation(); setDeptToDelete(dept); }}
              className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              title="Delete Department"
            >
              <FiTrash2 size={20} />
            </button>
          )}
          {isExpanded ? <FiChevronUp size={24} /> : <FiChevronDown size={24} />}
        </div>
      </div>

      {/* Expanded Content Section */}
      {isExpanded && (
        <div className="p-6 border-t border-slate-100 bg-slate-50/50">
          {semesters.length === 0 ? (
            <p className="text-slate-500 text-sm italic">No subjects configured for this department.</p>
          ) : (
            <div className="space-y-8">
              {semesters.map((semesterList, semIndex) => {
                const isListOfLists = Array.isArray(semesterList);
                const currentSemSubjects = isListOfLists ? semesterList : [semesterList];
                
                if (currentSemSubjects.length === 0) return null;

                return (
                  <div key={semIndex}>
                    <h4 className="text-sm font-bold text-violet-700 uppercase tracking-wider mb-4 flex items-center gap-2">
                      <span className="bg-violet-100 text-violet-700 px-2.5 py-1 rounded-md">
                        {isListOfLists ? `Semester ${semIndex + 1}` : 'Subjects'}
                      </span>
                    </h4>
                    
                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                      {currentSemSubjects.map((subjectItem, subIndex) => {
                        // Handle both strings (codes) and objects
                        const subjectCode = typeof subjectItem === 'object' ? (subjectItem.subjectCode || subjectItem.code || subjectItem._id) : subjectItem;
                        
                        // Look up details in our hashmap
                        const details = subjectMap[subjectCode] || (typeof subjectItem === 'object' ? subjectItem : null);
                        
                        return (
                          <div key={subIndex} className="bg-white p-4 rounded-xl border border-slate-200 flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-sm hover:border-violet-300 hover:shadow-md transition-all">
                            <div className="flex items-start gap-3">
                              <div>
                                <p className="font-semibold text-slate-800 text-sm leading-snug">
                                  {details?.name || details?.subjectName || `Subject Code: ${subjectCode}`}
                                </p>
                                <div className="flex flex-wrap gap-2 mt-2">
                                  <span className="text-[11px] font-mono bg-slate-100 text-slate-600 px-2 py-0.5 rounded">
                                    {subjectCode}
                                  </span>
                                  {details?.credits && (
                                    <span className="text-[11px] font-medium bg-green-50 text-green-700 px-2 py-0.5 rounded">
                                      {details.credits} Credits
                                    </span>
                                  )}
                                </div>
                              </div>
                            </div>
                            
                            <div className="flex items-center justify-between md:justify-end gap-4 md:gap-6 text-sm text-slate-600 bg-slate-50 px-5 py-2 rounded-lg border border-slate-100">
                                <div className="text-center">
                                  <p className="text-[10px] uppercase font-bold text-slate-400 mb-0.5">Internal</p>
                                  <p className="font-medium text-slate-700">{details?.totalInternalMarks || details?.internal || '-'}</p>
                                </div>
                                <div className="text-center">
                                  <p className="text-[10px] uppercase font-bold text-slate-400 mb-0.5">External</p>
                                  <p className="font-medium text-slate-700">{details?.totalExternalMarks || details?.external || '-'}</p>
                                </div>
                                <div className="text-center">
                                  <p className="text-[10px] uppercase font-bold text-slate-400 mb-0.5">Total</p>
                                  <p className="font-bold text-violet-700">{(details?.totalExternalMarks || 0) + (details?.totalInternalMarks || 0) || details?.total || '-'}</p>
                                </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default DepartmentAccordionItem;
