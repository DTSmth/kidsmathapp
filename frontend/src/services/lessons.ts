import apiClient from './client';
import type { LessonDetailDto, LessonSubmissionRequest, LessonSubmissionResult } from '../types';

export const getLessonDetail = (lessonId: number): Promise<LessonDetailDto> =>
  apiClient.get<{ data: LessonDetailDto }>(`/lessons/${lessonId}`).then(r => r.data.data);

export const submitLesson = (lessonId: number, request: LessonSubmissionRequest): Promise<LessonSubmissionResult> =>
  apiClient.post<{ data: LessonSubmissionResult }>(`/lessons/${lessonId}/submit`, request).then(r => r.data.data);
