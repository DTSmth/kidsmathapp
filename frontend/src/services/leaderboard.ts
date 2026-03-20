import api from './api';
import type { FamilyLeaderboardDto, GameLeaderboardDto, GameMode } from '../types';

export const getFamilyLeaderboard = (childId: number): Promise<FamilyLeaderboardDto> =>
  api.get('/leaderboard/family', { params: { childId } }).then(r => r.data.data);

export const getGameLeaderboard = (gameId: number, childId: number, mode: GameMode = 'NORMAL'): Promise<GameLeaderboardDto> =>
  api.get(`/leaderboard/games/${gameId}`, { params: { childId, mode } }).then(r => r.data.data);
