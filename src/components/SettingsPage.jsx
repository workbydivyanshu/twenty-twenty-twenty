import { useState } from 'react';
import { useAppSettings } from '../contexts/SettingsContext';
import { useSessionStore } from '../hooks/useSessionStore';

function Toggle({ checked, onChange, id }) {
  return (
    <label className="settings-toggle" htmlFor={id}>
      <input
        type="checkbox"
        id={id}
        checked={checked}
        onChange={e => onChange(e.target.checked)}
      />
      <div className="settings-toggle-track" />
      <div className="settings-toggle-thumb" />
    </label>
  );
}

function PermStatus({ permission }) {
  if (permission === 'granted') {
    return (
      <div className="settings-perm-status granted">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        Enabled
      </div>
    );
  }
  if (permission === 'denied') {
    return (
      <div className="settings-perm-status denied">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
        Blocked
      </div>
    );
  }
  return (
    <div className="settings-perm-status default">
      Not set
    </div>
  );
}

export default function SettingsPage() {
  const {
    settings,
    updateSetting,
    requestNotificationPermission,
    sound,
  } = useAppSettings();
  const { exportSessions, clearAllSessions } = useSessionStore('all');

  const [showClearConfirm, setShowClearConfirm] = useState(false);

  const handleTestSound = () => {
    sound.playBreakChime(settings.volume);
  };

  const handleTestNotification = async () => {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification('Break reminder test', {
        body: 'Notifications are working!',
        icon: '/favicon.svg',
      });
    }
  };

  const handleClearData = () => {
    clearAllSessions();
    setShowClearConfirm(false);
  };

  return (
    <div className="settings-view">
      <div className="settings-section" style={{ '--i': 1 }}>
        <div className="settings-section-title">Notifications</div>

        <div className="settings-item">
          <div className="settings-item-info">
            <div className="settings-item-label">Enable Notifications</div>
            <div className="settings-item-desc">Get notified when it's time for a break</div>
          </div>
          <Toggle
            id="notif-toggle"
            checked={settings.notificationsEnabled}
            onChange={v => updateSetting('notificationsEnabled', v)}
          />
        </div>

        <div className="settings-item">
          <div className="settings-item-info">
            <div className="settings-item-label">Permission Status</div>
            <div className="settings-item-desc">Browser notification permission</div>
          </div>
          <PermStatus permission={settings.notificationPermission} />
        </div>

        <div className="settings-item">
          <button className="settings-btn" onClick={requestNotificationPermission}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            {settings.notificationPermission === 'granted' ? 'Re-request Permission' : 'Request Permission'}
          </button>
        </div>

        {settings.notificationsEnabled && settings.notificationPermission === 'granted' && (
          <div className="settings-item">
            <button className="settings-btn" onClick={handleTestNotification}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="5 3 19 12 5 21 5 3"/>
              </svg>
              Test Notification
            </button>
          </div>
        )}
      </div>

      <div className="settings-section" style={{ '--i': 2 }}>
        <div className="settings-section-title">Sound</div>

        <div className="settings-item">
          <div className="settings-item-info">
            <div className="settings-item-label">Enable Sound</div>
            <div className="settings-item-desc">Play chime when a break is due</div>
          </div>
          <Toggle
            id="sound-toggle"
            checked={settings.soundEnabled}
            onChange={v => updateSetting('soundEnabled', v)}
          />
        </div>

        {settings.soundEnabled && (
          <div className="settings-item" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '10px' }}>
            <div className="settings-item-info">
              <div className="settings-item-label">Volume</div>
            </div>
            <input
              type="range"
              className="volume-slider"
              min="0"
              max="1"
              step="0.05"
              value={settings.volume}
              onChange={e => updateSetting('volume', parseFloat(e.target.value))}
            />
            <button className="settings-btn" onClick={handleTestSound} style={{ alignSelf: 'flex-start' }}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/>
                <path d="M15.54 8.46a5 5 0 0 1 0 7.07"/>
                <path d="M19.07 4.93a10 10 0 0 1 0 14.14"/>
              </svg>
              Test Sound
            </button>
          </div>
        )}
      </div>

      <div className="settings-section" style={{ '--i': 3 }}>
        <div className="settings-section-title">Break Reminders</div>

        <div className="settings-item">
          <div className="settings-item-info">
            <div className="settings-item-label">Reminder Interval</div>
            <div className="settings-item-desc">20 minutes — the optimal rest period</div>
          </div>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: '14px', color: 'var(--accent-primary)', fontWeight: 500 }}>
            20 min
          </div>
        </div>

        <div className="settings-item">
          <div className="settings-item-info">
            <div className="settings-item-label">Break Duration</div>
            <div className="settings-item-desc">How long each break lasts</div>
          </div>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: '14px', color: 'var(--accent-primary)', fontWeight: 500 }}>
            20 sec
          </div>
        </div>
      </div>

      <div className="settings-section" style={{ '--i': 4 }}>
        <div className="settings-section-title">Data</div>

        <div className="settings-item">
          <div className="settings-item-info">
            <div className="settings-item-label">Export Sessions</div>
            <div className="settings-item-desc">Download your session history as JSON</div>
          </div>
          <button className="settings-btn" onClick={exportSessions}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Export
          </button>
        </div>

        <div className="settings-item">
          <div className="settings-item-info">
            <div className="settings-item-label">Clear All Data</div>
            <div className="settings-item-desc">Delete all sessions and reset settings</div>
          </div>
          {!showClearConfirm ? (
            <button className="settings-danger-btn" onClick={() => setShowClearConfirm(true)}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
              Clear
            </button>
          ) : (
            <div style={{ display: 'flex', gap: '8px' }}>
              <button className="settings-danger-btn" onClick={handleClearData} style={{ color: 'var(--danger)', flex: 1 }}>
                Confirm
              </button>
              <button className="btn-ghost" onClick={() => setShowClearConfirm(false)} style={{ padding: '10px 16px' }}>
                Cancel
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
