import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { Haptics, ImpactStyle } from '@capacitor/haptics';
import { useTimer } from '../hooks/useTimer';
import { useBreakInterval } from '../hooks/useBreakInterval';
import { useSessionStore } from '../hooks/useSessionStore';
import { useSound } from '../hooks/useSound';
import { useNotifications } from '../hooks/useNotifications';
import { useAppSettings } from './SettingsContext';

const SessionContext = createContext(null);

export function SessionProvider({ children, activeProfileId = 'default' })
{
	const { settings } = useAppSettings();
	const volume = settings.soundEnabled ? settings.volume : 0;

	const [sessionState, setSessionState] = useState('idle');
	const [breaksTaken, setBreaksTaken] = useState(0);
	const [breaksSkipped, setBreaksSkipped] = useState(0);
	const [breaksTriggered, setBreaksTriggered] = useState(0);
	const [completedSession, setCompletedSession] = useState(null);
	const [sessionStartTime, setSessionStartTime] = useState(null);
	const [currentProfileId, setCurrentProfileId] = useState(activeProfileId);
	const [breakCountdown, setBreakCountdown] = useState(0);

	const sound = useSound();
	const notifications = useNotifications(settings);
	const { elapsed, isRunning, start, startDebug, stop, pause, resumeAfterBreak, reset, getElapsed } = useTimer();
	const { saveSession } = useSessionStore(currentProfileId);
	const countdownRef = useRef(null);
	const lastBreakElapsedRef = useRef(0);

	useEffect(() => {
		return () => {
			if (countdownRef.current) {
				clearInterval(countdownRef.current);
			}
		};
	}, []);

	const handleBreakTrigger = useCallback(() => {
		lastBreakElapsedRef.current = getElapsed();
		pause();
		setBreaksTriggered(prev => prev + 1);
		setSessionState('break_pending');
		notifications.triggerBreakNotification();
		if (settings.soundEnabled) sound.playBreakChime(volume);
		try {
			Haptics.impact({ style: ImpactStyle.Heavy });
		} catch {
			/* ignore haptics */
		}
	}, [pause, sound, settings.soundEnabled, volume, getElapsed, notifications]);

	const { nextBreakIn } = useBreakInterval(getElapsed, isRunning, handleBreakTrigger);

	const handleStartBreakCountdown = useCallback(() => {
		setBreakCountdown(20);
		setSessionState('break_active');
		countdownRef.current = setInterval(() => {
			setBreakCountdown(prev => {
				if (prev <= 1) {
					clearInterval(countdownRef.current);
					return 0;
				}
				return prev - 1;
			});
		}, 1000);
	}, []);

	const handleEndBreakCountdown = useCallback(() => {
		if (countdownRef.current) {
			clearInterval(countdownRef.current);
			countdownRef.current = null;
		}
		lastBreakElapsedRef.current = getElapsed();
		setBreakCountdown(0);
	}, [getElapsed]);

	const handleStart = useCallback(async () => {
		setCurrentProfileId(activeProfileId);
		reset();
		setBreaksTaken(0);
		setBreaksSkipped(0);
		setBreaksTriggered(0);
		setCompletedSession(null);
		setBreakCountdown(0);
		const now = Date.now();
		setSessionStartTime(now);
		setSessionState('active');
		start();
		if (settings.soundEnabled) sound.playStart(volume);
		try {
			await Haptics.impact({ style: ImpactStyle.Medium });
		} catch {
			/* ignore haptics */
		}
	}, [reset, start, sound, settings.soundEnabled, volume, activeProfileId]);

	const handleStartDebug = useCallback(async () => {
		setCurrentProfileId(activeProfileId);
		reset();
		setBreaksTaken(0);
		setBreaksSkipped(0);
		setBreaksTriggered(0);
		setCompletedSession(null);
		setBreakCountdown(0);
		const now = Date.now();
		setSessionStartTime(now);
		setSessionState('active');
		startDebug();
	}, [reset, startDebug, activeProfileId]);

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
		try {
			await Haptics.impact({ style: ImpactStyle.Heavy });
		} catch {
			/* ignore haptics */
		}
	}, [stop, sessionStartTime, breaksTriggered, breaksTaken, breaksSkipped, saveSession, sound, settings.soundEnabled, volume, currentProfileId]);

	const handleBreakTake = useCallback(() => {
		handleStartBreakCountdown();
	}, [handleStartBreakCountdown]);

	const handleBreakSkipSession = useCallback(async () => {
		await handleEnd();
	}, [handleEnd]);

	const handleBreakConfirm = useCallback(async () => {
		handleEndBreakCountdown();
		resumeAfterBreak();
		setBreaksTaken(prev => prev + 1);
		setSessionState('active');
		if (settings.soundEnabled) sound.playConfirm(volume);
		try {
			await Haptics.impact({ style: ImpactStyle.Light });
		} catch {
			/* ignore haptics */
		}
	}, [handleEndBreakCountdown, resumeAfterBreak, sound, settings.soundEnabled, volume]);

	const handleBreakSkip = useCallback(async () => {
		handleEndBreakCountdown();
		resumeAfterBreak();
		setBreaksSkipped(prev => prev + 1);
		setSessionState('active');
		if (settings.soundEnabled) sound.playSkip(volume);
		try {
			await Haptics.impact({ style: ImpactStyle.Light });
		} catch {
			/* ignore haptics */
		}
	}, [handleEndBreakCountdown, resumeAfterBreak, sound, settings.soundEnabled, volume]);

	const handleDismissSummary = useCallback(() => {
		setSessionState('idle');
		setCompletedSession(null);
		reset();
	}, [reset]);

	const value = {
		sessionState,
		elapsed,
		isRunning,
		nextBreakIn,
		breakCountdown,
		breaksTaken,
		breaksSkipped,
		breaksTriggered,
		completedSession,
		currentProfileId,
		handleStart,
		handleStartDebug,
		handleEnd,
		handleBreakTake,
		handleBreakSkipSession,
		handleBreakConfirm,
		handleBreakSkip,
		handleDismissSummary,
	};

	return (
		<SessionContext.Provider value={value}>
			{children}
		</SessionContext.Provider>
	);
}

export function useSession()
{
	const ctx = useContext(SessionContext);
	if (!ctx) throw new Error('useSession must be used within SessionProvider');
	return ctx;
}
