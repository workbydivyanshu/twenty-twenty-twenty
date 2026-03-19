import { useState, useEffect } from 'react';
import { RING_CIRCUMFERENCE } from '../utils/constants';

export default function BreakOverlay({ countdown, onConfirm, onSkip, onEndSession, onEnter }) {
  const [showConfirmPrompt, setShowConfirmPrompt] = useState(false);
  const [showEndDialog, setShowEndDialog] = useState(false);

  useEffect(() => {
    if (onEnter) onEnter();
  }, [onEnter]);

  useEffect(() => {
    if (countdown === 0) {
      setShowConfirmPrompt(true);
    }
  }, [countdown]);

  const progress = countdown > 0 ? (20 - countdown) / 20 : 1;
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

      {countdown > 0 ? (
        <>
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
        </>
      ) : showConfirmPrompt ? (
        <>
          <div className="break-confirm-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <circle cx="12" cy="12" r="10" />
              <path d="M12 6v6M12 16v.01" />
            </svg>
          </div>

          <div className="break-title" id="break-title">
            Did you rest your eyes?
          </div>

          <div className="break-subtitle">
            Looked at something 20 feet away<br />for 20 seconds
          </div>

          <div className="break-actions">
            <button className="btn-danger" onClick={() => setShowEndDialog(true)}>
              No, I didn't
            </button>
            <button className="btn-success" onClick={onConfirm}>
              Yes, I did
            </button>
          </div>
        </>
      ) : null}

      {showEndDialog && (
        <div className="end-dialog-overlay">
          <div className="end-dialog">
            <h3>End session?</h3>
            <p>Your break will be counted as skipped.</p>
            <div className="end-dialog-actions">
              <button className="btn-ghost" onClick={() => {
                setShowEndDialog(false);
                setShowConfirmPrompt(false);
                onSkip();
              }}>
                Continue anyway
              </button>
              <button className="btn-danger" onClick={() => {
                setShowEndDialog(false);
                setShowConfirmPrompt(false);
                onEndSession();
              }}>
                End Session
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
