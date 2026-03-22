import api from './api';

export interface PracticeQuestion {
  id: number;
  questionText: string;
  options: string[];
  correctAnswer: string;
}

export interface PracticeSession {
  sessionToken: string;
  questions: PracticeQuestion[];
}

export interface PracticeResult {
  score: number;
  correctCount: number;
  totalCount: number;
  starsEarned: number;
  newItem?: { name: string; emoji: string; tier: string };
}

export async function generatePractice(childId: number, topicType: string, count = 10): Promise<PracticeSession> {
  const res = await api.post('/practice/generate', { childId, topicType, count });
  return res.data.data;
}

export async function submitPractice(
  sessionToken: string,
  answers: { questionIndex: number; answer: string }[]
): Promise<PracticeResult> {
  const res = await api.post('/practice/submit', { sessionToken, answers });
  return res.data.data;
}
