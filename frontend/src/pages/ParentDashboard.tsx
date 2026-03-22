import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import Card from '../components/common/Card';
import Avatar from '../components/common/Avatar';
import { getParentChildren, getParentDashboard } from '../services/parent';
import type { ChildSummaryDto, ParentDashboardData } from '../types';

const ParentDashboard = () => {
  const [children, setChildren] = useState<ChildSummaryDto[]>([]);
  const [selectedChildId, setSelectedChildId] = useState<number | null>(null);
  const [dashboard, setDashboard] = useState<ParentDashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [dashLoading, setDashLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getParentChildren()
      .then(data => {
        setChildren(data);
        if (data.length > 0) {
          setSelectedChildId(data[0].id);
        }
      })
      .catch(() => setError('Could not load your learners.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedChildId) return;
    setDashLoading(true);
    setDashboard(null);
    getParentDashboard(selectedChildId)
      .then(setDashboard)
      .catch(() => setDashboard(null))
      .finally(() => setDashLoading(false));
  }, [selectedChildId]);

  const selectedChild = children.find(c => c.id === selectedChildId);

  return (
    <Layout>
      <div className="space-y-6">
        <h1 className="text-3xl font-extrabold text-gray-800">Your Learners</h1>

        {error && <p className="text-coral">{error}</p>}

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[1, 2].map(i => (
              <div key={i} className="bg-white rounded-3xl shadow-lg p-6 border-4 border-gray-200 animate-pulse h-40" />
            ))}
          </div>
        ) : children.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-6xl mb-4">🐣</p>
            <p className="text-xl font-bold text-gray-800 mb-2">No learners yet!</p>
            <p className="text-gray-600">Add a child profile to get started.</p>
          </div>
        ) : (
          <>
            {/* Child selector tabs */}
            {children.length > 1 && (
              <div className="flex gap-3 overflow-x-auto pb-2">
                {children.map(child => (
                  <button
                    key={child.id}
                    onClick={() => setSelectedChildId(child.id)}
                    className={`flex items-center gap-2 px-4 py-2 rounded-full font-semibold text-sm whitespace-nowrap transition-all ${
                      selectedChildId === child.id
                        ? 'bg-primary text-white shadow-md'
                        : 'bg-white text-gray-600 border-2 border-gray-200 hover:border-primary'
                    }`}
                  >
                    <Avatar avatarId={child.avatarId} size="sm" />
                    {child.name}
                  </button>
                ))}
              </div>
            )}

            {/* Dashboard for selected child */}
            {selectedChild && (
              <div className="space-y-4">
                {/* Header */}
                <Card color="primary">
                  <div className="flex items-center gap-4">
                    <Avatar avatarId={selectedChild.avatarId} size="lg" />
                    <div>
                      <h2 className="text-xl font-extrabold text-gray-800">{selectedChild.name}</h2>
                      <p className="text-sm text-gray-500">⭐ {selectedChild.totalStars} stars total</p>
                    </div>
                    {dashboard && (
                      <div className="ml-auto">
                        <div className="bg-primary/10 rounded-2xl px-4 py-2 text-center">
                          <p className="text-lg font-bold text-primary">{dashboard.daysActiveThisWeek}/7</p>
                          <p className="text-xs text-gray-500">days · {dashboard.totalMinutesThisWeek} mins</p>
                        </div>
                      </div>
                    )}
                  </div>
                </Card>

                {dashLoading ? (
                  <div className="bg-white rounded-3xl shadow-lg p-6 animate-pulse h-32" />
                ) : dashboard ? (
                  <>
                    {/* Needs Practice */}
                    {dashboard.topicAccuracies.some(t => t.needsPractice) && (
                      <Card>
                        <h3 className="font-bold text-gray-700 mb-3">Needs Practice</h3>
                        <div className="flex flex-wrap gap-2">
                          {dashboard.topicAccuracies.filter(t => t.needsPractice).map(t => (
                            <div key={t.topicId} className="bg-coral/10 border border-coral/30 rounded-xl px-3 py-2">
                              <span className="text-lg">{t.topicEmoji}</span>
                              <span className="ml-2 text-sm font-semibold text-gray-700">{t.topicName}</span>
                              <span className="ml-2 text-xs text-coral">{Math.round(t.accuracy)}%</span>
                            </div>
                          ))}
                        </div>
                      </Card>
                    )}

                    {/* Topic accuracy bars */}
                    <Card>
                      <h3 className="font-bold text-gray-700 mb-3">Topic Accuracy</h3>
                      <div className="space-y-3">
                        {dashboard.topicAccuracies.filter(t => t.lessonsCompleted > 0).map(t => (
                          <div key={t.topicId}>
                            <div className="flex justify-between items-center mb-1">
                              <span className="text-sm font-medium text-gray-700">
                                {t.topicEmoji} {t.topicName}
                              </span>
                              <span className="text-sm text-gray-500">{Math.round(t.accuracy)}%</span>
                            </div>
                            <div className="w-full bg-gray-100 rounded-full h-2">
                              <div
                                className={`h-2 rounded-full transition-all ${t.accuracy >= 70 ? 'bg-primary' : 'bg-coral'}`}
                                style={{ width: `${Math.min(t.accuracy, 100)}%` }}
                              />
                            </div>
                          </div>
                        ))}
                        {dashboard.topicAccuracies.every(t => t.lessonsCompleted === 0) && (
                          <p className="text-gray-500 text-sm text-center py-4">No lessons completed yet</p>
                        )}
                      </div>
                    </Card>

                    {/* 7-day heatmap */}
                    <Card>
                      <h3 className="font-bold text-gray-700 mb-3">Last 7 Days</h3>
                      <div className="flex justify-between gap-1">
                        {dashboard.heatmap.map(day => (
                          <div key={day.date} className="flex flex-col items-center gap-1 flex-1">
                            <div
                              className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${
                                day.isToday
                                  ? 'ring-2 ring-primary ring-offset-1'
                                  : ''
                              } ${
                                day.practiced
                                  ? 'bg-primary text-white'
                                  : 'bg-gray-200 text-gray-400'
                              }`}
                              title={`${day.date}: ${day.minutesPracticed} min`}
                            >
                              {day.practiced ? '✓' : '·'}
                            </div>
                            <span className="text-xs text-gray-500">
                              {new Date(day.date + 'T00:00:00').toLocaleDateString('en', { weekday: 'short' }).slice(0, 2)}
                            </span>
                          </div>
                        ))}
                      </div>
                    </Card>

                    {/* 30-day trajectory */}
                    {dashboard.trajectory.some(t => t.avgScore > 0) && (
                      <Card>
                        <h3 className="font-bold text-gray-700 mb-3">Score Trend</h3>
                        <div className="relative h-24">
                          <svg viewBox="0 0 300 80" className="w-full h-full" preserveAspectRatio="none">
                            {dashboard.trajectory.length > 1 && (
                              <polyline
                                points={dashboard.trajectory.map((pt, i) => {
                                  const x = (i / (dashboard.trajectory.length - 1)) * 280 + 10;
                                  const y = 70 - (pt.avgScore / 100) * 60;
                                  return `${x},${y}`;
                                }).join(' ')}
                                fill="none"
                                stroke="#4ECDC4"
                                strokeWidth="3"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                              />
                            )}
                            {dashboard.trajectory.map((pt, i) => {
                              const x = dashboard.trajectory.length > 1
                                ? (i / (dashboard.trajectory.length - 1)) * 280 + 10
                                : 150;
                              const y = 70 - (pt.avgScore / 100) * 60;
                              return (
                                <circle key={i} cx={x} cy={y} r="4" fill="#4ECDC4" />
                              );
                            })}
                          </svg>
                          <div className="flex justify-between mt-1">
                            {dashboard.trajectory.map((pt, i) => (
                              <span key={i} className="text-xs text-gray-500">{pt.weekLabel}</span>
                            ))}
                          </div>
                        </div>
                      </Card>
                    )}

                    {/* Upgrade CTA */}
                    {!dashboard.isPremium && (
                      <div className="bg-gradient-to-r from-accent to-yellow-300 rounded-3xl p-6 text-center shadow-lg">
                        <p className="text-2xl mb-2">🚀</p>
                        <h3 className="font-extrabold text-gray-800 text-lg mb-1">Unlock Unlimited Learning</h3>
                        <p className="text-gray-700 text-sm mb-4">
                          {selectedChild.name} is limited to 3 lessons/day on the free plan.
                        </p>
                        <p className="text-gray-600 text-xs mb-4">$4.99 / month · Cancel anytime</p>
                        <button className="bg-primary text-white font-bold px-6 py-3 rounded-2xl hover:bg-primary/90 transition-colors">
                          Upgrade to Premium
                        </button>
                      </div>
                    )}
                  </>
                ) : (
                  <p className="text-gray-500 text-sm text-center py-8">No activity yet — start learning! 🚀</p>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  );
};

export default ParentDashboard;
