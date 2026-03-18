import { useState } from 'react';
import { useSessionStore } from '../hooks/useSessionStore';
import { useAppSettings } from '../contexts/SettingsContext';
import { formatDurationLong } from '../utils/dateUtils';

function StatCard({ value, label, delay = 0, accent }) {
  return (
    <div className="recap-card" style={{ '--i': delay }}>
      <div className="recap-card-value" style={accent ? { color: accent } : {}}>{value}</div>
      <div className="recap-card-label">{label}</div>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="recap-empty">
      <div className="recap-empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 6v6l4 2"/>
        </svg>
      </div>
      <div className="recap-empty-title">No sessions yet</div>
      <div className="recap-empty-text">
        Start your first digital session to see your wellness recap here.
      </div>
    </div>
  );
}

function StreakCalendar({ days }) {
  const weeks = [];
  let currentWeek = [];
  days.forEach((day, i) => {
    currentWeek.push(day);
    if (day.isWeekStart || i === days.length - 1) {
      weeks.push(currentWeek);
      currentWeek = [];
    }
  });

  const dayLabels = ['', 'M', '', 'W', '', 'F', ''];

  return (
    <div className="streak-calendar">
      <div className="streak-calendar-label">Activity</div>
      <div className="streak-calendar-grid">
        <div className="streak-day-labels">
          {dayLabels.map((label, i) => (
            <div key={i} className="streak-day-label">{label}</div>
          ))}
        </div>
        <div className="streak-weeks">
          {weeks.map((week, wi) => (
            <div key={wi} className="streak-week">
              {week.map((day, di) => (
                <div
                  key={di}
                  className={`streak-day ${day.hasSession ? 'active' : ''} ${day.isToday ? 'today' : ''}`}
                  title={day.date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                />
              ))}
            </div>
          ))}
        </div>
      </div>
      <div className="streak-legend">
        <span>Less</span>
        <div className="streak-day" />
        <div className="streak-day active" />
        <span>More</span>
      </div>
    </div>
  );
}

function ComparisonChart({ label, current, previous, formatFn }) {
  const cur = current || { count: 0, totalTime: 0 };
  const prev = previous || { count: 0, totalTime: 0 };
  const maxTime = Math.max(cur.totalTime, prev.totalTime, 1);

  const curWidth = (cur.totalTime / maxTime) * 100;
  const prevWidth = (prev.totalTime / maxTime) * 100;

  const timeChange = prev.totalTime > 0
    ? Math.round(((cur.totalTime - prev.totalTime) / prev.totalTime) * 100)
    : cur.totalTime > 0 ? 100 : 0;

  const countChange = prev.count > 0
    ? Math.round(((cur.count - prev.count) / prev.count) * 100)
    : cur.count > 0 ? 100 : 0;

  return (
    <div className="comparison-chart">
      <div className="comparison-header">
        <span className="comparison-label">{label}</span>
        {timeChange !== 0 && (
          <span className={`comparison-change ${timeChange >= 0 ? 'up' : 'down'}`}>
            {timeChange >= 0 ? '+' : ''}{timeChange}%
          </span>
        )}
      </div>
      <div className="comparison-bars">
        <div className="comparison-bar-row">
          <span className="comparison-bar-label">This {label.toLowerCase()}</span>
          <div className="comparison-bar-track">
            <div className="comparison-bar-fill current" style={{ width: `${curWidth}%` }} />
          </div>
          <span className="comparison-bar-value">{formatFn(cur.totalTime)}</span>
        </div>
        <div className="comparison-bar-row">
          <span className="comparison-bar-label">Last {label.toLowerCase()}</span>
          <div className="comparison-bar-track">
            <div className="comparison-bar-fill previous" style={{ width: `${prevWidth}%` }} />
          </div>
          <span className="comparison-bar-value">{formatFn(prev.totalTime)}</span>
        </div>
      </div>
      <div className="comparison-stats">
        <span className="comparison-stat">
          <span className="comparison-stat-val">{cur.count}</span> sessions
          {countChange !== 0 && (
            <span className={`comparison-stat-change ${countChange >= 0 ? 'up' : 'down'}`}>
              {countChange >= 0 ? '+' : ''}{countChange}%
            </span>
          )}
        </span>
        <span className="comparison-stat">
          <span className="comparison-stat-val">{formatFn(cur.totalTime)}</span> total
        </span>
      </div>
    </div>
  );
}

function BestDayCard({ bestDay }) {
  if (!bestDay) return null;

  const maxCount = Math.max(...bestDay.allDays.map(d => d.count), 1);

  return (
    <div className="best-day-card">
      <div className="best-day-header">
        <div className="best-day-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" width="18" height="18">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" fill="var(--warning)" stroke="var(--warning)" />
          </svg>
        </div>
        <div>
          <div className="best-day-title">Best Day</div>
          <div className="best-day-value">{bestDay.day}</div>
        </div>
        <div className="best-day-count">{bestDay.count} sessions</div>
      </div>
      <div className="best-day-chart">
        {bestDay.allDays.map((day, i) => (
          <div key={i} className="best-day-bar-wrap" title={`${day.name}: ${day.count} sessions`}>
            <div
              className={`best-day-bar ${day.count === maxCount && maxCount > 0 ? 'top' : ''}`}
              style={{ height: `${Math.max((day.count / maxCount) * 100, 4)}%` }}
            />
            <div className="best-day-bar-label">{day.shortName.slice(0, 1)}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function RecapDashboard() {
  const [tab, setTab] = useState('week');
  const { settings } = useAppSettings();
  const { getFiltered, getPreviousPeriod, computeStats, getStreakCalendar, getBestDayOfWeek } = useSessionStore(settings.activeProfileId);

  const sessions = getFiltered(tab);
  const prevSessions = getPreviousPeriod(tab);
  const stats = computeStats(sessions);
  const prevStats = computeStats(prevSessions);
  const streakDays = getStreakCalendar(12);
  const bestDay = getBestDayOfWeek();

  return (
    <div className="recap-view">
      <div className="recap-header">
        <div className="recap-title">Your Recap</div>
        <div className="recap-tabs">
          {['week', 'month', 'year'].map(t => (
            <button
              key={t}
              className={`recap-tab ${tab === t ? 'active' : ''}`}
              onClick={() => setTab(t)}
            >
              {t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>
      </div>

      {sessions.length === 0 && prevSessions.length === 0 ? (
        <EmptyState />
      ) : (
        <>
          {sessions.length > 0 && (
            <>
              <StreakCalendar days={streakDays} />

              <div className="recap-section-title">Overview</div>
              <div className="recap-overview">
                <StatCard value={stats.count} label="Sessions" delay={1} />
                <StatCard value={formatDurationLong(stats.totalTime)} label="Total Time" delay={2} />
                <StatCard value={formatDurationLong(stats.longestSession)} label="Longest Session" delay={3} />
                <StatCard value={formatDurationLong(stats.avgDuration)} label="Avg Session" delay={4} />
                <StatCard value={stats.totalBreaksTaken} label="Breaks Taken" delay={5} />
                <StatCard
                  value={stats.streakDays > 0 ? `${stats.streakDays}d` : '—'}
                  label="Streak"
                  delay={6}
                  accent={stats.streakDays > 0 ? 'var(--warning)' : undefined}
                />
              </div>

              {tab !== 'year' && (
                <>
                  <div className="recap-section-title">vs Previous {tab.charAt(0).toUpperCase() + tab.slice(1)}</div>
                  <ComparisonChart
                    label={tab.charAt(0).toUpperCase() + tab.slice(1)}
                    current={stats}
                    previous={prevStats}
                    formatFn={formatDurationLong}
                  />
                </>
              )}

              {bestDay && (
                <>
                  <div className="recap-section-title">Insights</div>
                  <BestDayCard bestDay={bestDay} />
                </>
              )}

              <div className="recap-compliance">
                <div className="compliance-header">
                  <span className="compliance-label">Compliance Rate</span>
                  <span className="compliance-value">{Math.round(stats.complianceRate * 100)}%</span>
                </div>
                <div className="compliance-bar">
                  <div className="compliance-fill" style={{ width: `${stats.complianceRate * 100}%` }} />
                </div>
              </div>

              <div className="recap-sessions">
                <div className="recap-sessions-title">Recent Sessions</div>
                {sessions.slice(0, 15).map((session, i) => (
                  <div key={session.id} className="recap-session-item" style={{ animationDelay: `${i * 0.05}s` }}>
                    <div className="recap-session-left">
                      <div className="recap-session-date">
                        {new Date(session.startTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                      </div>
                      <div className="recap-session-time">
                        {new Date(session.startTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
                      </div>
                    </div>
                    <div className="recap-session-right">
                      <div className="recap-session-dur">{formatDurationLong(session.durationMs)}</div>
                      <div className="recap-session-breaks">
                        {session.breaksTaken} / {session.breaksTriggered}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {sessions.length === 0 && prevSessions.length > 0 && (
            <div className="recap-no-current">
              <div className="recap-no-current-text">
                No sessions this {tab}. Checked last {tab} for comparison above.
              </div>
              <div className="recap-prev-summary">
                <StatCard value={prevStats.count} label="Sessions (Last)" delay={1} />
                <StatCard value={formatDurationLong(prevStats.totalTime)} label="Time (Last)" delay={2} />
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
