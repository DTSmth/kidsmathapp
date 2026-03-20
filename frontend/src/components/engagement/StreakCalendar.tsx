import React from 'react';
import type { StreakCalendarDto } from '../../types';

interface Props {
  data: StreakCalendarDto;
}

const milestones = [
  { days: 100, label: '💯 Century', color: 'text-yellow-600' },
  { days: 30, label: '🌟 Month', color: 'text-purple-600' },
  { days: 14, label: '⚡ Two Weeks', color: 'text-blue-600' },
  { days: 7, label: '🔥 One Week', color: 'text-orange-600' },
  { days: 3, label: '✨ Starter', color: 'text-green-600' },
];

export const StreakCalendar: React.FC<Props> = ({ data }) => {
  const { currentStreak, longestStreak, days } = data;
  const last7 = days.slice(-7);
  const todayPracticed = last7.find(d => d.isToday)?.practiced ?? false;
  const milestone = milestones.find(m => currentStreak >= m.days);
  const dayLabels = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];

  return (
    <div className="bg-white rounded-2xl p-4 shadow-md">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <span className="text-2xl">🔥</span>
          <div>
            <span className="text-xl font-bold text-gray-800">{currentStreak} day streak</span>
            {milestone && (
              <div className={`text-xs font-semibold ${milestone.color}`}>{milestone.label}</div>
            )}
          </div>
        </div>
        {longestStreak > 0 && (
          <div className="text-right text-xs text-gray-500">
            <div>Best</div>
            <div className="font-bold text-gray-700">{longestStreak} days</div>
          </div>
        )}
      </div>

      <div className="grid grid-cols-7 gap-1 mb-2">
        {last7.map((day) => {
          const date = new Date(day.date);
          const dayLabel = dayLabels[date.getDay()];
          return (
            <div key={day.date} className="flex flex-col items-center gap-1">
              <div className="text-[10px] text-gray-400 font-medium">{dayLabel}</div>
              <div
                className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold transition-all
                  ${day.isToday ? 'ring-2 ring-offset-1 ring-orange-400' : ''}
                  ${day.practiced
                    ? 'bg-orange-400 text-white shadow-sm'
                    : day.isToday
                    ? 'bg-orange-100 text-orange-300'
                    : 'bg-gray-100 text-gray-300'}
                `}
              >
                {day.practiced ? '✓' : day.isToday ? '•' : ''}
              </div>
            </div>
          );
        })}
      </div>

      {currentStreak === 0 && !todayPracticed && (
        <p className="text-center text-sm text-gray-500 mt-1">✨ Start your first streak today!</p>
      )}
      {currentStreak > 0 && !todayPracticed && (
        <p className="text-center text-sm text-orange-500 font-semibold mt-1 animate-pulse">
          ⚠️ Practice today to keep your streak!
        </p>
      )}
    </div>
  );
};
