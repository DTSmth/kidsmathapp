import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import Layout from '../components/layout/Layout';
import Card from '../components/common/Card';
import ProgressBar from '../components/common/ProgressBar';
import { getTopicsWithProgress } from '../services/topics';
import { Lock, BookOpen } from 'lucide-react';
import type { TopicWithProgressDto } from '../types';

const TOPIC_COLORS = ['primary', 'accent', 'coral', 'purple', 'success'] as const;

const ICON_MAP: Record<string, string> = {
  counting: '🔢',
  numbers: '🔢',
  addition: '➕',
  subtraction: '➖',
  multiplication: '✖️',
  division: '➗',
  shapes: '🔷',
  fractions: '½',
  measurement: '📏',
  time: '⏰',
  money: '💰',
  geometry: '📐',
  compare: '⚖️',
};

const getIcon = (iconName: string) => ICON_MAP[iconName.toLowerCase()] || '📚';

const TopicSkeleton = () => (
  <div className="bg-white rounded-2xl border border-gray-200 p-4 shadow-sm animate-pulse">
    <div className="w-10 h-10 bg-gray-100 rounded-lg mx-auto mb-3" />
    <div className="h-4 bg-gray-100 rounded w-2/3 mx-auto mb-3" />
    <div className="h-1.5 bg-gray-100 rounded-full w-full" />
  </div>
);

const TopicList = () => {
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const [topics, setTopics] = useState<TopicWithProgressDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!selectedChild) return;
    setLoading(true);
    getTopicsWithProgress(selectedChild.id)
      .then(setTopics)
      .catch(() => setError('Could not load topics. Please try again.'))
      .finally(() => setLoading(false));
  }, [selectedChild]);

  return (
    <Layout>
      <div className="space-y-5">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Learn</h1>
          <p className="text-sm text-gray-500 mt-1">Pick a topic to start practicing</p>
        </div>

        {error && (
          <div className="text-center py-8">
            <p className="text-gray-600 text-sm mb-4">{error}</p>
            <button onClick={() => window.location.reload()} className="text-primary font-medium text-sm hover:underline">
              Try again
            </button>
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {[1, 2, 3, 4, 5, 6].map(i => <TopicSkeleton key={i} />)}
          </div>
        ) : topics.length === 0 ? (
          <div className="text-center py-16">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <BookOpen className="w-8 h-8 text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm">No topics available yet</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {topics.map((topic, i) => {
              const color = TOPIC_COLORS[i % TOPIC_COLORS.length];
              return (
                <Card
                  key={topic.id}
                  color={topic.isUnlocked ? color : 'none'}
                  hoverable={topic.isUnlocked}
                  onClick={topic.isUnlocked ? () => navigate(`/topics/${topic.id}/lessons`) : undefined}
                  className={`relative ${!topic.isUnlocked ? 'opacity-60' : ''}`}
                >
                  {!topic.isUnlocked && (
                    <div className="absolute inset-0 bg-white/60 rounded-2xl flex items-center justify-center z-10">
                      <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
                        <Lock className="w-5 h-5 text-gray-400" />
                      </div>
                    </div>
                  )}
                  <div className="text-center">
                    <span className="text-3xl">{getIcon(topic.iconName)}</span>
                    <h3 className="font-semibold text-gray-800 mt-2 text-sm">{topic.name}</h3>
                    <div className="mt-3">
                      <ProgressBar progress={topic.progressPercent} height="sm" showPercentage={false} />
                    </div>
                    <p className="text-xs text-gray-500 mt-1.5">
                      {topic.lessonsCompleted}/{topic.totalLessons} lessons
                    </p>
                  </div>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default TopicList;
