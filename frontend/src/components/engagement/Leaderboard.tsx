import React from 'react';
import type { LeaderboardEntryDto } from '../../types';

interface Props {
  entries: LeaderboardEntryDto[];
  valueLabel: string;
  valueUnit?: string;
  emptyMessage?: string;
}

export const Leaderboard: React.FC<Props> = ({ entries, valueLabel, valueUnit = '', emptyMessage }) => {
  if (entries.length === 0) {
    return (
      <div className="text-center py-8 text-gray-400">
        <div className="text-4xl mb-2">🏆</div>
        <div>{emptyMessage ?? 'No scores yet!'}</div>
      </div>
    );
  }

  const medalFor = (rank: number) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `#${rank}`;
  };

  return (
    <div className="space-y-2">
      {entries.map(entry => (
        <div
          key={entry.childId}
          className={`flex items-center gap-3 p-3 rounded-xl transition-all
            ${entry.isCurrentChild
              ? 'bg-teal-50 border-2 border-teal-400'
              : 'bg-gray-50 border border-gray-200'}
          `}
        >
          <div className="text-lg font-bold w-8 text-center">{medalFor(entry.rank)}</div>
          <div className="flex-1 min-w-0">
            <div className={`font-semibold truncate ${entry.isCurrentChild ? 'text-teal-700' : 'text-gray-800'}`}>
              {entry.childName}{entry.isCurrentChild ? ' (You)' : ''}
            </div>
            <div className="text-xs text-gray-500">🔥 {entry.currentStreak} day streak</div>
          </div>
          <div className="text-right flex-shrink-0">
            <div className="font-bold text-gray-800">{entry.value} {valueUnit}</div>
            <div className="text-xs text-gray-500">{valueLabel}</div>
          </div>
        </div>
      ))}
    </div>
  );
};
