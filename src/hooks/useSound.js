import { useRef, useCallback } from 'react';

export function useSound() {
  const ctxRef = useRef(null);

  const getCtx = useCallback(() => {
    if (!ctxRef.current) {
      try {
        ctxRef.current = new (window.AudioContext || window.webkitAudioContext)();
      } catch {
        return null;
      }
    }
    if (ctxRef.current.state === 'suspended') {
      ctxRef.current.resume();
    }
    return ctxRef.current;
  }, []);

  const playTone = useCallback((freq, duration, type = 'sine', gainVal = 0.3, volume = 1) => {
    const ctx = getCtx();
    if (!ctx) return;

    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.type = type;
    osc.frequency.setValueAtTime(freq, ctx.currentTime);

    const gv = gainVal * volume;
    gain.gain.setValueAtTime(0, ctx.currentTime);
    gain.gain.linearRampToValueAtTime(gv, ctx.currentTime + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);

    osc.start(ctx.currentTime);
    osc.stop(ctx.currentTime + duration + 0.05);
  }, [getCtx]);

  const playBreakChime = useCallback((volume = 0.7) => {
    const ctx = getCtx();
    if (!ctx) return;

    const now = ctx.currentTime;

    const osc1 = ctx.createOscillator();
    const osc2 = ctx.createOscillator();
    const gain = ctx.createGain();

    osc1.connect(gain);
    osc2.connect(gain);
    gain.connect(ctx.destination);

    osc1.type = 'sine';
    osc2.type = 'sine';
    osc1.frequency.setValueAtTime(528, now);
    osc1.frequency.exponentialRampToValueAtTime(440, now + 0.3);
    osc1.frequency.exponentialRampToValueAtTime(528, now + 0.6);
    osc2.frequency.setValueAtTime(660, now + 0.1);
    osc2.frequency.exponentialRampToValueAtTime(528, now + 0.4);

    const gv = 0.25 * volume;
    gain.gain.setValueAtTime(0, now);
    gain.gain.linearRampToValueAtTime(gv, now + 0.05);
    gain.gain.exponentialRampToValueAtTime(gv * 0.6, now + 0.3);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 1.2);

    osc1.start(now);
    osc1.stop(now + 1.5);
    osc2.start(now + 0.1);
    osc2.stop(now + 1.5);
  }, [getCtx]);

  const playConfirm = useCallback((volume = 0.7) => {
    playTone(660, 0.15, 'sine', 0.25, volume);
    setTimeout(() => playTone(880, 0.2, 'sine', 0.25, volume), 100);
  }, [playTone]);

  const playStart = useCallback((volume = 0.7) => {
    playTone(523, 0.15, 'sine', 0.2, volume);
    setTimeout(() => playTone(659, 0.15, 'sine', 0.2, volume), 120);
    setTimeout(() => playTone(784, 0.2, 'sine', 0.2, volume), 240);
  }, [playTone]);

  const playEnd = useCallback((volume = 0.7) => {
    playTone(784, 0.15, 'sine', 0.2, volume);
    setTimeout(() => playTone(659, 0.15, 'sine', 0.2, volume), 120);
    setTimeout(() => playTone(523, 0.25, 'sine', 0.2, volume), 240);
  }, [playTone]);

  const playSkip = useCallback((volume = 0.5) => {
    playTone(220, 0.1, 'triangle', 0.15, volume);
  }, [playTone]);

  return { playBreakChime, playConfirm, playStart, playEnd, playSkip };
}
