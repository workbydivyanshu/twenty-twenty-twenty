import { useEffect } from 'react';
import { RING_CIRCUMFERENCE } from '../utils/constants';

export default function BreakOverlay({ countdown, onConfirm, onSkip, onEnter }) {
  useEffect(() => {
    if (onEnter) onEnter();
  }, [onEnter]);

  const progress = (20 - countdown) / 20;
  const dashOffset = RING_CIRCUMFERENCE * (1 - progress);

  return (
    <div className="break-overlay" role="dialog" aria-modal="true" aria-labelledby="break-title">
      <svg style={{ position: 'absolute', width: 0, height: 0 }}>
        <defs>
          <linearGradient id="ringGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#818cf8" />
            <stop offset="100%" stopColor="#6366f1" />
          </linearGradient>
        </defs>
      </svg>

      <div className="break-ring-wrap">
        <svg className="break-ring-svg" width="240" height="240" viewBox="0 0 240 240">
          <circle className="break-ring-bg" cx="120" cy="120" r={110} />
          <circle
            className="break-ring-progress"
            cx="120"
            cy="120"
            r={110}
            style={{ strokeDashoffset: dashOffset }}
          />
        </svg>
        <div className="break-countdown" aria-live="assertive" aria-atomic="true">
          {countdown}
        </div>
      </div>

      <div className="break-title" id="break-title">
        Rest your eyes
      </div>

      <div className="break-subtitle">
        Look at something 20 feet away.<br />Give your eyes a break.
      </div>

      <div className="break-rule">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
          <path d="M12 2v20M2 12h20"/>
        </svg>
        20 feet · 20 seconds
      </div>

      <div className="break-actions">
        <button className="btn-success" onClick={onConfirm}>
          Yes, I rested
        </button>
        <button className="btn-ghost" onClick={onSkip}>
          Skip
        </button>
      </div>
    </div>
  );
}
