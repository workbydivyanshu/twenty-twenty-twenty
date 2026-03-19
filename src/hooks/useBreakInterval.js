/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect, useRef, useCallback } from 'react';
import { BREAK_INTERVAL_MS } from '../utils/constants';

export function useBreakInterval(getElapsed, isRunning, onTrigger) {
  const [nextBreakIn, setNextBreakIn] = useState(null);
  const intervalRef = useRef(null);

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

    const tick = () => {
      const elapsedMs = getElapsed();
      const breakPosition = elapsedMs % BREAK_INTERVAL_MS;
      const remaining = BREAK_INTERVAL_MS - breakPosition;

      if (remaining <= 0) {
        clearInterval();
        setNextBreakIn(0);
        if (onTrigger) onTrigger();
      } else {
        setNextBreakIn(Math.ceil(remaining / 1000));
      }
    };

    intervalRef.current = window.setInterval(tick, 500);
    tick();

    return () => {
      clearInterval();
    };
  }, [isRunning, getElapsed, onTrigger, clearInterval]);

  return {
    nextBreakIn,
  };
}
