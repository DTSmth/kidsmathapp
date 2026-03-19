import type { GameType } from '../../types';

export const GAME_CONFIG: Record<GameType, { emoji: string; color: string; gradient: string; gradeLabel: string }> = {
  NUMBER_POP: {
    emoji: '🎈',
    color: '#FF6B6B',
    gradient: 'linear-gradient(135deg, #FF6B6B 0%, #FF4757 100%)',
    gradeLabel: 'K-2',
  },
  COUNTING_CRITTERS: {
    emoji: '🐾',
    color: '#4ECDC4',
    gradient: 'linear-gradient(135deg, #4ECDC4 0%, #26a69a 100%)',
    gradeLabel: 'K-1',
  },
  SHAPE_SAFARI: {
    emoji: '🔺',
    color: '#A084E8',
    gradient: 'linear-gradient(135deg, #A084E8 0%, #7C3AED 100%)',
    gradeLabel: 'K-2',
  },
  MATH_RACE: {
    emoji: '🚀',
    color: '#4A90D9',
    gradient: 'linear-gradient(135deg, #4A90D9 0%, #1565C0 100%)',
    gradeLabel: '2-5',
  },
  PATTERN_PARADE: {
    emoji: '🎨',
    color: '#F59E0B',
    gradient: 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
    gradeLabel: '1-3',
  },
};
