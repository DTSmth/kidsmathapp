import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import Layout from '../components/layout/Layout';
import GameCard from '../components/games/GameCard';
import { Leaderboard } from '../components/engagement/Leaderboard';
import { getGames } from '../services/games';
import { getGameLeaderboard } from '../services/leaderboard';
import type { GameDto, GameLeaderboardDto, GameMode } from '../types';
import { Gamepad2, ChevronDown } from 'lucide-react';

const Play = () => {
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const [games, setGames] = useState<GameDto[]>([]);
  const [loading, setLoading] = useState(true);

  // Leaderboard state
  const [expandedGame, setExpandedGame] = useState<number | null>(null);
  const [leaderboards, setLeaderboards] = useState<Record<string, GameLeaderboardDto>>({});
  const [lbTab, setLbTab] = useState<Record<number, GameMode>>({});
  const [lbLoading, setLbLoading] = useState<Record<string, boolean>>({});

  useEffect(() => {
    if (!selectedChild) return;
    getGames(selectedChild.id)
      .then(setGames)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [selectedChild]);

  if (!selectedChild) return null;

  const cacheKey = (gameId: number, mode: GameMode) => `${gameId}_${mode}`;

  const fetchLeaderboard = async (gameId: number, mode: GameMode) => {
    const key = cacheKey(gameId, mode);
    if (leaderboards[key] || lbLoading[key]) return;
    setLbLoading(prev => ({ ...prev, [key]: true }));
    try {
      const data = await getGameLeaderboard(gameId, selectedChild.id, mode);
      setLeaderboards(prev => ({ ...prev, [key]: data }));
    } catch {
      // silently fail — panel will show empty state
    } finally {
      setLbLoading(prev => ({ ...prev, [key]: false }));
    }
  };

  const handleGameCardTap = (gameId: number) => {
    if (expandedGame === gameId) {
      setExpandedGame(null);
      return;
    }
    setExpandedGame(gameId);
    const mode = lbTab[gameId] ?? 'NORMAL';
    fetchLeaderboard(gameId, mode);
  };

  const handleTabSwitch = (gameId: number, mode: GameMode) => {
    setLbTab(prev => ({ ...prev, [gameId]: mode }));
    fetchLeaderboard(gameId, mode);
  };

  return (
    <Layout>
      <div className="space-y-4">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <Gamepad2 className="w-7 h-7 text-primary" />
            Games
          </h1>
          <p className="text-sm text-gray-500 mt-1">Tap a game to see the leaderboard!</p>
        </div>

        {/* Game grid */}
        {loading ? (
          <div className="grid grid-cols-2 gap-3">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="aspect-[5/6] rounded-2xl bg-gray-100 animate-pulse" />
            ))}
          </div>
        ) : games.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-5xl mb-3">🎮</p>
            <p className="font-semibold">No games available yet!</p>
            <p className="text-sm mt-1">Check back soon</p>
          </div>
        ) : (
          <div className="space-y-3">
            {games.map(game => {
              const isExpanded = expandedGame === game.id;
              const activeMode: GameMode = lbTab[game.id] ?? 'NORMAL';
              const key = cacheKey(game.id, activeMode);
              const lbData = leaderboards[key];
              const isLbLoading = lbLoading[key];

              return (
                <div key={game.id}>
                  {/* Game card row — full width when expanded, 2-col grid otherwise */}
                  <div
                    className="cursor-pointer relative"
                    onClick={() => handleGameCardTap(game.id)}
                  >
                    <GameCard
                      game={game}
                      onClick={() => {}} // click handled by parent div
                    />
                    {/* Chevron overlay */}
                    <div className="absolute top-3 right-3 bg-white/80 backdrop-blur rounded-full p-1 shadow-sm pointer-events-none">
                      <ChevronDown
                        className={`w-4 h-4 text-gray-500 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`}
                      />
                    </div>
                  </div>

                  {/* Expandable leaderboard panel */}
                  {isExpanded && (
                    <div className="bg-white rounded-2xl shadow-md mt-2 overflow-hidden">
                      {/* Tabs */}
                      <div className="flex bg-gray-100 rounded-t-2xl p-1 gap-1">
                        <button
                          onClick={() => handleTabSwitch(game.id, 'NORMAL')}
                          className={`flex-1 py-2 px-3 rounded-xl font-bold text-sm transition-all ${
                            activeMode === 'NORMAL'
                              ? 'bg-white shadow text-gray-800'
                              : 'text-gray-500 hover:text-gray-700'
                          }`}
                        >
                          Regular 🏆
                        </button>
                        <button
                          onClick={() => handleTabSwitch(game.id, 'ENDLESS')}
                          className={`flex-1 py-2 px-3 rounded-xl font-bold text-sm transition-all ${
                            activeMode === 'ENDLESS'
                              ? 'bg-white shadow text-gray-800'
                              : 'text-gray-500 hover:text-gray-700'
                          }`}
                        >
                          Endless Rush ⚡
                        </button>
                      </div>

                      {/* Leaderboard entries */}
                      <div className="p-4">
                        {isLbLoading ? (
                          <div className="space-y-2">
                            {[1, 2, 3].map(i => (
                              <div key={i} className="h-14 rounded-xl bg-gray-100 animate-pulse" />
                            ))}
                          </div>
                        ) : (
                          <Leaderboard
                            entries={lbData?.entries ?? []}
                            valueLabel={activeMode === 'ENDLESS' ? 'correct' : 'score'}
                            valueUnit={activeMode === 'ENDLESS' ? '' : '%'}
                            emptyMessage="No scores yet — be the first!"
                          />
                        )}
                      </div>

                      {/* Play Now button */}
                      <div className="px-4 pb-4">
                        <button
                          onClick={() => navigate(`/games/${game.id}/play`)}
                          className="w-full py-3 rounded-xl font-bold text-white text-sm shadow-md active:scale-95 transition-transform"
                          style={{ backgroundColor: '#4ECDC4' }}
                        >
                          Play Now →
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default Play;
