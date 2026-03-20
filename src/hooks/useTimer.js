import { useState, useEffect, useRef, useCallback } from 'react';
import { TICK_MS } from '../utils/constants';

export function useTimer()
{
	const [elapsed, setElapsed] = useState(0);
	const [isRunning, setIsRunning] = useState(false);
	const startRef = useRef(null);
	const pausedRef = useRef(0);
	const tickRef = useRef(null);

	const tick = useCallback(() => {
		if (startRef.current !== null) {
			setElapsed(Date.now() - startRef.current);
		}
	}, []);

	const clear_tick = useCallback(() => {
		if (tickRef.current) {
			clearInterval(tickRef.current);
			tickRef.current = null;
		}
	}, []);

	const start = useCallback(() => {
		clear_tick();
		startRef.current = Date.now() - pausedRef.current;
		setIsRunning(true);
		tickRef.current = setInterval(tick, TICK_MS);
	}, [tick, clear_tick]);

	const startDebug = useCallback(() => {
		clear_tick();
		pausedRef.current = 19 * 60 * 1000 + 30 * 1000;
		startRef.current = Date.now() - pausedRef.current;
		setIsRunning(true);
		tickRef.current = setInterval(tick, TICK_MS);
	}, [tick, clear_tick]);

	const stop = useCallback(() => {
		clear_tick();
		if (startRef.current !== null) {
			pausedRef.current = Date.now() - startRef.current;
			startRef.current = null;
		}
		setIsRunning(false);
		return pausedRef.current;
	}, [clear_tick]);

	const pause = useCallback(() => {
		clear_tick();
		if (startRef.current !== null) {
			pausedRef.current = Date.now() - startRef.current;
			startRef.current = null;
		}
		setIsRunning(false);
	}, [clear_tick]);

	const resumeAfterBreak = useCallback(() => {
		clear_tick();
		if (startRef.current !== null) {
			pausedRef.current = Date.now() - startRef.current;
		}
		startRef.current = Date.now() - pausedRef.current;
		setIsRunning(true);
		tickRef.current = setInterval(tick, TICK_MS);
	}, [tick, clear_tick]);

	const reset = useCallback(() => {
		clear_tick();
		setIsRunning(false);
		setElapsed(0);
		pausedRef.current = 0;
		startRef.current = null;
	}, [clear_tick]);

	const getElapsed = useCallback(() => {
		if (startRef.current !== null) {
			return Date.now() - startRef.current;
		}
		return pausedRef.current;
	}, []);

	useEffect(() => {
		return () => {
			clear_tick();
		};
	}, [clear_tick]);

	return { elapsed, isRunning, start, startDebug, stop, pause, resumeAfterBreak, reset, getElapsed };
}
