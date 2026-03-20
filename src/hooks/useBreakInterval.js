/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect, useRef, useCallback } from 'react';
import { BREAK_INTERVAL_MS } from '../utils/constants';

export function useBreakInterval(getElapsed, isRunning, onTrigger) {
  const [nextBreakIn, setNextBreakIn] = useState(null);
  const intervalRef = useRef(null);
  const lastTriggeredPeriodRef = useRef(0);
  const onTriggerRef = useRef(onTrigger);

  useEffect(() => {
    onTriggerRef.current = onTrigger;
  }, [onTrigger]);

  const clearInterval = useCallback(() => {
    if (intervalRef.current) {
      window.clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  }, []);

  useEffect(() => {
    if (!isRunning) {
      clearInterval();
      setNextBreakIn(null);
      return;
    }

    lastTriggeredPeriodRef.current = Math.floor(getElapsed() / BREAK_INTERVAL_MS);

    const tick = () => {
      const elapsedMs = getElapsed();
      const currentPeriod = Math.floor(elapsedMs / BREAK_INTERVAL_MS);

      if (currentPeriod > lastTriggeredPeriodRef.current) {
        lastTriggeredPeriodRef.current = currentPeriod;
        clearInterval();
        setNextBreakIn(0);
        if (onTriggerRef.current) onTriggerRef.current();
      } else {
        const remaining = BREAK_INTERVAL_MS - (elapsedMs % BREAK_INTERVAL_MS);
        setNextBreakIn(Math.ceil(remaining / 1000));
      }
    };

    intervalRef.current = window.setInterval(tick, 500);
    tick();

    return () => {
      clearInterval();
    };
  }, [isRunning, getElapsed, clearInterval]);

  return {
    nextBreakIn,
  };
}
