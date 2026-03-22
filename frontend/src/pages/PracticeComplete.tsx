import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { submitPractice } from '../services/practice';
import type { PracticeResult } from '../services/practice';
import Confetti from '../components/gamification/Confetti';
import Button from '../components/common/Button';
import { Star } from 'lucide-react';

const PracticeComplete = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const sessionToken = location.state?.sessionToken as string;
  const answers = location.state?.answers as { questionIndex: number; answer: string }[];
  const questions = location.state?.questions as { correctAnswer: string }[];
  const topicName = location.state?.topicName as string || 'Math';

  const [result, setResult] = useState<PracticeResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [saveError, setSaveError] = useState(false);

  // Calculate local score as fallback
  const localCorrect = questions && answers
    ? answers.filter(a => questions[a.questionIndex]?.correctAnswer === a.answer).length
    : 0;
  const localTotal = questions?.length || 10;

  useEffect(() => {
    if (!sessionToken || !answers) {
      navigate('/practice');
      return;
    }

    submitPractice(sessionToken, answers)
      .then(data => {
        setResult(data);
      })
      .catch(() => {
        setSaveError(true);
        // Still show celebration using local calculation
        setResult({
          score: localTotal > 0 ? Math.round((localCorrect / localTotal) * 100) : 0,
          correctCount: localCorrect,
          totalCount: localTotal,
          starsEarned: Math.floor(localCorrect / localTotal * 5),
        });
      })
      .finally(() => setLoading(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-6xl animate-bounce">⭐</div>
      </div>
    );
  }

  const correctCount = result?.correctCount ?? localCorrect;
  const totalCount = result?.totalCount ?? localTotal;
  const score = result?.score ?? 0;
  const starsEarned = result?.starsEarned ?? 0;

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-primary/5 flex flex-col items-center justify-center px-4 py-8 gap-6">
      {score >= 60 && <Confetti />}

      {/* Score */}
      <div className="text-center animate-fade-in">
        <p className="text-6xl font-extrabold text-primary">
          {correctCount}/{totalCount}
        </p>
        <p className="text-gray-500 text-lg mt-1">{score}% correct</p>
      </div>

      {/* Stars */}
      {starsEarned > 0 && (
        <div className="flex items-center gap-2 animate-bounce">
          {Array.from({ length: Math.min(starsEarned, 5) }).map((_, i) => (
            <Star key={i} className="w-8 h-8 text-amber-400 fill-current" />
          ))}
          <span className="text-gray-600 font-semibold ml-2">+{starsEarned} stars</span>
        </div>
      )}

      {/* New item */}
      {result?.newItem && (
        <div className="bg-accent/20 border-2 border-accent rounded-3xl px-6 py-4 text-center animate-fade-in">
          <p className="text-3xl">{result.newItem.emoji}</p>
          <p className="font-bold text-gray-800 mt-1">You found: {result.newItem.name}!</p>
          <p className="text-xs text-gray-500">{result.newItem.tier}</p>
        </div>
      )}

      {/* Insight */}
      <div className="bg-white rounded-2xl border border-gray-200 px-6 py-4 text-center max-w-sm shadow-sm">
        <p className="text-gray-700">
          {score >= 80
            ? `Great job on ${topicName}! 🌟 Keep it up!`
            : score >= 60
            ? `Nice work on ${topicName}! A little more practice will help. 💪`
            : `Keep practicing ${topicName}! You're getting better! 🎯`}
        </p>
      </div>

      {/* Save error banner */}
      {saveError && (
        <div className="bg-amber-50 border border-amber-200 rounded-2xl px-4 py-2 text-center text-sm text-amber-700 max-w-sm">
          Score couldn't save · We'll try again next time
        </div>
      )}

      {/* Actions */}
      <div className="flex flex-col gap-3 w-full max-w-xs">
        <Button variant="primary" fullWidth size="lg" onClick={() => navigate('/practice/quiz', {
          state: { topicType: location.state?.topicType, topicName }
        })}>
          Practice Again
        </Button>
        <button
          onClick={() => navigate('/dashboard')}
          className="text-gray-500 hover:text-gray-700 font-medium py-2 transition-colors text-center"
        >
          Back to Home
        </button>
      </div>
    </div>
  );
};

export default PracticeComplete;
