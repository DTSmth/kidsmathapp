import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import Card from '../components/common/Card';
import Avatar from '../components/common/Avatar';
import { listChildren } from '../services/children';
import { getChildDashboard } from '../services/progress';
import type { Child, DashboardDto } from '../types';

interface ChildWithDashboard {
  child: Child;
  dashboard: DashboardDto | null;
}

const ParentDashboard = () => {
  const [data, setData] = useState<ChildWithDashboard[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listChildren()
      .then(async (children: Child[]) => {
        const withDashboards = await Promise.all(
          children.map(async (child) => {
            try {
              const dashboard = await getChildDashboard(child.id);
              return { child, dashboard };
            } catch {
              return { child, dashboard: null };
            }
          })
        );
        setData(withDashboards);
      })
      .catch(() => setError('Could not load your learners.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="space-y-6">
        <h1 className="text-3xl font-extrabold text-gray-800">👨‍👩‍👧‍👦 Your Learners</h1>

        {error && <p className="text-coral">{error}</p>}

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[1, 2].map(i => (
              <div
                key={i}
                className="bg-white rounded-3xl shadow-lg p-6 border-4 border-gray-200 animate-pulse h-40"
              />
            ))}
          </div>
        ) : data.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-6xl mb-4">🐣</p>
            <p className="text-xl font-bold text-gray-800 mb-2">No learners yet!</p>
            <p className="text-gray-600">Add a child profile to get started.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {data.map(({ child, dashboard }) => (
              <Card key={child.id} color="primary" className="space-y-4">
                {/* Child header */}
                <div className="flex items-center gap-4">
                  <Avatar avatarId={child.avatarId} size="lg" />
                  <div>
                    <h2 className="text-xl font-extrabold text-gray-800">{child.name}</h2>
                    <p className="text-sm text-gray-500 capitalize">
                      {child.gradeLevel?.replace('_', ' ') || 'Grade not set'}
                    </p>
                  </div>
                </div>

                {dashboard ? (
                  <div className="flex gap-6">
                    <div className="text-center">
                      <p className="text-3xl font-extrabold text-gray-800">{dashboard.totalStars}</p>
                      <p className="text-sm text-gray-500">⭐ stars</p>
                    </div>
                    <div className="text-center">
                      <p className="text-3xl font-extrabold text-gray-800">{dashboard.currentStreak}</p>
                      <p className="text-sm text-gray-500">🔥 streak</p>
                    </div>
                    <div className="text-center">
                      <p className="text-3xl font-extrabold text-gray-800">
                        {dashboard.topics.filter(t => t.percentComplete > 0).length}
                      </p>
                      <p className="text-sm text-gray-500">📚 topics</p>
                    </div>
                  </div>
                ) : (
                  <p className="text-gray-500 text-sm">No activity yet — start learning! 🚀</p>
                )}

                {dashboard?.recentAchievements?.length ? (
                  <div>
                    <p className="text-sm font-semibold text-gray-600 mb-2">Recent achievements:</p>
                    <div className="flex gap-2 flex-wrap">
                      {dashboard.recentAchievements.slice(0, 3).map(a => (
                        <span
                          key={a.id}
                          className="bg-accent/30 rounded-full px-3 py-1 text-sm font-semibold text-gray-700"
                        >
                          🏆 {a.name}
                        </span>
                      ))}
                    </div>
                  </div>
                ) : null}
              </Card>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default ParentDashboard;
