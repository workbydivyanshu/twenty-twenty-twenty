import { useState, useEffect, useRef, useCallback } from 'react';
import { TICK_MS } from '../utils/constants';

export function useTimer() {
  const [elapsed, setElapsed] = useState(0);
  const [isRunning, setIsRunning] = useState(false);
  const startRef = useRef(null);
  const pausedRef = useRef(0);
  const frameRef = useRef(null);
  const tickRef = useRef(null);

  const tick = useCallback(() => {
    if (startRef.current !== null) {
      setElapsed(performance.now() - startRef.current);
      frameRef.current = requestAnimationFrame(tickRef.current);
    }
  }, []);

  tickRef.current = tick;

  const start = useCallback(() => {
    startRef.current = performance.now() - pausedRef.current;
    setIsRunning(true);
    frameRef.current = requestAnimationFrame(tickRef.current);
  }, []);

  const stop = useCallback(() => {
    if (frameRef.current) {
      cancelAnimationFrame(frameRef.current);
      frameRef.current = null;
    }
    if (startRef.current !== null) {
      pausedRef.current = performance.now() - startRef.current;
    }
    setIsRunning(false);
    return pausedRef.current;
  }, []);

  const reset = useCallback(() => {
    if (frameRef.current) {
      cancelAnimationFrame(frameRef.current);
      frameRef.current = null;
    }
    setIsRunning(false);
    setElapsed(0);
    pausedRef.current = 0;
    startRef.current = null;
  }, []);

  const getElapsed = useCallback(() => {
    if (isRunning && startRef.current !== null) {
      return performance.now() - startRef.current;
    }
    return pausedRef.current;
  }, [isRunning]);

  useEffect(() => {
    return () => {
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current);
      }
    };
  }, []);

  return { elapsed, isRunning, start, stop, reset, getElapsed };
}
