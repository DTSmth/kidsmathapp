import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import Layout from '../components/layout/Layout';
import Card from '../components/common/Card';
import { getLessonsForTopic } from '../services/topics';
import type { LessonWithProgressDto } from '../types';
import { ArrowLeft, Star, Check, BookOpen } from 'lucide-react';

const LessonList = () => {
  const { topicId } = useParams<{ topicId: string }>();
  const { selectedChild } = useChild();
  const navigate = useNavigate();
  const [lessons, setLessons] = useState<LessonWithProgressDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!selectedChild || !topicId) return;
    getLessonsForTopic(Number(topicId), selectedChild.id)
      .then(setLessons)
      .catch(() => setError('Could not load lessons.'))
      .finally(() => setLoading(false));
  }, [topicId, selectedChild]);

  return (
    <Layout>
      <div className="space-y-5">
        <button
          onClick={() => navigate('/dashboard')}
          className="flex items-center gap-1.5 text-gray-500 hover:text-gray-700 transition-colors text-sm font-medium"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to topics
        </button>

        <h1 className="text-2xl font-bold text-gray-800">
          {lessons[0]?.topicName || 'Lessons'}
        </h1>

        {error && (
          <div className="bg-coral/10 border border-coral/30 rounded-lg px-4 py-3 text-coral-dark text-sm">
            {error}
          </div>
        )}

        {loading ? (
          <div className="space-y-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm animate-pulse h-16" />
            ))}
          </div>
        ) : lessons.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <BookOpen className="w-8 h-8 text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm">No lessons here yet!</p>
          </div>
        ) : (
          <div className="space-y-3">
            {lessons.map((lesson, i) => (
              <Card
                key={lesson.id}
                color={lesson.completed ? 'success' : 'none'}
                hoverable
                onClick={() => navigate(`/lessons/${lesson.id}/quiz`)}
                className="flex items-center gap-4"
              >
                <div
                  className={`w-9 h-9 rounded-full flex items-center justify-center font-semibold text-sm shrink-0
                    ${lesson.completed ? 'bg-success text-white' : 'bg-gray-100 text-gray-600'}`}
                >
                  {lesson.completed ? <Check className="w-4 h-4" /> : i + 1}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-gray-800 text-sm truncate">{lesson.title}</p>
                  {lesson.score != null && (
                    <p className="text-xs text-gray-500">Best: {lesson.score}%</p>
                  )}
                </div>
                <div className="flex items-center gap-1 text-amber-500 shrink-0">
                  <Star className="w-4 h-4 fill-current" />
                  <span className="font-semibold text-gray-700 text-sm">{lesson.starsReward}</span>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default LessonList;
