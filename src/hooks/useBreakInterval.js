/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect, useRef, useCallback } from 'react';
import { BREAK_INTERVAL_MS } from '../utils/constants';

export function useBreakInterval(isRunning, onTrigger) {
  const [nextBreakIn, setNextBreakIn] = useState(null);
  const [breakCountdown, setBreakCountdown] = useState(20);
  const [isBreakActive, setIsBreakActive] = useState(false);
  const lastBreakRef = useRef(null);
  const intervalRef = useRef(null);
  const countdownRef = useRef(null);
  const hiddenAtRef = useRef(null);

  const trigger = useCallback(() => {
    setIsBreakActive(true);
    setBreakCountdown(20);
    if (onTrigger) onTrigger();
  }, [onTrigger]);

  const endBreak = useCallback(() => {
    setIsBreakActive(false);
    setBreakCountdown(20);
    lastBreakRef.current = Date.now();
  }, []);

  const clearIntervals = useCallback(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    if (countdownRef.current) clearInterval(countdownRef.current);
  }, []);

  useEffect(() => {
    if (!isRunning) {
      clearIntervals();
      setNextBreakIn(null);
      setIsBreakActive(false);
      setBreakCountdown(20);
      lastBreakRef.current = Date.now();
      return;
    }

    if (hiddenAtRef.current !== null) {
      const hiddenDuration = Date.now() - hiddenAtRef.current;
      lastBreakRef.current += hiddenDuration;
      hiddenAtRef.current = null;
    }
    if (lastBreakRef.current === null) {
      lastBreakRef.current = Date.now();
    }

    intervalRef.current = setInterval(() => {
      const now = Date.now();
      const since = now - lastBreakRef.current;
      const remaining = BREAK_INTERVAL_MS - since;

      if (remaining <= 0) {
        trigger();
      } else {
        setNextBreakIn(Math.ceil(remaining / 1000));
      }
    }, 500);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isRunning, trigger, clearIntervals]);

  useEffect(() => {
    const handleVisibility = () => {
      if (document.hidden) {
        hiddenAtRef.current = Date.now();
        clearIntervals();
      } else if (isRunning && !isBreakActive) {
        lastBreakRef.current = Date.now();
        intervalRef.current = setInterval(() => {
          const now = Date.now();
          const since = now - lastBreakRef.current;
          const remaining = BREAK_INTERVAL_MS - since;
          if (remaining <= 0) trigger();
          else setNextBreakIn(Math.ceil(remaining / 1000));
        }, 500);
      }
    };

    document.addEventListener('visibilitychange', handleVisibility);
    return () => document.removeEventListener('visibilitychange', handleVisibility);
  }, [isRunning, isBreakActive, trigger, clearIntervals]);

  useEffect(() => {
    if (isBreakActive) {
      countdownRef.current = setInterval(() => {
        setBreakCountdown(prev => {
          if (prev <= 1) {
            clearInterval(countdownRef.current);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else {
      if (countdownRef.current) {
        clearInterval(countdownRef.current);
        countdownRef.current = null;
      }
    }
    return () => {
      if (countdownRef.current) clearInterval(countdownRef.current);
    };
  }, [isBreakActive]);

  return {
    isBreakActive,
    breakCountdown,
    nextBreakIn,
    endBreak,
    trigger,
  };
}
