import { useState, useEffect } from 'react';
import { useChild } from '../context/ChildContext';
import Layout from '../components/layout/Layout';
import { getChildDashboard } from '../services/progress';
import type { AchievementDto } from '../types';
import { Trophy, Star, Lock } from 'lucide-react';

const Achievements = () => {
  const { selectedChild } = useChild();
  const [achievements, setAchievements] = useState<AchievementDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedChild) return;
    getChildDashboard(selectedChild.id)
      .then(d => setAchievements(d.recentAchievements))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [selectedChild]);

  return (
    <Layout>
      <div className="space-y-5">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Trophies</h1>
          <p className="text-sm text-gray-500 mt-1">Earn badges by completing lessons!</p>
        </div>

        {/* Stars summary */}
        {selectedChild && (
          <div className="flex items-center gap-2 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3">
            <Star className="w-5 h-5 text-amber-500 fill-current" />
            <span className="font-bold text-gray-800 text-lg">{selectedChild.totalStars}</span>
            <span className="text-gray-600 text-sm">stars earned</span>
          </div>
        )}

        {loading ? (
          <div className="space-y-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="bg-white rounded-xl border border-gray-200 p-4 animate-pulse h-20" />
            ))}
          </div>
        ) : achievements.length === 0 ? (
          <div className="text-center py-16">
            <Trophy className="w-16 h-16 text-gray-200 mx-auto mb-4" />
            <p className="font-semibold text-gray-600">No trophies yet</p>
            <p className="text-sm text-gray-400 mt-1">Complete lessons to earn your first badge!</p>
          </div>
        ) : (
          <div className="space-y-3">
            {achievements.map(a => (
              <div
                key={a.id}
                className={`bg-white rounded-xl border p-4 flex items-center gap-4 ${
                  a.earned ? 'border-amber-200 bg-amber-50/40' : 'border-gray-200 opacity-60'
                }`}
              >
                <div className={`w-12 h-12 rounded-full flex items-center justify-center shrink-0 ${
                  a.earned ? 'bg-amber-100' : 'bg-gray-100'
                }`}>
                  {a.earned ? (
                    <Trophy className="w-6 h-6 text-amber-500" />
                  ) : (
                    <Lock className="w-5 h-5 text-gray-400" />
                  )}
                </div>
                <div>
                  <p className="font-semibold text-gray-800 text-sm">{a.name}</p>
                  <p className="text-xs text-gray-500">{a.description}</p>
                  {a.unlockedAt && (
                    <p className="text-xs text-amber-600 mt-0.5">
                      Earned {new Date(a.unlockedAt).toLocaleDateString()}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default Achievements;
