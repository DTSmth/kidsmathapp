export interface User {
  id: number;
  email: string;
  role: 'PARENT' | 'ADMIN';
}

export interface Child {
  id: number;
  name: string;
  avatarId: string;
  gradeLevel: GradeLevel;
  totalStars: number;
  currentStreak: number;
}

export type GradeLevel = 'KINDERGARTEN' | 'GRADE_1' | 'GRADE_2' | 'GRADE_3' | 'GRADE_4' | 'GRADE_5';

export interface Topic {
  id: number;
  name: string;
  description: string;
  iconName: string;
  progress: number;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface ChildSummaryDto {
  id: number;
  name: string;
  avatarId: string;
  gradeLevel: GradeLevel;
  totalStars: number;
  currentStreak: number;
}

export interface CreateChildRequest {
  name: string;
  avatarId: string;
  gradeLevel: GradeLevel;
  birthDate?: string;
}

export type AvatarId = 'leo' | 'ollie' | 'bella' | 'max';

export const AVATARS: { id: AvatarId; emoji: string; name: string }[] = [
  { id: 'leo', emoji: '🦁', name: 'Leo' },
  { id: 'ollie', emoji: '🦉', name: 'Ollie' },
  { id: 'bella', emoji: '🐰', name: 'Bella' },
  { id: 'max', emoji: '🐵', name: 'Max' },
];

export const GRADE_LEVELS: { value: GradeLevel; label: string }[] = [
  { value: 'KINDERGARTEN', label: 'Kindergarten' },
  { value: 'GRADE_1', label: 'Grade 1' },
  { value: 'GRADE_2', label: 'Grade 2' },
  { value: 'GRADE_3', label: 'Grade 3' },
  { value: 'GRADE_4', label: 'Grade 4' },
  { value: 'GRADE_5', label: 'Grade 5' },
];

// Topic with progress
export interface TopicWithProgressDto {
  id: number;
  name: string;
  description: string;
  iconName: string;
  lessonsCompleted: number;
  totalLessons: number;
  progressPercent: number;
  isUnlocked: boolean;
}

// Lesson with progress
export interface LessonWithProgressDto {
  id: number;
  title: string;
  description: string;
  orderIndex: number;
  starsReward: number;
  topicId: number;
  topicName: string;
  completed: boolean;
  score: number | null;
  completedAt: string | null;
}

// Question
export interface QuestionDto {
  id: number;
  questionText: string;
  questionType: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'FREE_TEXT';
  options: string[];
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  imageUrl: string | null;
  correctAnswer: string;
}

// Lesson detail
export type LessonMode = 'STANDARD' | 'STORY' | 'VISUAL_BUILDER';

export interface LessonDetailDto {
  id: number;
  title: string;
  description: string;
  orderIndex: number;
  starsReward: number;
  topicId: number;
  topicName: string;
  content: string;
  lessonMode: LessonMode;
  questions: QuestionDto[];
}

// Answer submission
export interface AnswerSubmissionDto {
  questionId: number;
  answer: string;
}

// Lesson submission request
export interface LessonSubmissionRequest {
  childId: number;
  answers: AnswerSubmissionDto[];
}

// Answer result (per-question check)
export interface AnswerResultDto {
  questionId: number;
  correct: boolean;
  correctAnswer: string;
  message: string;
}

// Achievement
export interface AchievementDto {
  id: number;
  name: string;
  description: string;
  badgeImageUrl: string | null;
  unlockedAt: string | null;
  earned: boolean;
}

// Lesson submission result
export interface LessonSubmissionResult {
  lessonId: number;
  totalQuestions: number;
  correctAnswers: number;
  score: number;
  passed: boolean;
  message: string;
  results: AnswerResultDto[];
  starsEarned: number;
  bonusStars: number;
  totalStars: number;
  newAchievements: AchievementDto[];
  streakUpdated: boolean;
  currentStreak: number;
}

// Topic progress for dashboard
export interface TopicProgressDto {
  topicId: number;
  topicName: string;
  lessonsCompleted: number;
  totalLessons: number;
  percentComplete: number;
}

// Game types
export type GameType = 'NUMBER_POP' | 'COUNTING_CRITTERS' | 'SHAPE_SAFARI' | 'MATH_RACE' | 'PATTERN_PARADE';

export interface GameDto {
  id: number;
  name: string;
  description: string;
  gameType: GameType;
  iconName: string;
  baseStarsReward: number;
  timeLimit: number;
  personalBestScore: number | null;
  personalBestStars: number | null;
}

export interface GameDetailDto {
  id: number;
  name: string;
  description: string;
  gameType: GameType;
  baseStarsReward: number;
  timeLimit: number;
  questions: QuestionDto[];
}

export interface GameScoreRequest {
  childId: number;
  score: number;
  timeSpent: number;
  comboBonus: number;
  answersLog?: string;
}

export interface GameScoreResult {
  score: number;
  starsEarned: number;
  personalBestScore: number;
  isNewPersonalBest: boolean;
  streakUpdated: boolean;
  newAchievements: AchievementDto[];
  gamificationApplied: boolean;
}

export interface PendingGameScore {
  gameId: number;
  request: GameScoreRequest;
  timestamp: number;
}

// Dashboard DTO
export interface DashboardDto {
  childId: number;
  childName: string;
  totalStars: number;
  currentStreak: number;
  topics: TopicProgressDto[];
  recentAchievements: AchievementDto[];
  dailyChallengeComplete: boolean;
}
