import { useRef, useCallback } from 'react';

// Synthesized sounds using Web Audio API — no asset files needed
function getAudioContext(): AudioContext | null {
  try {
    const WebkitAudioContext = (window as Window & { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
    return new (window.AudioContext || WebkitAudioContext!)();
  } catch {
    return null;
  }
}

function playTone(
  ctx: AudioContext,
  frequency: number,
  duration: number,
  type: OscillatorType = 'sine',
  gainValue = 0.3
) {
  const oscillator = ctx.createOscillator();
  const gain = ctx.createGain();
  oscillator.connect(gain);
  gain.connect(ctx.destination);
  oscillator.type = type;
  oscillator.frequency.setValueAtTime(frequency, ctx.currentTime);
  gain.gain.setValueAtTime(gainValue, ctx.currentTime);
  gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);
  oscillator.start(ctx.currentTime);
  oscillator.stop(ctx.currentTime + duration);
}

export function useSoundEffects() {
  const ctxRef = useRef<AudioContext | null>(null);

  function getCtx() {
    if (!ctxRef.current) ctxRef.current = getAudioContext();
    return ctxRef.current;
  }

  // Balloon pop: short noise burst
  const playPop = useCallback(() => {
    const ctx = getCtx();
    if (!ctx) return;
    playTone(ctx, 800, 0.08, 'square', 0.4);
    setTimeout(() => playTone(ctx, 400, 0.06, 'square', 0.2), 60);
  }, []);

  // Correct answer ding
  const playDing = useCallback(() => {
    const ctx = getCtx();
    if (!ctx) return;
    playTone(ctx, 880, 0.12, 'sine', 0.25);
    setTimeout(() => playTone(ctx, 1320, 0.15, 'sine', 0.2), 100);
  }, []);

  // Wrong answer boing
  const playBoing = useCallback(() => {
    const ctx = getCtx();
    if (!ctx) return;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.type = 'sine';
    osc.frequency.setValueAtTime(300, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(100, ctx.currentTime + 0.3);
    gain.gain.setValueAtTime(0.3, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);
    osc.start(ctx.currentTime);
    osc.stop(ctx.currentTime + 0.3);
  }, []);

  // Rocket boost whoosh
  const playWhoosh = useCallback(() => {
    const ctx = getCtx();
    if (!ctx) return;
    const bufferSize = ctx.sampleRate * 0.15;
    const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) data[i] = Math.random() * 2 - 1;
    const source = ctx.createBufferSource();
    source.buffer = buffer;
    const filter = ctx.createBiquadFilter();
    filter.type = 'bandpass';
    filter.frequency.setValueAtTime(2000, ctx.currentTime);
    filter.frequency.exponentialRampToValueAtTime(500, ctx.currentTime + 0.15);
    const gain = ctx.createGain();
    gain.gain.setValueAtTime(0.2, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.15);
    source.connect(filter);
    filter.connect(gain);
    gain.connect(ctx.destination);
    source.start();
    source.stop(ctx.currentTime + 0.15);
  }, []);

  // Combo fanfare
  const playCombo = useCallback(() => {
    const ctx = getCtx();
    if (!ctx) return;
    [523, 659, 784, 1047].forEach((freq, i) => {
      setTimeout(() => playTone(ctx, freq, 0.12, 'sine', 0.2), i * 80);
    });
  }, []);

  // Victory fanfare
  const playVictory = useCallback(() => {
    const ctx = getCtx();
    if (!ctx) return;
    [523, 659, 784, 1047, 1047].forEach((freq, i) => {
      setTimeout(() => playTone(ctx, freq, 0.18, 'triangle', 0.25), i * 120);
    });
  }, []);

  return { playPop, playDing, playBoing, playWhoosh, playCombo, playVictory };
}
