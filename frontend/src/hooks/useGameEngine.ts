import { useState, useEffect, useRef, useCallback } from 'react';

export type GamePhase = 'intro' | 'countdown' | 'playing' | 'gameover' | 'complete';
export type MascotMood = 'excited' | 'correct' | 'wrong' | 'celebrating' | 'thinking';

export interface GameEngineState {
  phase: GamePhase;
  countdown: number;
  timeRemaining: number;
  lives: number;
  combo: number;
  score: number;
  totalAnswered: number;
  starsEarned: number;
  speedBonus: boolean;
  mascotMood: MascotMood;
  comboJustTriggered: boolean;
}

export interface GameEngineOptions {
  timeLimit: number;         // seconds
  maxLives?: number;         // default 3
  baseStarsReward: number;
  onGameOver: (result: GameResult) => void;
  onGameComplete: (result: GameResult) => void;
}

export interface GameResult {
  score: number;          // % correct (0-100)
  starsEarned: number;
  timeSpent: number;      // seconds
  comboBonus: number;
  answersLog: AnswerLogEntry[];
}

export interface AnswerLogEntry {
  questionId: number;
  answeredAt: number; // ms since game start
}

export function useGameEngine(options: GameEngineOptions) {
  const {
    timeLimit,
    maxLives = 3,
    baseStarsReward,
    onGameOver,
    onGameComplete,
  } = options;

  const [phase, setPhase] = useState<GamePhase>('intro');
  const [countdown, setCountdown] = useState(3);
  const [timeRemaining, setTimeRemaining] = useState(timeLimit);
  const [lives, setLives] = useState(maxLives);
  const [combo, setCombo] = useState(0);
  const [score, setScore] = useState(0);
  const [totalAnswered, setTotalAnswered] = useState(0);
  const [mascotMood, setMascotMood] = useState<MascotMood>('excited');
  const [comboJustTriggered, setComboJustTriggered] = useState(false);
  const [speedBonus, setSpeedBonus] = useState(false);

  const gameStartTime = useRef<number>(0);
  const lastAnswerTime = useRef<number>(0);
  const answersLog = useRef<AnswerLogEntry[]>([]);
  const maxComboReached = useRef(0);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const timedOutRef = useRef(false);

  const startTimer = useCallback(() => {
    if (timerRef.current) clearInterval(timerRef.current);
    timedOutRef.current = false;
    timerRef.current = setInterval(() => {
      setTimeRemaining(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current!);
          timedOutRef.current = true;
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Pause timer when tab goes to background
  useEffect(() => {
    const onVisibility = () => {
      if (document.hidden && phase === 'playing') {
        if (timerRef.current) clearInterval(timerRef.current);
      } else if (!document.hidden && phase === 'playing') {
        startTimer();
      }
    };
    document.addEventListener('visibilitychange', onVisibility);
    return () => document.removeEventListener('visibilitychange', onVisibility);
  }, [phase, startTimer]);

  // Countdown then start
  useEffect(() => {
    if (phase !== 'countdown') return;
    if (countdown <= 0) {
      setPhase('playing');
      gameStartTime.current = Date.now();
      lastAnswerTime.current = Date.now();
      startTimer();
      return;
    }
    const t = setTimeout(() => setCountdown(c => c - 1), 1000);
    return () => clearTimeout(t);
  }, [phase, countdown, startTimer]);

  // React to timer expiry safely outside the state setter
  useEffect(() => {
    if (timeRemaining === 0 && timedOutRef.current && phase === 'playing') {
      timedOutRef.current = false;
      endGame(false);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [timeRemaining, phase]);

  // Cleanup timer on unmount
  useEffect(() => () => { if (timerRef.current) clearInterval(timerRef.current); }, []);

  function startGame() {
    setPhase('countdown');
    setCountdown(3);
    setTimeRemaining(timeLimit);
    setLives(maxLives);
    setScore(0);
    setTotalAnswered(0);
    setCombo(0);
    setMascotMood('excited');
    answersLog.current = [];
    maxComboReached.current = 0;
  }

  function endGame(completed: boolean) {
    if (timerRef.current) clearInterval(timerRef.current);
    const timeSpent = Math.floor((Date.now() - gameStartTime.current) / 1000);
    const pctScore = totalAnswered > 0 ? Math.round((score / totalAnswered) * 100) : 0;
    const starsEarned = Math.round((pctScore / 100) * baseStarsReward) + Math.min(maxComboReached.current, Math.floor(baseStarsReward / 2));
    const result: GameResult = {
      score: pctScore,
      starsEarned,
      timeSpent,
      comboBonus: Math.min(maxComboReached.current, Math.floor(baseStarsReward / 2)),
      answersLog: answersLog.current,
    };

    if (completed) {
      setPhase('complete');
      setMascotMood('celebrating');
      onGameComplete(result);
    } else {
      setPhase('gameover');
      setMascotMood('thinking');
      onGameOver(result);
    }
  }

  const onCorrect = useCallback((questionId?: number) => {
    if (phase !== 'playing') return;
    const now = Date.now();
    const elapsed = now - lastAnswerTime.current;
    const isSpeedBonus = elapsed < 3000;
    lastAnswerTime.current = now;

    if (questionId !== undefined) {
      answersLog.current.push({ questionId, answeredAt: now - gameStartTime.current });
    }

    setScore(s => s + 1);
    setTotalAnswered(t => t + 1);
    setMascotMood('correct');
    setSpeedBonus(isSpeedBonus);

    setCombo(c => {
      const next = c + 1;
      if (next > maxComboReached.current) maxComboReached.current = next;
      if (next >= 3) {
        setComboJustTriggered(true);
        setTimeout(() => setComboJustTriggered(false), 1600);
      }
      return next;
    });

    setTimeout(() => setMascotMood('excited'), 800);
  }, [phase]);

  const onWrong = useCallback((questionId?: number) => {
    if (phase !== 'playing') return;
    lastAnswerTime.current = Date.now();

    if (questionId !== undefined) {
      answersLog.current.push({ questionId, answeredAt: Date.now() - gameStartTime.current });
    }

    setTotalAnswered(t => t + 1);
    setMascotMood('wrong');
    setCombo(0);
    setSpeedBonus(false);

    setLives(l => {
      const next = l - 1;
      if (next <= 0) {
        setTimeout(() => endGame(false), 800);
      }
      return next;
    });

    setTimeout(() => setMascotMood('excited'), 800);
  }, [phase]);

  const onAllQuestionsAnswered = useCallback(() => {
    endGame(true);
  }, [score, totalAnswered, baseStarsReward]);

  return {
    phase,
    countdown,
    timeRemaining,
    lives,
    combo,
    score,
    totalAnswered,
    mascotMood,
    comboJustTriggered,
    speedBonus,
    startGame,
    onCorrect,
    onWrong,
    onAllQuestionsAnswered,
  };
}
