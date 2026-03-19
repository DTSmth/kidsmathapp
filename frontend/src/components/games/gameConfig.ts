import type { GameType } from '../../types';

export const GAME_CONFIG: Record<GameType, { emoji: string; color: string; gradient: string; gradeLabel: string }> = {
  NUMBER_POP: {
    emoji: '🎈',
    color: '#FF6B6B',
    gradient: 'from-coral/30 to-coral/10',
    gradeLabel: 'K-2',
  },
  COUNTING_CRITTERS: {
    emoji: '🐾',
    color: '#4ECDC4',
    gradient: 'from-primary/30 to-primary/10',
    gradeLabel: 'K-1',
  },
  SHAPE_SAFARI: {
    emoji: '🔺',
    color: '#A084E8',
    gradient: 'from-purple/30 to-purple/10',
    gradeLabel: 'K-2',
  },
  MATH_RACE: {
    emoji: '🚀',
    color: '#4A90D9',
    gradient: 'from-blue-400/30 to-blue-400/10',
    gradeLabel: '2-5',
  },
  PATTERN_PARADE: {
    emoji: '🎨',
    color: '#E6CF5A',
    gradient: 'from-accent/50 to-accent/20',
    gradeLabel: '1-3',
  },
};
