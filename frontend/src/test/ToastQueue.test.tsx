import { render, screen, fireEvent } from '@testing-library/react';
import ToastQueue from '../components/gamification/ToastQueue';
import type { AchievementDto } from '../types';

const makeAchievement = (id: number): AchievementDto => ({
  id,
  name: `Achievement ${id}`,
  description: 'Test',
  badgeImageUrl: null,
  unlockedAt: null,
  earned: true,
});

describe('ToastQueue', () => {
  it('shows first toast immediately', () => {
    render(<ToastQueue achievements={[makeAchievement(1)]} />);
    expect(screen.getByText('Achievement 1')).toBeInTheDocument();
  });

  it('dismisses toast when clicked', () => {
    render(<ToastQueue achievements={[makeAchievement(1)]} />);
    // Click the toast to dismiss it
    const toast = screen.getByRole('status');
    fireEvent.click(toast);
    expect(screen.queryByText('Achievement 1')).not.toBeInTheDocument();
  });
});
