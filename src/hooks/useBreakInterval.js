/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect, useRef, useCallback } from 'react';
import { BREAK_INTERVAL_MS } from '../utils/constants';

export function useBreakInterval(getElapsed, isRunning, onTrigger) {
  const [nextBreakIn, setNextBreakIn] = useState(null);
  const [breakCountdown, setBreakCountdown] = useState(20);
  const [isBreakActive, setIsBreakActive] = useState(false);

  const intervalRef = useRef(null);
  const countdownRef = useRef(null);

  const endBreak = useCallback(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    if (countdownRef.current) clearInterval(countdownRef.current);
    countdownRef.current = null;
    setIsBreakActive(false);
    setBreakCountdown(20);
  }, []);

  useEffect(() => {
    if (!isRunning) {
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (countdownRef.current) clearInterval(countdownRef.current);
      intervalRef.current = null;
      countdownRef.current = null;
      setNextBreakIn(null);
      setIsBreakActive(false);
      setBreakCountdown(20);
      return;
    }

    const tick = () => {
      const elapsedMs = getElapsed();
      const breakPosition = elapsedMs % BREAK_INTERVAL_MS;
      const remaining = BREAK_INTERVAL_MS - breakPosition;

      if (remaining <= 0) {
        if (intervalRef.current) clearInterval(intervalRef.current);
        intervalRef.current = null;
        setIsBreakActive(true);
        setBreakCountdown(20);
        if (onTrigger) onTrigger();

        countdownRef.current = setInterval(() => {
          setBreakCountdown(prev => {
            if (prev <= 1) {
              clearInterval(countdownRef.current);
              countdownRef.current = null;
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } else {
        setNextBreakIn(Math.ceil(remaining / 1000));
      }
    };

    intervalRef.current = setInterval(tick, 500);
    tick();

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (countdownRef.current) clearInterval(countdownRef.current);
    };
  }, [isRunning, getElapsed, onTrigger]);

  return {
    isBreakActive,
    breakCountdown,
    nextBreakIn,
    endBreak,
  };
}
