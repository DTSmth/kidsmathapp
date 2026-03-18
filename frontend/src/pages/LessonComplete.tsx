import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Button from '../components/common/Button';
import Confetti from '../components/gamification/Confetti';
import ToastQueue from '../components/gamification/ToastQueue';
import { useChild } from '../context/ChildContext';
import { Star, RotateCcw, ArrowRight, ArrowLeft } from 'lucide-react';
import type { LessonSubmissionResult } from '../types';

const getScoreLabel = (score: number) => {
  if (score === 100) return { label: 'Perfect!', color: 'text-primary' };
  if (score >= 90) return { label: 'Amazing!', color: 'text-primary' };
  if (score >= 80) return { label: 'Great job!', color: 'text-success-dark' };
  if (score >= 70) return { label: 'Nice work!', color: 'text-amber-600' };
  return { label: 'Keep practicing!', color: 'text-gray-600' };
};

const LessonComplete = () => {
  const { lessonId } = useParams<{ lessonId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const { selectedChild } = useChild();
  const [showToasts, setShowToasts] = useState(false);

  const result = location.state?.result as LessonSubmissionResult | undefined;

  useEffect(() => {
    if (result?.passed && result.newAchievements.length > 0) {
      const t = setTimeout(() => setShowToasts(true), 800);
      return () => clearTimeout(t);
    }
  }, [result]);

  if (!result) {
    navigate('/dashboard');
    return null;
  }

  const { label, color } = getScoreLabel(result.score);

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-primary/5 flex flex-col items-center justify-center px-4 py-8 gap-8">
      {result.passed && <Confetti />}

      {/* Score display */}
      <div className="text-center animate-fade-in">
        <p className={`text-6xl font-bold ${result.passed ? 'text-primary' : 'text-gray-400'}`}>
          {result.score}%
        </p>
        <p className={`text-xl font-semibold mt-2 ${color}`}>
          {label}
        </p>
      </div>

      {/* Stars earned */}
      {result.passed && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl px-6 py-4 text-center animate-fade-in">
          <div className="flex items-center justify-center gap-2 mb-1">
            <Star className="w-6 h-6 text-amber-500 fill-current" />
            <span className="text-2xl font-bold text-gray-800">+{result.starsEarned}</span>
            {result.bonusStars > 0 && (
              <span className="text-sm text-amber-600 font-medium">+{result.bonusStars} bonus</span>
            )}
          </div>
          <p className="text-sm text-gray-600">Total: {result.totalStars} stars</p>
        </div>
      )}

      {/* Mascot message */}
      <div className="bg-white rounded-2xl border border-gray-200 px-6 py-4 text-center max-w-sm shadow-sm">
        <span className="text-4xl block mb-2">
          {result.passed ? '🦁' : '🦉'}
        </span>
        <p className="text-gray-700">
          {result.passed
            ? `Great job, ${selectedChild?.name}! Keep up the awesome work!`
            : `Don't give up, ${selectedChild?.name}! Practice makes perfect!`}
        </p>
      </div>

      {/* Actions */}
      <div className="flex flex-col gap-3 w-full max-w-xs">
        {result.passed ? (
          <Button variant="primary" fullWidth size="lg" onClick={() => navigate(-2)}>
            Continue
            <ArrowRight className="w-5 h-5" />
          </Button>
        ) : (
          <>
            <Button
              variant="primary"
              fullWidth
              size="lg"
              onClick={() => navigate(`/lessons/${lessonId}/quiz`)}
            >
              <RotateCcw className="w-5 h-5" />
              Try again
            </Button>
            <button
              onClick={() => navigate(-2)}
              className="flex items-center justify-center gap-1.5 text-gray-500 hover:text-gray-700 font-medium py-2 transition-colors text-sm"
            >
              <ArrowLeft className="w-4 h-4" />
              Back to lessons
            </button>
          </>
        )}
      </div>

      {/* Achievement toasts */}
      {showToasts && <ToastQueue achievements={result.newAchievements} />}
    </div>
  );
};

export default LessonComplete;
