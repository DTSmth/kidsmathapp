import { useEffect } from 'react';

interface AchievementToastProps {
  id: string;
  name: string;
  description: string;
  onDismiss: (id: string) => void;
}

const AchievementToast = ({ id, name, description, onDismiss }: AchievementToastProps) => {
  useEffect(() => {
    const timer = setTimeout(() => onDismiss(id), 3000);
    return () => clearTimeout(timer);
  }, [id, onDismiss]);

  return (
    <div
      className="animate-slide-up bg-white rounded-3xl border-4 border-accent shadow-xl px-5 py-4 flex items-center gap-4 cursor-pointer w-80"
      onClick={() => onDismiss(id)}
      role="status"
      aria-live="polite"
    >
      <span className="text-4xl">🏆</span>
      <div>
        <p className="font-extrabold text-gray-800 text-sm">Achievement Unlocked!</p>
        <p className="font-bold text-gray-700">{name}</p>
        <p className="text-xs text-gray-500">{description}</p>
      </div>
    </div>
  );
};

export default AchievementToast;
