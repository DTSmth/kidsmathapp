import React from 'react';
import type { RankLevel } from '../../types';

interface Props {
  rankLevel: RankLevel;
  emoji: string;
  starsToNextRank: number | null;
  totalStars: number;
  compact?: boolean;
}

const rankColors: Record<RankLevel, string> = {
  STARTER: 'bg-gray-100 text-gray-700 border-gray-300',
  EXPLORER: 'bg-green-100 text-green-700 border-green-300',
  CHAMPION: 'bg-blue-100 text-blue-700 border-blue-300',
  WIZARD: 'bg-purple-100 text-purple-700 border-purple-300',
  LEGEND: 'bg-yellow-100 text-yellow-700 border-yellow-400',
};

export const RankBadge: React.FC<Props> = ({ rankLevel, emoji, starsToNextRank, totalStars, compact }) => {
  if (compact) {
    return (
      <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold border ${rankColors[rankLevel]}`}>
        {emoji} {rankLevel}
      </span>
    );
  }

  return (
    <div className={`rounded-2xl p-4 border-2 ${rankColors[rankLevel]} flex items-center justify-between`}>
      <div className="flex items-center gap-3">
        <span className="text-3xl">{emoji}</span>
        <div>
          <div className="font-bold text-lg">{rankLevel}</div>
          <div className="text-sm opacity-75">{totalStars} ⭐ total stars</div>
        </div>
      </div>
      {starsToNextRank !== null ? (
        <div className="text-right text-sm opacity-75">
          <div>{starsToNextRank} ⭐ to</div>
          <div>next rank</div>
        </div>
      ) : (
        <div className="text-sm font-bold opacity-75">MAX RANK 👑</div>
      )}
    </div>
  );
};
