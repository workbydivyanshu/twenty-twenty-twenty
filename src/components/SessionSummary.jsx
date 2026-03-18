import { useState, useCallback } from 'react';
import { formatDurationLong, formatPercentage } from '../utils/dateUtils';

export default function SessionSummary({ session, onDone }) {
  const [sharing, setSharing] = useState(false);
  const { durationMs, breaksTaken, breaksSkipped, breaksTriggered } = session;
  const totalBreaks = breaksTaken + breaksSkipped;
  const complianceRate = totalBreaks > 0 ? breaksTaken / (breaksTriggered || 1) : 0;

  const handleShare = useCallback(async () => {
    setSharing(true);
    try {
      const canvas = document.createElement('canvas');
      canvas.width = 600;
      canvas.height = 400;
      const ctx = canvas.getContext('2d');

      const bg = ctx.createLinearGradient(0, 0, 600, 400);
      bg.addColorStop(0, '#0a0a14');
      bg.addColorStop(1, '#0f0f24');
      ctx.fillStyle = bg;
      ctx.fillRect(0, 0, 600, 400);

      const accentGrad = ctx.createLinearGradient(0, 0, 600, 0);
      accentGrad.addColorStop(0, '#818cf8');
      accentGrad.addColorStop(1, '#6366f1');
      ctx.fillStyle = accentGrad;
      ctx.fillRect(0, 0, 600, 4);

      ctx.fillStyle = '#818cf8';
      ctx.font = 'bold 28px system-ui, sans-serif';
      ctx.fillText('Twenty Session Complete', 40, 70);

      ctx.fillStyle = '#94a3b8';
      ctx.font = '14px system-ui, sans-serif';
      const dateStr = new Date(session.startTime).toLocaleDateString('en-US', {
        weekday: 'long', month: 'long', day: 'numeric', year: 'numeric'
      });
      ctx.fillText(dateStr, 40, 96);

      const cards = [
        { label: 'Total Time', value: formatDurationLong(durationMs) },
        { label: 'Breaks Taken', value: breaksTaken, color: '#34d399' },
        { label: 'Breaks Skipped', value: breaksSkipped, color: '#64748b' },
        { label: 'Compliance', value: `${Math.round(complianceRate * 100)}%`, color: '#818cf8' },
      ];

      cards.forEach((card, i) => {
        const x = 40 + i * 130;
        const y = 140;
        ctx.fillStyle = 'rgba(255,255,255,0.05)';
        ctx.strokeStyle = 'rgba(255,255,255,0.08)';
        ctx.lineWidth = 1;
        roundRect(ctx, x, y, 120, 90, 12);
        ctx.fill();
        ctx.stroke();

        ctx.fillStyle = card.color || '#f1f5f9';
        ctx.font = 'bold 26px system-ui, monospace';
        ctx.fillText(card.value, x + 12, y + 42);

        ctx.fillStyle = '#64748b';
        ctx.font = '11px system-ui, sans-serif';
        ctx.fillText(card.label.toUpperCase(), x + 12, y + 66);
      });

      if (totalBreaks > 0) {
        ctx.fillStyle = '#475569';
        ctx.fillText('COMPLIANCE', 40, 280);
        ctx.fillStyle = 'rgba(255,255,255,0.08)';
        roundRect(ctx, 40, 288, 520, 12, 6);
        ctx.fill();
        ctx.fillStyle = accentGrad;
        roundRect(ctx, 40, 288, 520 * complianceRate, 12, 6);
        ctx.fill();
      }

      ctx.fillStyle = '#475569';
      ctx.font = '12px system-ui, sans-serif';
      ctx.fillText('20-20-20 Rule · twenty.app', 40, 370);

      const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png'));
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `twenty-session-${new Date().toISOString().split('T')[0]}.png`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Share failed:', err);
    }
    setSharing(false);
  }, [session, durationMs, breaksTaken, breaksSkipped, complianceRate, totalBreaks]);

  return (
    <div className="session-summary">
      <div className="summary-header">
        <div className="summary-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <div className="summary-title">Session Complete</div>
        <div className="summary-subtitle">Great work taking care of your eyes</div>
      </div>

      <div className="summary-stats">
        <div className="summary-stat" style={{ '--i': 1 }}>
          <div className="summary-stat-value">{formatDurationLong(durationMs)}</div>
          <div className="summary-stat-label">Total Time</div>
        </div>
        <div className="summary-stat" style={{ '--i': 2 }}>
          <div className="summary-stat-value success">{breaksTaken}</div>
          <div className="summary-stat-label">Breaks Taken</div>
        </div>
        <div className="summary-stat" style={{ '--i': 3 }}>
          <div className="summary-stat-value warning">{breaksSkipped}</div>
          <div className="summary-stat-label">Breaks Skipped</div>
        </div>
        <div className="summary-stat" style={{ '--i': 4 }}>
          <div className="summary-stat-value">{breaksTriggered}</div>
          <div className="summary-stat-label">Breaks Triggered</div>
        </div>
      </div>

      {totalBreaks > 0 && (
        <div className="summary-compliance">
          <div className="compliance-header">
            <span className="compliance-label">Compliance Rate</span>
            <span className="compliance-value">{formatPercentage(complianceRate, 0)}</span>
          </div>
          <div className="compliance-bar">
            <div
              className="compliance-fill"
              style={{ width: `${complianceRate * 100}%` }}
            />
          </div>
        </div>
      )}

      <div className="summary-actions">
        <button className="btn-share" onClick={handleShare} disabled={sharing}>
          {sharing ? (
            <svg className="spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" width="16" height="16">
              <path d="M21 12a9 9 0 11-6.219-8.56"/>
            </svg>
          ) : (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" width="16" height="16">
              <path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8"/>
              <polyline points="16 6 12 2 8 6"/>
              <line x1="12" y1="2" x2="12" y2="15"/>
            </svg>
          )}
          {sharing ? 'Generating...' : 'Share as Image'}
        </button>
        <button className="btn-ghost" onClick={onDone}>
          Done
        </button>
      </div>
    </div>
  );
}

function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.lineTo(x + w - r, y);
  ctx.quadraticCurveTo(x + w, y, x + w, y + r);
  ctx.lineTo(x + w, y + h - r);
  ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
  ctx.lineTo(x + r, y + h);
  ctx.quadraticCurveTo(x, y + h, x, y + h - r);
  ctx.lineTo(x, y + r);
  ctx.quadraticCurveTo(x, y, x + r, y);
  ctx.closePath();
}
