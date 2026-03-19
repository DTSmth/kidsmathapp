import { useState, useCallback } from 'react';
import { submitGameScore } from '../services/games';
import type { GameScoreRequest, GameScoreResult, PendingGameScore } from '../types';

const PENDING_KEY = 'pendingGameScores';

function loadPending(): PendingGameScore[] {
  try {
    return JSON.parse(localStorage.getItem(PENDING_KEY) || '[]');
  } catch {
    return [];
  }
}

function savePending(scores: PendingGameScore[]) {
  localStorage.setItem(PENDING_KEY, JSON.stringify(scores));
}

export function useGameSubmit() {
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(false);

  const submit = useCallback(async (
    gameId: number,
    request: GameScoreRequest
  ): Promise<GameScoreResult | null> => {
    setSubmitting(true);
    setSubmitError(false);
    try {
      const result = await submitGameScore(gameId, request);
      setSubmitting(false);
      return result;
    } catch {
      // Cache for retry on next app load
      const pending = loadPending();
      pending.push({ gameId, request, timestamp: Date.now() });
      savePending(pending);
      setSubmitError(true);
      setSubmitting(false);
      return null;
    }
  }, []);

  return { submit, submitting, submitError };
}

// Call this from AuthContext on app load to flush pending scores
export async function flushPendingScores() {
  const pending = loadPending();
  if (pending.length === 0) return;

  const remaining: PendingGameScore[] = [];
  for (const entry of pending) {
    // Skip entries older than 7 days
    if (Date.now() - entry.timestamp > 7 * 24 * 60 * 60 * 1000) continue;
    try {
      await submitGameScore(entry.gameId, entry.request);
    } catch {
      remaining.push(entry);
    }
  }
  savePending(remaining);
}
