/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect, useRef, useCallback } from 'react';
import { BREAK_INTERVAL_MS } from '../utils/constants';

export function useBreakInterval(getElapsed, isRunning, onTrigger)
{
	const [nextBreakIn, setNextBreakIn] = useState(null);
	const intervalRef = useRef(null);
	const lastTriggeredPeriodRef = useRef(0);
	const onTriggerRef = useRef(onTrigger);

	useEffect(() => {
		onTriggerRef.current = onTrigger;
	}, [onTrigger]);

	const clear_interval = useCallback(() => {
		if (intervalRef.current) {
			window.clearInterval(intervalRef.current);
			intervalRef.current = null;
		}
	}, []);

	useEffect(() => {
		if (!isRunning) {
			clear_interval();
			setNextBreakIn(null);
			return;
		}

		lastTriggeredPeriodRef.current = Math.floor(getElapsed() / BREAK_INTERVAL_MS);

		const tick = () => {
			const elapsed_ms = getElapsed();
			const current_period = Math.floor(elapsed_ms / BREAK_INTERVAL_MS);

			if (current_period > lastTriggeredPeriodRef.current) {
				lastTriggeredPeriodRef.current = current_period;
				clear_interval();
				setNextBreakIn(0);
				if (onTriggerRef.current) {
					onTriggerRef.current();
				}
			} else {
				const remaining = BREAK_INTERVAL_MS - (elapsed_ms % BREAK_INTERVAL_MS);
				setNextBreakIn(Math.ceil(remaining / 1000));
			}
		};

		intervalRef.current = window.setInterval(tick, 500);
		tick();

		return () => {
			clear_interval();
		};
	}, [isRunning, getElapsed, clear_interval]);

	return {
		nextBreakIn,
	};
}
