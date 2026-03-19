import { useState, useEffect } from 'react';
import AmbientBg from './components/AmbientBg';
import Nav from './components/Nav';
import SessionBadge from './components/SessionBadge';
import BreakStats from './components/BreakStats';
import StartEndButton from './components/StartEndButton';
import BreakOverlay from './components/BreakOverlay';
import SessionSummary from './components/SessionSummary';
import RecapDashboard from './components/RecapDashboard';
import SettingsPage from './components/SettingsPage';
import Onboarding from './components/Onboarding';
import ProfileSelector from './components/ProfileSelector';
import { SessionProvider, useSession } from './contexts/SessionContext';
import { SettingsProvider, useAppSettings } from './contexts/SettingsContext';

function formatNextBreak(seconds) {
  if (seconds === null) return null;
  if (seconds >= 60) {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}m ${s}s`;
  }
  return `${seconds}s`;
}

function formatDuration(ms) {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  }
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
}

function AppShell() {
  const [view, setView] = useState('home');
  const { getActiveProfile, settings } = useAppSettings();
  const activeProfile = getActiveProfile();
  const [showProfilePicker, setShowProfilePicker] = useState(false);

  return (
    <SessionProvider activeProfileId={settings.activeProfileId}>
      <div className="app">
        <AmbientBg />
        <Nav
          view={view}
          onViewChange={setView}
          activeProfile={activeProfile}
          onProfileClick={() => setShowProfilePicker(true)}
        />
        <main className="main">
          {view === 'home' && <HomeView />}
          {view === 'recap' && <RecapDashboard />}
          {view === 'settings' && <SettingsPage />}
        </main>
        <BreakOverlayLayer />
        {showProfilePicker && (
          <ProfileSelector onClose={() => setShowProfilePicker(false)} />
        )}
      </div>
    </SessionProvider>
  );
}

function HomeView() {
  const {
    sessionState, elapsed, isRunning, nextBreakIn,
    breaksTaken, breaksSkipped, completedSession,
    handleStart, handleEnd, handleDismissSummary,
  } = useSession();

  if (sessionState === 'summary' && completedSession) {
    return (
      <div className="home-view">
        <SessionSummary session={completedSession} onDone={handleDismissSummary} />
      </div>
    );
  }

  return (
    <div className="home-view">
      <div className="timer-section">
        <div className={`timer-display ${isRunning ? 'running' : ''}`} role="timer" aria-live="polite">
          {formatDuration(elapsed)}
        </div>
        <SessionBadge state={sessionState} />
      </div>

      <BreakStats taken={breaksTaken} skipped={breaksSkipped} />

      {isRunning && nextBreakIn !== null && (
        <div className="next-break">
          Next break in <span>{formatNextBreak(nextBreakIn)}</span>
        </div>
      )}

      <div className="action-section">
        <StartEndButton
          isRunning={isRunning}
          onStart={handleStart}
          onEnd={handleEnd}
        />
      </div>
    </div>
  );
}

function BreakOverlayLayer() {
  const {
    sessionState, breakCountdown,
    handleBreakTake, handleBreakSkipSession,
    handleBreakConfirm, handleBreakSkip, handleEnd
  } = useSession();

  if (sessionState !== 'break_pending' && sessionState !== 'break_active') return null;

  return (
    <BreakOverlay
      sessionState={sessionState}
      countdown={breakCountdown}
      onTakeBreak={handleBreakTake}
      onSkipSession={handleBreakSkipSession}
      onConfirm={handleBreakConfirm}
      onSkip={handleBreakSkip}
      onEndSession={handleEnd}
    />
  );
}

export default function App() {
  const [showOnboarding, setShowOnboarding] = useState(() => {
    try {
      const saved = localStorage.getItem('twenty-v2-settings');
      if (!saved) return true;
      const parsed = JSON.parse(saved);
      return !parsed.onboardingComplete;
    } catch {
      return true;
    }
  });

  useEffect(() => {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('/sw.js').catch(() => {});
    }
  }, []);

  return (
    <SettingsProvider>
      <AppShell />
      {showOnboarding && <Onboarding onComplete={() => setShowOnboarding(false)} />}
    </SettingsProvider>
  );
}
