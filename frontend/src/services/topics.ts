import apiClient from './client';
import type { TopicWithProgressDto, LessonWithProgressDto } from '../types';

export const getTopicsWithProgress = (childId: number): Promise<TopicWithProgressDto[]> =>
  apiClient.get<{ data: TopicWithProgressDto[] }>(`/topics?childId=${childId}`).then(r => r.data.data);

export const getLessonsForTopic = (topicId: number, childId: number): Promise<LessonWithProgressDto[]> =>
  apiClient.get<{ data: LessonWithProgressDto[] }>(`/topics/${topicId}/lessons?childId=${childId}`).then(r => r.data.data);
