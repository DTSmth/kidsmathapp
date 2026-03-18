import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import QuizLayout from '../components/layout/QuizLayout';
import QuestionCard from '../components/quiz/QuestionCard';
import AnimalMascot from '../components/characters/AnimalMascot';
import Button from '../components/common/Button';
import { getLessonDetail, submitLesson } from '../services/lessons';
import { checkAnswer } from '../services/progress';
import type { LessonDetailDto, AnswerSubmissionDto, AnswerResultDto } from '../types';

// JWT pre-flight check
const isTokenExpiringSoon = (): boolean => {
  const token = localStorage.getItem('token');
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const expiresAt = payload.exp * 1000;
    const thirtyMinutes = 30 * 60 * 1000;
    return Date.now() > expiresAt - thirtyMinutes;
  } catch {
    return false;
  }
};

type QuizPhase = 'loading' | 'intro' | 'question' | 'submitting' | 'error';

const LessonQuiz = () => {
  const { lessonId } = useParams<{ lessonId: string }>();
  const { selectedChild, updateChildStats } = useChild();
  const navigate = useNavigate();

  const [lesson, setLesson] = useState<LessonDetailDto | null>(null);
  const [phase, setPhase] = useState<QuizPhase>('loading');
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<AnswerSubmissionDto[]>([]);
  const [jwtWarning, setJwtWarning] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (!lessonId) return;
    // JWT pre-flight check
    if (isTokenExpiringSoon()) setJwtWarning(true);

    getLessonDetail(Number(lessonId))
      .then(data => {
        if (data.questions.length === 0) {
          setErrorMsg('This lesson has no questions yet.');
          setPhase('error');
          return;
        }
        setLesson(data);
        setPhase('intro');
      })
      .catch(() => {
        setErrorMsg("Couldn't load questions. Please go back and try again.");
        setPhase('error');
      });
  }, [lessonId]);

  const handleAnswer = async (questionId: number, answer: string): Promise<AnswerResultDto> => {
    const result = await checkAnswer(questionId, answer);
    setAnswers(prev => [...prev.filter(a => a.questionId !== questionId), { questionId, answer }]);
    return result;
  };

  const handleAdvance = () => {
    if (!lesson) return;
    if (currentIndex < lesson.questions.length - 1) {
      setCurrentIndex(prev => prev + 1);
    } else {
      handleSubmit();
    }
  };

  const handleSubmit = async () => {
    if (!lesson || !selectedChild) return;
    setPhase('submitting');
    try {
      const result = await submitLesson(lesson.id, {
        childId: selectedChild.id,
        answers,
      });
      // Update ChildContext with fresh stats
      updateChildStats(result.totalStars, result.currentStreak);
      // Navigate to completion screen with result
      navigate(`/lessons/${lesson.id}/complete`, { state: { result, lessonTitle: lesson.title } });
    } catch {
      setPhase('error');
      setErrorMsg("Couldn't submit your answers. Please try again.");
    }
  };

  const mascotAnimal = (selectedChild?.avatarId || 'leo') as 'leo' | 'ollie' | 'bella' | 'max';

  if (phase === 'loading') {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center gap-6">
        <AnimalMascot
          animal={mascotAnimal}
          mood="thinking"
          message="Getting your questions ready! 📚"
        />
      </div>
    );
  }

  if (phase === 'error') {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center gap-6 px-4">
        <p className="text-2xl">😕</p>
        <p className="text-gray-700 text-center">{errorMsg}</p>
        <Button variant="primary" onClick={() => navigate(-1)}>Go Back</Button>
      </div>
    );
  }

  if (phase === 'submitting') {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center gap-6">
        <AnimalMascot
          animal={mascotAnimal}
          mood="thinking"
          message="Checking your answers..."
        />
      </div>
    );
  }

  if (!lesson) return null;

  if (phase === 'intro') {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center gap-8 px-4">
        {jwtWarning && (
          <div className="bg-accent/20 border-2 border-accent rounded-2xl px-4 py-3 text-center max-w-sm">
            <p className="font-semibold text-gray-700">
              ⚠️ Your session expires soon. Finish quickly to save your progress!
            </p>
          </div>
        )}
        <AnimalMascot
          animal={mascotAnimal}
          mood="excited"
          size="lg"
          message={`Let's do "${lesson.title}", ${selectedChild?.name}! 🎯`}
        />
        <div className="text-center">
          <p className="text-gray-600 mb-1">{lesson.questions.length} questions</p>
          <p className="text-gray-600">
            Earn <span className="font-bold text-accent">⭐ ×{lesson.starsReward}</span> by scoring 70%+
          </p>
        </div>
        <Button
          variant="primary"
          className="text-xl px-10 py-4"
          onClick={() => setPhase('question')}
        >
          Let's Go! 🚀
        </Button>
      </div>
    );
  }

  const currentQuestion = lesson.questions[currentIndex];

  return (
    <QuizLayout
      totalQuestions={lesson.questions.length}
      currentQuestion={currentIndex}
      onExit={() => navigate(`/topics/${lesson.topicId}/lessons`)}
    >
      <div className="animate-fade-in">
        <QuestionCard
          question={currentQuestion}
          onAnswer={handleAnswer}
          onAdvance={handleAdvance}
          questionNumber={currentIndex + 1}
          totalQuestions={lesson.questions.length}
        />
      </div>
    </QuizLayout>
  );
};

export default LessonQuiz;
