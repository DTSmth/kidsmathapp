import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import Layout from '../components/layout/Layout';
import DailyChallengeBanner from '../components/common/DailyChallengeBanner';
import { getChildDashboard } from '../services/progress';
import { Star, Flame, Trophy, Gamepad2 } from 'lucide-react';
import type { DashboardDto, GameDto, StreakCalendarDto, FamilyLeaderboardDto, InventoryItemDto } from '../types';
import { getGames } from '../services/games';
import { GAME_CONFIG } from '../components/games/gameConfig';
import { StreakCalendar } from '../components/engagement/StreakCalendar';
import { RankBadge } from '../components/engagement/RankBadge';
import { ItemChest } from '../components/engagement/ItemChest';
import { Leaderboard } from '../components/engagement/Leaderboard';
import { claimDailyBonus } from '../services/inventory';
import { getFamilyLeaderboard } from '../services/leaderboard';
import api from '../services/api';

const Dashboard = () => {
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState<DashboardDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [games, setGames] = useState<GameDto[]>([]);

  // Streak calendar
  const [streakCalendar, setStreakCalendar] = useState<StreakCalendarDto | null>(null);

  // Daily bonus
  const [dailyBonusClaiming, setDailyBonusClaiming] = useState(false);
  const [dailyBonusClaimed, setDailyBonusClaimed] = useState(false);
  const [newItem, setNewItem] = useState<InventoryItemDto | null>(null);

  // Leaderboard
  const [leaderboard, setLeaderboard] = useState<FamilyLeaderboardDto | null>(null);
  const [leaderboardTab, setLeaderboardTab] = useState<'stars' | 'streaks'>('stars');

  useEffect(() => {
    if (!selectedChild) return;
    setLoading(true);
    getChildDashboard(selectedChild.id)
      .then(setDashboard)
      .catch(() => {})
      .finally(() => setLoading(false));
    getGames(selectedChild.id)
      .then(data => setGames(data.slice(0, 3)))
      .catch(() => {});

    // Fetch streak calendar
    api.get(`/streaks/${selectedChild.id}/calendar`)
      .then(r => setStreakCalendar(r.data.data))
      .catch(() => {});

    // Fetch family leaderboard
    getFamilyLeaderboard(selectedChild.id)
      .then(setLeaderboard)
      .catch(() => {});
  }, [selectedChild]);

  const handleClaimDailyBonus = async () => {
    if (!selectedChild || dailyBonusClaiming) return;
    setDailyBonusClaiming(true);
    try {
      const result = await claimDailyBonus(selectedChild.id);
      setDailyBonusClaimed(true);
      if (result.itemGranted && result.item) {
        setNewItem(result.item);
      }
      // Refresh streak calendar after claiming
      api.get(`/streaks/${selectedChild.id}/calendar`)
        .then(r => setStreakCalendar(r.data.data))
        .catch(() => {});
    } catch {
      // silently fail
    } finally {
      setDailyBonusClaiming(false);
    }
  };

  if (!selectedChild) return null;

  const streak = selectedChild.currentStreak;

  // Determine if daily bonus is available
  const todayEntry = streakCalendar?.days.find(d => d.isToday);
  const showDailyBonusBanner =
    !dailyBonusClaimed &&
    todayEntry !== undefined &&
    !todayEntry.dailyBonusClaimed;

  return (
    <Layout>
      {/* Item chest modal */}
      {newItem && (
        <ItemChest item={newItem} onClose={() => setNewItem(null)} />
      )}

      <div className="space-y-6">
        {/* Welcome + nav buttons */}
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Hi, {selectedChild.name}! 👋</h1>
            {streak > 0 && (
              <p className="text-sm text-gray-500 mt-1">You're on a {streak} day streak — keep it up!</p>
            )}
          </div>
          <div className="flex gap-2 shrink-0">
            <button
              onClick={() => navigate('/collector')}
              className="text-xl hover:scale-110 transition-transform"
              title="Collector's Album"
            >
              🏆
            </button>
            <button
              onClick={() => navigate('/avatar')}
              className="text-xl hover:scale-110 transition-transform"
              title="Customize Avatar"
            >
              ✨
            </button>
          </div>
        </div>

        {/* Daily bonus banner */}
        {showDailyBonusBanner && (
          <button
            onClick={handleClaimDailyBonus}
            disabled={dailyBonusClaiming}
            className="w-full bg-gradient-to-r from-yellow-400 to-orange-400 text-white rounded-2xl px-5 py-4 flex items-center justify-between shadow-md hover:opacity-90 transition-opacity disabled:opacity-60"
          >
            <div className="text-left">
              <p className="font-bold text-lg">🎁 Claim Daily Bonus!</p>
              <p className="text-sm opacity-90">Practice today and earn a surprise item</p>
            </div>
            <span className="text-2xl">{dailyBonusClaiming ? '⏳' : '→'}</span>
          </button>
        )}

        {/* Stats */}
        <div className="flex gap-3">
          <div className="flex items-center gap-2 bg-amber-50 border border-amber-200 px-4 py-3 rounded-xl flex-1 justify-center">
            <Star className="w-5 h-5 text-amber-500 fill-current" />
            <span className="font-bold text-gray-800 text-xl">{selectedChild.totalStars}</span>
            <span className="text-sm text-gray-500">stars</span>
          </div>
          {streak > 0 && (
            <div className="flex items-center gap-2 bg-orange-50 border border-orange-200 px-4 py-3 rounded-xl flex-1 justify-center">
              <Flame className="w-5 h-5 text-orange-500" />
              <span className="font-bold text-gray-800 text-xl">{streak}</span>
              <span className="text-sm text-gray-500">day streak</span>
            </div>
          )}
        </div>

        {/* Rank Badge */}
        {dashboard?.rankLevel && (
          <RankBadge
            rankLevel={dashboard.rankLevel}
            emoji={dashboard.rankLevelEmoji}
            starsToNextRank={dashboard.starsToNextRank}
            totalStars={dashboard.totalStars}
          />
        )}

        {/* Streak Calendar */}
        {streakCalendar && (
          <StreakCalendar data={streakCalendar} />
        )}

        {/* Daily challenge */}
        {dashboard && (
          <DailyChallengeBanner
            complete={dashboard.dailyChallengeComplete}
            onStartChallenge={() => navigate('/topics')}
          />
        )}

        {/* Play CTA */}
        <button
          onClick={() => navigate('/play')}
          className="w-full bg-primary text-white rounded-2xl px-5 py-4 flex items-center justify-between shadow-md hover:bg-primary-dark transition-colors"
        >
          <div className="text-left">
            <p className="font-bold text-lg">Play & Learn</p>
            <p className="text-primary-light text-sm opacity-90">Games and lessons</p>
          </div>
          <Gamepad2 className="w-6 h-6" />
        </button>

        {/* Practice CTA */}
        <button
          onClick={() => navigate('/practice')}
          className="w-full bg-accent text-gray-800 rounded-2xl px-5 py-4 flex items-center justify-between shadow-md hover:bg-accent/80 transition-colors"
        >
          <div className="text-left">
            <p className="font-bold text-lg">Practice Mode</p>
            <p className="text-gray-600 text-sm opacity-90">Quick generated exercises</p>
          </div>
          <span className="text-2xl">🎯</span>
        </button>

        {/* Games preview */}
        {games.length > 0 && (
          <div>
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-base font-semibold text-gray-800">🎮 Games</h2>
              <button
                onClick={() => navigate('/play')}
                className="text-primary text-sm font-medium hover:underline"
              >
                See all
              </button>
            </div>
            <div className="flex gap-3 overflow-x-auto pb-1">
              {games.map(game => {
                const cfg = GAME_CONFIG[game.gameType];
                return (
                  <button
                    key={game.id}
                    onClick={() => navigate(`/games/${game.id}/play`)}
                    className="shrink-0 flex flex-col items-center gap-1.5 bg-white border border-gray-200 rounded-2xl px-4 py-3 hover:border-primary transition-colors"
                  >
                    <span className="text-3xl">{cfg.emoji}</span>
                    <p className="text-xs font-semibold text-gray-700 text-center max-w-[72px] leading-tight">{game.name}</p>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* Recent achievements */}
        {!loading && dashboard && dashboard.recentAchievements.length > 0 && (
          <div>
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-base font-semibold text-gray-800">Recent trophies</h2>
              <button
                onClick={() => navigate('/achievements')}
                className="text-primary text-sm font-medium hover:underline"
              >
                See all
              </button>
            </div>
            <div className="flex gap-3 overflow-x-auto pb-1">
              {dashboard.recentAchievements.slice(0, 4).map(a => (
                <div
                  key={a.id}
                  className="flex flex-col items-center gap-1.5 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 shrink-0"
                >
                  <Trophy className="w-6 h-6 text-amber-500" />
                  <p className="text-xs font-semibold text-gray-700 text-center max-w-[72px]">{a.name}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Topic progress summary */}
        {!loading && dashboard && dashboard.topics.length > 0 && (
          <div>
            <h2 className="text-base font-semibold text-gray-800 mb-3">Your progress</h2>
            <div className="space-y-2">
              {dashboard.topics.filter(t => t.lessonsCompleted > 0).slice(0, 3).map(t => (
                <div key={t.topicId} className="bg-white rounded-xl border border-gray-200 px-4 py-3 flex items-center gap-3">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-800">{t.topicName}</p>
                    <p className="text-xs text-gray-500">{t.lessonsCompleted}/{t.totalLessons} lessons</p>
                  </div>
                  <span className="text-sm font-semibold text-primary">{Math.round(t.percentComplete)}%</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Family Leaderboard */}
        {leaderboard && (
          <div>
            <h2 className="text-base font-semibold text-gray-800 mb-3">👨‍👩‍👧 Family Leaderboard</h2>
            <div className="flex gap-2 mb-3">
              <button
                onClick={() => setLeaderboardTab('stars')}
                className={`px-4 py-2 rounded-xl text-sm font-bold transition-all
                  ${leaderboardTab === 'stars'
                    ? 'bg-amber-400 text-white shadow-md'
                    : 'bg-white text-gray-600 border border-gray-200 hover:border-amber-300'}
                `}
              >
                ⭐ Stars
              </button>
              <button
                onClick={() => setLeaderboardTab('streaks')}
                className={`px-4 py-2 rounded-xl text-sm font-bold transition-all
                  ${leaderboardTab === 'streaks'
                    ? 'bg-orange-400 text-white shadow-md'
                    : 'bg-white text-gray-600 border border-gray-200 hover:border-orange-300'}
                `}
              >
                🔥 Streaks
              </button>
            </div>
            {leaderboardTab === 'stars' ? (
              <Leaderboard
                entries={leaderboard.starRankings}
                valueLabel="stars"
                valueUnit="⭐"
                emptyMessage="No star rankings yet!"
              />
            ) : (
              <Leaderboard
                entries={leaderboard.streakRankings}
                valueLabel="day streak"
                valueUnit="🔥"
                emptyMessage="No streak rankings yet!"
              />
            )}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default Dashboard;
