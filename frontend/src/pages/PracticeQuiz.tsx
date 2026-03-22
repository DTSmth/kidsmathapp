import { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import { generatePractice } from '../services/practice';
import type { PracticeQuestion, PracticeSession } from '../services/practice';

const PracticeQuiz = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { selectedChild } = useChild();

  const topicType = location.state?.topicType as string || 'addition';
  const topicName = location.state?.topicName as string || 'Math';

  const [session, setSession] = useState<PracticeSession | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<{ questionIndex: number; answer: string }[]>([]);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<'correct' | 'wrong' | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [elapsed, setElapsed] = useState(0);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!selectedChild) return;
    generatePractice(selectedChild.id, topicType, 10)
      .then(data => {
        setSession(data);
        setLoading(false);
        timerRef.current = setInterval(() => setElapsed(e => e + 1), 1000);
      })
      .catch(() => {
        setError("Couldn't load practice questions. Please go back and try again.");
        setLoading(false);
      });
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [selectedChild, topicType]);

  const currentQuestion: PracticeQuestion | undefined = session?.questions[currentIndex];

  const handleAnswer = (answer: string) => {
    if (feedback || !currentQuestion) return;

    const isCorrect = answer === currentQuestion.correctAnswer;
    setSelectedAnswer(answer);
    setFeedback(isCorrect ? 'correct' : 'wrong');

    const newAnswer = { questionIndex: currentIndex, answer };
    const updatedAnswers = [...answers, newAnswer];
    setAnswers(updatedAnswers);

    const delay = isCorrect ? 1200 : 2000;
    setTimeout(() => {
      if (!session) return;
      const nextIndex = currentIndex + 1;
      if (nextIndex >= session.questions.length) {
        if (timerRef.current) clearInterval(timerRef.current);
        navigate('/practice/complete', {
          state: {
            sessionToken: session.sessionToken,
            answers: updatedAnswers,
            questions: session.questions,
            topicName,
          }
        });
      } else {
        setCurrentIndex(nextIndex);
        setSelectedAnswer(null);
        setFeedback(null);
      }
    }, delay);
  };

  const formatTime = (s: number) => `${Math.floor(s / 60)}:${(s % 60).toString().padStart(2, '0')}`;

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-6xl animate-bounce">🎯</div>
      </div>
    );
  }

  if (error || !session) {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center gap-4">
        <p className="text-coral text-lg">{error || 'Something went wrong'}</p>
        <button onClick={() => navigate(-1)} className="text-primary font-semibold">Go back</button>
      </div>
    );
  }

  const progress = ((currentIndex) / session.questions.length) * 100;

  return (
    <div className="min-h-screen bg-background flex flex-col">
      {/* Header */}
      <div className="bg-white shadow-sm px-4 py-3 flex items-center justify-between">
        <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-600 text-sm">✕</button>
        <div className="flex items-center gap-2">
          {session.questions.map((_, i) => (
            <div
              key={i}
              className={`w-2 h-2 rounded-full transition-all ${
                i < currentIndex ? 'bg-primary' : i === currentIndex ? 'bg-primary scale-125' : 'bg-gray-200'
              }`}
            />
          ))}
        </div>
        <span className="text-gray-400 text-sm font-mono">{formatTime(elapsed)}</span>
      </div>

      {/* Progress bar */}
      <div className="h-1.5 bg-gray-100">
        <div className="h-full bg-primary transition-all" style={{ width: `${progress}%` }} />
      </div>

      {/* Question */}
      <div className="flex-1 flex flex-col justify-between p-6">
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center max-w-sm">
            <p className="text-sm text-gray-400 mb-4">{topicName} · Question {currentIndex + 1} of {session.questions.length}</p>
            <p className="text-3xl font-extrabold text-gray-800 leading-relaxed">
              {currentQuestion?.questionText}
            </p>
          </div>
        </div>

        {/* Answer buttons */}
        <div className="grid grid-cols-2 gap-3 mt-6">
          {currentQuestion?.options.map((option, i) => {
            let btnClass = 'bg-white border-2 border-gray-200 text-gray-800 font-bold';
            if (selectedAnswer === option) {
              if (feedback === 'correct') {
                btnClass = 'bg-green-100 border-2 border-green-500 text-green-700 font-bold';
              } else {
                btnClass = 'bg-red-100 border-2 border-red-400 text-red-700 font-bold animate-pulse';
              }
            } else if (feedback === 'wrong' && option === currentQuestion.correctAnswer) {
              btnClass = 'bg-green-50 border-2 border-green-400 text-green-600 font-bold';
            }

            return (
              <button
                key={i}
                onClick={() => handleAnswer(option)}
                disabled={!!feedback}
                className={`${btnClass} py-5 px-4 rounded-2xl text-lg transition-all min-h-[72px] active:scale-95 disabled:cursor-default`}
              >
                {option}
              </button>
            );
          })}
        </div>

        {/* Feedback emoji */}
        {feedback && (
          <div className="text-center mt-4 text-3xl animate-bounce">
            {feedback === 'correct' ? '✅' : '❌'}
          </div>
        )}
      </div>
    </div>
  );
};

export default PracticeQuiz;
