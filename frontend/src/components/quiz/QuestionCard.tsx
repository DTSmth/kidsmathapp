import { useState } from 'react';
import NumberPad from './NumberPad';
import Button from '../common/Button';
import type { QuestionDto, AnswerResultDto } from '../../types';

interface QuestionCardProps {
  question: QuestionDto;
  onAnswer: (questionId: number, answer: string) => Promise<AnswerResultDto>;
  onAdvance: () => void;
  questionNumber: number;
  totalQuestions: number;
}

type FeedbackState = null | 'loading' | { result: AnswerResultDto };

const QuestionCard = ({ question, onAnswer, onAdvance, questionNumber, totalQuestions }: QuestionCardProps) => {
  const [feedback, setFeedback] = useState<FeedbackState>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleAnswer = async (answer: string) => {
    if (feedback || isLoading) return;
    setIsLoading(true);
    setSelectedOption(answer);
    const result = await onAnswer(question.id, answer);
    setFeedback({ result });
    setIsLoading(false);
  };

  const isMC = question.questionType === 'MULTIPLE_CHOICE' || question.questionType === 'TRUE_FALSE';
  const hasAnswered = !!feedback && feedback !== 'loading';
  const resultFeedback = hasAnswered ? (feedback as { result: AnswerResultDto }).result : null;

  // Card border color based on state
  const cardBorder = !hasAnswered
    ? 'border-gray-200'
    : resultFeedback?.correct
    ? 'border-success bg-success/5'
    : 'border-coral bg-coral/5';

  const cardAnimation = hasAnswered && !resultFeedback?.correct ? 'animate-shake' : '';

  return (
    <div
      className={`bg-white rounded-3xl shadow-lg border-4 p-6 transition-all duration-300 ${cardBorder} ${cardAnimation}`}
      role={hasAnswered && !resultFeedback?.correct ? 'alert' : undefined}
    >
      {/* Question text */}
      <p className="text-xl md:text-2xl font-bold text-gray-800 mb-6 text-center leading-relaxed">
        {question.questionText}
      </p>

      {/* Answer surface */}
      {!hasAnswered ? (
        isMC ? (
          <div className="flex flex-col gap-3">
            {question.options.map((option) => (
              <button
                key={option}
                onClick={() => handleAnswer(option)}
                disabled={isLoading}
                aria-label={`Answer option: ${option}`}
                className={`w-full rounded-2xl px-6 py-4 font-bold text-lg border-2 border-gray-200 bg-white text-gray-700
                  hover:border-primary hover:bg-primary/10 hover:scale-[1.02]
                  active:scale-95 transition-all duration-200 disabled:opacity-50`}
              >
                {option}
              </button>
            ))}
          </div>
        ) : (
          <NumberPad onSubmit={handleAnswer} disabled={isLoading} />
        )
      ) : (
        /* Teach mode: show correct answer with feedback */
        <div className="space-y-4">
          {isMC ? (
            <div className="flex flex-col gap-3">
              {question.options.map((option) => {
                const isCorrect = option === resultFeedback?.correctAnswer;
                const isSelected = option === selectedOption;
                let optionStyle = 'border-2 border-gray-100 bg-gray-50 text-gray-500';
                if (isCorrect) optionStyle = 'border-2 border-success bg-success/20 text-gray-800 font-bold';
                else if (isSelected && !resultFeedback?.correct) optionStyle = 'border-2 border-coral bg-coral/20 text-gray-700';
                return (
                  <div key={option} className={`w-full rounded-2xl px-6 py-4 font-bold text-lg ${optionStyle}`}>
                    {isCorrect ? '✅ ' : isSelected ? '❌ ' : ''}{option}
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="text-center space-y-2">
              <p className="text-lg text-gray-600">
                Your answer:{' '}
                <span className={`font-bold ${resultFeedback?.correct ? 'text-success' : 'text-coral'}`}>
                  {selectedOption}
                </span>
              </p>
              {!resultFeedback?.correct && (
                <p className="text-lg text-gray-700">
                  Correct answer: <span className="font-bold text-success">{resultFeedback?.correctAnswer}</span>
                </p>
              )}
            </div>
          )}

          {/* Feedback message */}
          <div className={`text-center py-2 rounded-2xl ${resultFeedback?.correct ? 'bg-success/20' : 'bg-coral/10'}`}>
            <p className="font-bold text-lg">
              {resultFeedback?.correct
                ? '⭐ Correct!'
                : '💪 ' + (resultFeedback?.message || 'Almost!')}
            </p>
          </div>

          <Button
            variant="primary"
            fullWidth
            onClick={onAdvance}
            className="mt-2"
          >
            {questionNumber < totalQuestions ? 'Next Question →' : 'See Results! 🎉'}
          </Button>
        </div>
      )}
    </div>
  );
};

export default QuestionCard;
