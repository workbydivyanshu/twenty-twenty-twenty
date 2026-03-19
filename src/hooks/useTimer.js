import { useState, useEffect, useRef, useCallback } from 'react';
import { TICK_MS } from '../utils/constants';

export function useTimer() {
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

  tickRef.current = tick;

  const start = useCallback(() => {
    startRef.current = Date.now() - pausedRef.current;
    setIsRunning(true);
    tickRef.current = setInterval(tick, TICK_MS);
  }, [tick]);

  const stop = useCallback(() => {
    if (tickRef.current) {
      clearInterval(tickRef.current);
      tickRef.current = null;
    }
    if (startRef.current !== null) {
      pausedRef.current = Date.now() - startRef.current;
    }
    setIsRunning(false);
    return pausedRef.current;
  }, []);

  const pause = useCallback(() => {
    if (tickRef.current) {
      clearInterval(tickRef.current);
      tickRef.current = null;
    }
    if (startRef.current !== null) {
      pausedRef.current = Date.now() - startRef.current;
    }
    setIsRunning(false);
  }, []);

  const resumeAfterBreak = useCallback(() => {
    if (startRef.current !== null) {
      pausedRef.current = Date.now() - startRef.current;
    }
    startRef.current = Date.now() - pausedRef.current;
    setIsRunning(true);
    tickRef.current = setInterval(tick, TICK_MS);
  }, [tick]);

  const reset = useCallback(() => {
    if (tickRef.current) {
      clearInterval(tickRef.current);
      tickRef.current = null;
    }
    setIsRunning(false);
    setElapsed(0);
    pausedRef.current = 0;
    startRef.current = null;
  }, []);

  const getElapsed = useCallback(() => {
    if (startRef.current !== null) {
      return Date.now() - startRef.current;
    }
    return pausedRef.current;
  }, []);

  useEffect(() => {
    return () => {
      if (tickRef.current) {
        clearInterval(tickRef.current);
      }
    };
  }, []);

  return { elapsed, isRunning, start, stop, pause, resumeAfterBreak, reset, getElapsed };
}
