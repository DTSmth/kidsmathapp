import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import Layout from '../components/layout/Layout';
import { getParentDashboard } from '../services/parent';
import type { ParentDashboardData } from '../types';
import { Flame } from 'lucide-react';

const TOPIC_OPTIONS = [
  { type: 'addition', label: 'Addition', emoji: '➕' },
  { type: 'subtraction', label: 'Subtraction', emoji: '➖' },
  { type: 'multiplication', label: 'Multiplication', emoji: '✖️' },
  { type: 'division', label: 'Division', emoji: '➗' },
  { type: 'counting', label: 'Counting', emoji: '🔢' },
  { type: 'fractions', label: 'Fractions', emoji: '🍕' },
];

const PracticeSelect = () => {
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState<ParentDashboardData | null>(null);
  const [recommendedTopic, setRecommendedTopic] = useState(TOPIC_OPTIONS[0]);

  useEffect(() => {
    if (!selectedChild) return;
    // Try to get parent dashboard for recommendation (best-effort)
    getParentDashboard(selectedChild.id)
      .then(data => {
        setDashboard(data);
        // Find topic with lowest accuracy
        const withAccuracy = data.topicAccuracies.filter(t => t.lessonsCompleted > 0);
        if (withAccuracy.length > 0) {
          const lowest = withAccuracy.reduce((a, b) => a.accuracy < b.accuracy ? a : b);
          const match = TOPIC_OPTIONS.find(t =>
            lowest.topicName.toLowerCase().includes(t.type) ||
            lowest.topicEmoji === t.emoji
          );
          if (match) setRecommendedTopic(match);
        }
      })
      .catch(() => {});
  }, [selectedChild]);

  const handleStart = (type: string, label: string) => {
    navigate('/practice/quiz', { state: { topicType: type, topicName: label } });
  };

  if (!selectedChild) return null;

  return (
    <Layout>
      <div className="space-y-6">
        {/* Header */}
        <div className="text-center py-4">
          <p className="text-5xl mb-2">🎯</p>
          <h1 className="text-2xl font-extrabold text-gray-800">
            Ready to practice, {selectedChild.name}?
          </h1>
          {selectedChild.currentStreak > 0 && (
            <div className="inline-flex items-center gap-1 bg-amber-50 text-amber-600 px-3 py-1 rounded-full text-sm font-semibold mt-2">
              <Flame className="w-4 h-4" />
              {selectedChild.currentStreak} day streak!
            </div>
          )}
        </div>

        {/* Recommended */}
        <div>
          <p className="text-sm font-semibold text-gray-500 mb-2">Recommended for you</p>
          <button
            onClick={() => handleStart(recommendedTopic.type, recommendedTopic.label)}
            className="w-full bg-primary text-white font-extrabold py-5 px-6 rounded-3xl text-xl hover:bg-primary/90 transition-colors flex items-center justify-center gap-3 shadow-lg"
          >
            {recommendedTopic.emoji} Start {recommendedTopic.label} Practice
          </button>
        </div>

        {/* Topic grid */}
        <div>
          <p className="text-sm font-semibold text-gray-500 mb-3">Or choose a topic</p>
          <div className="grid grid-cols-3 gap-3">
            {TOPIC_OPTIONS.map(topic => (
              <button
                key={topic.type}
                onClick={() => handleStart(topic.type, topic.label)}
                className="bg-white border-2 border-gray-200 hover:border-primary rounded-2xl p-4 text-center transition-all hover:shadow-md active:scale-95"
              >
                <div className="text-3xl mb-1">{topic.emoji}</div>
                <div className="text-xs font-semibold text-gray-700">{topic.label}</div>
                {dashboard && (() => {
                  const acc = dashboard.topicAccuracies.find(t =>
                    t.topicName.toLowerCase().includes(topic.type)
                  );
                  return acc && acc.lessonsCompleted > 0 ? (
                    <div className={`text-xs mt-1 ${acc.accuracy < 70 ? 'text-coral' : 'text-primary'}`}>
                      {Math.round(acc.accuracy)}%
                    </div>
                  ) : null;
                })()}
              </button>
            ))}
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default PracticeSelect;
