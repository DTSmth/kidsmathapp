import React, { useState, useEffect } from 'react';
import type { InventoryItemDto } from '../../types';

interface Props {
  item: InventoryItemDto;
  onClose: () => void;
}

type Phase = 'shake' | 'glow' | 'reveal';

const tierGradient: Record<string, string> = {
  COMMON: 'from-gray-200 to-gray-100 border-gray-300',
  RARE: 'from-blue-200 to-blue-100 border-blue-400',
  LEGENDARY: 'from-yellow-200 to-yellow-100 border-yellow-500',
};

export const ItemChest: React.FC<Props> = ({ item, onClose }) => {
  const [phase, setPhase] = useState<Phase>('shake');

  useEffect(() => {
    const t1 = setTimeout(() => setPhase('glow'), 800);
    const t2 = setTimeout(() => setPhase('reveal'), 1600);
    return () => { clearTimeout(t1); clearTimeout(t2); };
  }, []);

  return (
    <div
      className="fixed inset-0 bg-black/60 flex items-center justify-center z-50"
      onClick={phase === 'reveal' ? onClose : undefined}
    >
      <div className="flex flex-col items-center gap-4">
        {phase !== 'reveal' ? (
          <div className={`text-8xl ${phase === 'shake' ? 'animate-bounce' : 'animate-pulse'}`}>
            🎁
          </div>
        ) : (
          <div className={`w-40 h-40 rounded-3xl border-4 bg-gradient-to-b ${tierGradient[item.tier] ?? tierGradient.COMMON} flex flex-col items-center justify-center gap-2 animate-bounce`}>
            <span className="text-6xl">{item.emoji}</span>
            <div className="text-center px-2">
              <div className="font-bold text-sm text-gray-800">{item.name}</div>
              <div className="text-xs text-gray-500 capitalize">{item.tier.toLowerCase()}</div>
            </div>
          </div>
        )}
        {phase === 'reveal' && (
          <div className="text-center">
            <div className="text-white text-xl font-bold mb-3">🎉 New Item!</div>
            <button
              onClick={onClose}
              className="px-6 py-2 bg-white text-gray-800 rounded-full font-bold text-sm hover:bg-gray-100 transition-colors"
            >
              Awesome!
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
