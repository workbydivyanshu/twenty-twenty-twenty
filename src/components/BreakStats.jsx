export default function BreakStats({ taken, skipped }) {
  return (
    <div className="break-stats">
      <div className="break-stat">
        <svg className="break-stat-icon taken" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/>
          <circle cx="12" cy="12" r="3"/>
        </svg>
        <span className="break-stat-num taken">{taken}</span>
        <span className="break-stat-num">taken</span>
      </div>
      <div className="break-stat">
        <svg className="break-stat-icon skipped" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/>
          <line x1="2" y1="2" x2="22" y2="22" strokeWidth="2.5"/>
        </svg>
        <span className="break-stat-num">{skipped}</span>
        <span className="break-stat-num">skipped</span>
      </div>
    </div>
  );
}
