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
    if (queue.current.length === 0) {
      processing.current = false;
      return;
    }
    setVisible(prev => {
      if (prev.length >= 2) return prev;
      const next = queue.current.shift();
      if (!next) return prev;
      return [...prev, { id: `${next.id}-${Date.now()}`, achievement: next }];
    });
    setTimeout(processNext, 1500);
  }, []); // stable — visible.length read inside setState callback

  useEffect(() => {
    if (achievements.length > 0) {
      queue.current = [...achievements];
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
