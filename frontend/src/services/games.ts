import apiClient from './client';
import type { GameDto, GameDetailDto, GameScoreRequest, GameScoreResult } from '../types';

export const getGames = (childId: number): Promise<GameDto[]> =>
  apiClient.get<{ data: GameDto[] }>(`/games?childId=${childId}`).then(r => r.data.data);

export const getGameDetail = (gameId: number, childId: number): Promise<GameDetailDto> =>
  apiClient.get<{ data: GameDetailDto }>(`/games/${gameId}?childId=${childId}`).then(r => r.data.data);

export const submitGameScore = (gameId: number, request: GameScoreRequest): Promise<GameScoreResult> =>
  apiClient.post<{ data: GameScoreResult }>(`/games/${gameId}/score`, request).then(r => r.data.data);
