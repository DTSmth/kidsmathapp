import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import Layout from '../components/layout/Layout';
import GameCard from '../components/games/GameCard';
import { getGames } from '../services/games';
import type { GameDto } from '../types';
import { Gamepad2 } from 'lucide-react';

const Play = () => {
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const [games, setGames] = useState<GameDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedChild) return;
    getGames(selectedChild.id)
      .then(setGames)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [selectedChild]);

  if (!selectedChild) return null;

  return (
    <Layout>
      <div className="space-y-4">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <Gamepad2 className="w-7 h-7 text-primary" />
            Games
          </h1>
          <p className="text-sm text-gray-500 mt-1">Tap a game to start playing!</p>
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
          <div className="grid grid-cols-2 gap-3">
            {games.map(game => (
              <GameCard
                key={game.id}
                game={game}
                onClick={() => navigate(`/games/${game.id}/play`)}
              />
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default Play;
