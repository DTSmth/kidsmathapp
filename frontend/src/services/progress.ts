import apiClient from './client';
import type { DashboardDto, AnswerResultDto } from '../types';

export const getChildDashboard = (childId: number): Promise<DashboardDto> =>
  apiClient.get<{ data: DashboardDto }>(`/progress/dashboard/${childId}`).then(r => r.data.data);

export const checkAnswer = (questionId: number, answer: string): Promise<AnswerResultDto> =>
  apiClient.post<{ data: AnswerResultDto }>(`/questions/${questionId}/check`, { answer }).then(r => r.data.data);
