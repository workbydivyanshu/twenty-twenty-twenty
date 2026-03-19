import { createContext, useContext, useState, useCallback } from 'react';
import { Haptics, ImpactStyle } from '@capacitor/haptics';
import { useTimer } from '../hooks/useTimer';
import { useBreakInterval } from '../hooks/useBreakInterval';
import { useSessionStore } from '../hooks/useSessionStore';
import { useSound } from '../hooks/useSound';
import { useAppSettings } from './SettingsContext';

const SessionContext = createContext(null);

export function SessionProvider({ children, activeProfileId = 'default' }) {
  const { settings } = useAppSettings();
  const volume = settings.soundEnabled ? settings.volume : 0;

  const [sessionState, setSessionState] = useState('idle');
  const [breaksTaken, setBreaksTaken] = useState(0);
  const [breaksSkipped, setBreaksSkipped] = useState(0);
  const [breaksTriggered, setBreaksTriggered] = useState(0);
  const [completedSession, setCompletedSession] = useState(null);
  const [sessionStartTime, setSessionStartTime] = useState(null);
  const [currentProfileId, setCurrentProfileId] = useState(activeProfileId);

  const sound = useSound();
  const { elapsed, isRunning, start, stop, reset, getElapsed } = useTimer();
  const { saveSession } = useSessionStore(currentProfileId);

  const handleBreakTrigger = useCallback(() => {
    setBreaksTriggered(prev => prev + 1);
    if (settings.soundEnabled) sound.playBreakChime(volume);
    try { Haptics.impact({ style: ImpactStyle.Heavy }); } catch { /* ignore haptics */ }
  }, [sound, settings.soundEnabled, volume]);

  const { isBreakActive, breakCountdown, nextBreakIn, endBreak } = useBreakInterval(
    getElapsed,
    isRunning,
    handleBreakTrigger
  );

  const handleStart = useCallback(async () => {
    setCurrentProfileId(activeProfileId);
    reset();
    setBreaksTaken(0);
    setBreaksSkipped(0);
    setBreaksTriggered(0);
    setCompletedSession(null);
    const now = Date.now();
    setSessionStartTime(now);
    setSessionState('active');
    start();
    if (settings.soundEnabled) sound.playStart(volume);
    try { await Haptics.impact({ style: ImpactStyle.Medium }); } catch { /* ignore haptics */ }
  }, [reset, start, sound, settings.soundEnabled, volume, activeProfileId]);

  const handleEnd = useCallback(async () => {
    const finalElapsed = stop();
    const endTime = new Date().toISOString();
    const startTime = sessionStartTime ? new Date(sessionStartTime).toISOString() : new Date(Date.now() - finalElapsed).toISOString();

    const session = {
      startTime,
      endTime,
      durationMs: finalElapsed,
      breaksTriggered,
      breaksTaken,
      breaksSkipped,
    };

    setCompletedSession({ ...session, profileId: currentProfileId });
    saveSession(session, currentProfileId);
    setSessionState('summary');
    if (settings.soundEnabled) sound.playEnd(volume);
    try { await Haptics.impact({ style: ImpactStyle.Heavy }); } catch { /* ignore haptics */ }
  }, [stop, sessionStartTime, breaksTriggered, breaksTaken, breaksSkipped, saveSession, sound, settings.soundEnabled, volume, currentProfileId]);

  const handleBreakConfirm = useCallback(async () => {
    endBreak();
    setBreaksTaken(prev => prev + 1);
    setSessionState('active');
    if (settings.soundEnabled) sound.playConfirm(volume);
    try { await Haptics.impact({ style: ImpactStyle.Light }); } catch { /* ignore haptics */ }
  }, [endBreak, sound, settings.soundEnabled, volume]);

  const handleBreakSkip = useCallback(async () => {
    endBreak();
    setBreaksSkipped(prev => prev + 1);
    setSessionState('active');
    if (settings.soundEnabled) sound.playSkip(volume);
    try { await Haptics.impact({ style: ImpactStyle.Light }); } catch { /* ignore haptics */ }
  }, [endBreak, sound, settings.soundEnabled, volume]);

  const handleBreakEnter = useCallback(() => {
    setSessionState('break');
  }, []);

  const handleDismissSummary = useCallback(() => {
    setSessionState('idle');
    setCompletedSession(null);
    reset();
  }, [reset]);

  const value = {
    sessionState,
    elapsed,
    isRunning,
    isBreakActive,
    breakCountdown,
    nextBreakIn,
    breaksTaken,
    breaksSkipped,
    breaksTriggered,
    completedSession,
    currentProfileId,
    handleStart,
    handleEnd,
    handleBreakConfirm,
    handleBreakSkip,
    handleBreakEnter,
    handleDismissSummary,
  };

  return (
    <SessionContext.Provider value={value}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error('useSession must be used within SessionProvider');
  return ctx;
}
