import { useState, useCallback, useEffect, useRef } from 'react';
import AchievementToast from './AchievementToast';
import type { AchievementDto } from '../../types';

interface ToastQueueProps {
  achievements: AchievementDto[];
}

interface ToastItem {
  id: string;
  achievement: AchievementDto;
}

const ToastQueue = ({ achievements }: ToastQueueProps) => {
  const [visible, setVisible] = useState<ToastItem[]>([]);
  const queue = useRef<AchievementDto[]>([]);
  const processing = useRef(false);

  const processNext = useCallback(() => {
    if (queue.current.length === 0 || visible.length >= 2) {
      processing.current = false;
      return;
    }
    const next = queue.current.shift()!;
    const item: ToastItem = {
      id: `${next.id}-${Date.now()}`,
      achievement: next,
    };
    setVisible(prev => [...prev, item]);
    setTimeout(processNext, 1500);
  }, [visible.length]);

  useEffect(() => {
    if (achievements.length > 0) {
      queue.current.push(...achievements);
      if (!processing.current) {
        processing.current = true;
        processNext();
      }
    }
  }, [achievements, processNext]);

  const dismiss = useCallback((id: string) => {
    setVisible(prev => prev.filter(t => t.id !== id));
  }, []);

  if (visible.length === 0) return null;

  return (
    <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 flex flex-col gap-3 items-center">
      {visible.map(item => (
        <AchievementToast
          key={item.id}
          id={item.id}
          name={item.achievement.name}
          description={item.achievement.description || ''}
          onDismiss={dismiss}
        />
      ))}
    </div>
  );
};

export default ToastQueue;
