import { useState, useEffect, useCallback, useRef } from 'react';
import NumberPad from './NumberPad';
import Button from '../common/Button';
import type { QuestionDto, AnswerResultDto } from '../../types';
import { Volume2, Headphones } from 'lucide-react';

interface QuestionCardProps {
  question: QuestionDto;
  onAnswer: (questionId: number, answer: string) => Promise<AnswerResultDto>;
  onAdvance: () => void;
  questionNumber: number;
  totalQuestions: number;
}

type FeedbackState = null | { result: AnswerResultDto };

const speak = (text: string) => {
  if (!window.speechSynthesis) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.rate = 0.85;
  utterance.pitch = 1.1;
  window.speechSynthesis.speak(utterance);
};

/** Extracts "three" from "Tap the number three!" — returns null if not that pattern */
const extractTapNumber = (text: string): string | null => {
  const match = text.match(/^Tap the number (.+?)!?$/i);
  return match ? match[1] : null;
};

// ── Audio-first card for Number Recognition ──────────────────────────────────
const AudioNumberQuestion = ({
  question, onAnswer, onAdvance, questionNumber, totalQuestions,
}: QuestionCardProps) => {
  const [feedback, setFeedback] = useState<FeedbackState>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [pulsing, setPulsing] = useState(false);
  const spokenRef = useRef(false);

  const spokenWord = extractTapNumber(question.questionText) ?? question.questionText;

  const playNumber = useCallback(() => {
    setPulsing(true);
    speak(spokenWord);
    setTimeout(() => setPulsing(false), 900);
  }, [spokenWord]);

  useEffect(() => {
    if (!spokenRef.current) {
      spokenRef.current = true;
      const t = setTimeout(playNumber, 400);
      return () => clearTimeout(t);
    }
  }, [playNumber]);

  useEffect(() => () => { window.speechSynthesis?.cancel(); }, []);

  const handleAnswer = async (answer: string) => {
    if (feedback || isLoading) return;
    setIsLoading(true);
    setSelectedOption(answer);
    const result = await onAnswer(question.id, answer);
    setFeedback({ result });
    setIsLoading(false);
    speak(result.correct ? 'Correct! Great job!' : `The answer is ${spokenWord}`);
  };

  const resultFeedback = feedback?.result ?? null;
  const cardBorder = !feedback
    ? 'border-purple/40'
    : resultFeedback?.correct
    ? 'border-success bg-success/5'
    : 'border-coral bg-coral/5';
  const cardAnimation = feedback && !resultFeedback?.correct ? 'animate-shake' : '';

  return (
    <div className={`bg-white rounded-3xl shadow-lg border-4 p-6 transition-all duration-300 ${cardBorder} ${cardAnimation}`}>
      {/* Audio-first prompt */}
      {!feedback && (
        <div className="flex flex-col items-center gap-4 mb-6">
          <p className="text-base font-semibold text-gray-500 text-center">
            Listen and tap the number you hear!
          </p>
          <button
            onClick={playNumber}
            aria-label="Play number"
            className={`w-24 h-24 rounded-full flex flex-col items-center justify-center gap-1 transition-all duration-200
              ${pulsing
                ? 'bg-purple scale-110 shadow-lg shadow-purple/40'
                : 'bg-purple/10 hover:bg-purple/20 hover:scale-105'
              }`}
          >
            <Headphones className={`w-10 h-10 ${pulsing ? 'text-white' : 'text-purple'}`} />
            <span className={`text-xs font-bold ${pulsing ? 'text-white' : 'text-purple'}`}>
              {pulsing ? 'Playing…' : 'Tap to hear'}
            </span>
          </button>
        </div>
      )}

      {/* Answer options — large numeral buttons */}
      {!feedback ? (
        <div className="grid grid-cols-2 gap-3">
          {question.options.map((option) => (
            <button
              key={option}
              onClick={() => handleAnswer(option)}
              disabled={isLoading}
              className="w-full rounded-2xl border-2 border-gray-200 bg-white py-6 font-bold text-4xl text-gray-700
                hover:border-purple hover:bg-purple/10 hover:scale-[1.03]
                active:scale-95 transition-all duration-200 disabled:opacity-50"
            >
              {option}
            </button>
          ))}
        </div>
      ) : (
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            {question.options.map((option) => {
              const isCorrect = option === resultFeedback?.correctAnswer;
              const isSelected = option === selectedOption;
              let style = 'border-2 border-gray-100 bg-gray-50 text-gray-400';
              if (isCorrect) style = 'border-2 border-success bg-success/20 text-gray-800';
              else if (isSelected) style = 'border-2 border-coral bg-coral/20 text-gray-700';
              return (
                <div key={option} className={`w-full rounded-2xl py-6 font-bold text-4xl text-center ${style}`}>
                  {isCorrect ? '✅' : isSelected ? '❌' : option}
                </div>
              );
            })}
          </div>
          <div className={`text-center py-2 rounded-2xl ${resultFeedback?.correct ? 'bg-success/20' : 'bg-coral/10'}`}>
            <p className="font-bold text-lg">
              {resultFeedback?.correct ? `⭐ Yes! That's ${spokenWord}!` : `💪 That's ${spokenWord}!`}
            </p>
          </div>
          <Button variant="primary" fullWidth onClick={onAdvance} className="mt-2">
            {questionNumber < totalQuestions ? 'Next Question →' : 'See Results! 🎉'}
          </Button>
        </div>
      )}
    </div>
  );
};

// ── Standard question card (counting, MC, free text) ─────────────────────────
const StandardQuestion = ({
  question, onAnswer, onAdvance, questionNumber, totalQuestions,
}: QuestionCardProps) => {
  const [feedback, setFeedback] = useState<FeedbackState>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const spokenRef = useRef(false);

  useEffect(() => {
    if (!spokenRef.current) {
      spokenRef.current = true;
      const t = setTimeout(() => speak(question.questionText), 300);
      return () => clearTimeout(t);
    }
  }, [question.questionText]);

  useEffect(() => () => { window.speechSynthesis?.cancel(); }, []);

  const handleAnswer = useCallback(async (answer: string) => {
    if (feedback || isLoading) return;
    setIsLoading(true);
    setSelectedOption(answer);
    const result = await onAnswer(question.id, answer);
    setFeedback({ result });
    setIsLoading(false);
    speak(result.correct ? 'Correct! Great job!' : `The answer is ${result.correctAnswer}`);
  }, [feedback, isLoading, onAnswer, question.id]);

  const isMC = question.questionType === 'MULTIPLE_CHOICE' || question.questionType === 'TRUE_FALSE';
  const resultFeedback = feedback?.result ?? null;
  const cardBorder = !feedback
    ? 'border-gray-200'
    : resultFeedback?.correct
    ? 'border-success bg-success/5'
    : 'border-coral bg-coral/5';
  const cardAnimation = feedback && !resultFeedback?.correct ? 'animate-shake' : '';

  return (
    <div className={`bg-white rounded-3xl shadow-lg border-4 p-6 transition-all duration-300 ${cardBorder} ${cardAnimation}`}>
      <div className="flex items-start gap-2 mb-6">
        <p className="text-xl md:text-2xl font-bold text-gray-800 text-center leading-relaxed flex-1">
          {question.questionText}
        </p>
        <button
          onClick={() => speak(question.questionText)}
          aria-label="Read question aloud"
          className="shrink-0 w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center hover:bg-primary/20 transition-colors mt-0.5"
        >
          <Volume2 className="w-5 h-5 text-primary" />
        </button>
      </div>

      {!feedback ? (
        isMC ? (
          <div className="grid grid-cols-2 gap-3">
            {question.options.map((option) => (
              <button
                key={option}
                onClick={() => handleAnswer(option)}
                disabled={isLoading}
                className="w-full rounded-2xl px-4 py-5 font-bold text-2xl border-2 border-gray-200 bg-white text-gray-700
                  hover:border-primary hover:bg-primary/10 hover:scale-[1.02]
                  active:scale-95 transition-all duration-200 disabled:opacity-50"
              >
                {option}
              </button>
            ))}
          </div>
        ) : (
          <NumberPad onSubmit={handleAnswer} disabled={isLoading} />
        )
      ) : (
        <div className="space-y-4">
          {isMC ? (
            <div className="grid grid-cols-2 gap-3">
              {question.options.map((option) => {
                const isCorrect = option === resultFeedback?.correctAnswer;
                const isSelected = option === selectedOption;
                let style = 'border-2 border-gray-100 bg-gray-50 text-gray-400';
                if (isCorrect) style = 'border-2 border-success bg-success/20 text-gray-800 font-bold';
                else if (isSelected) style = 'border-2 border-coral bg-coral/20 text-gray-700';
                return (
                  <div key={option} className={`w-full rounded-2xl px-4 py-5 font-bold text-2xl text-center ${style}`}>
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
          <div className={`text-center py-2 rounded-2xl ${resultFeedback?.correct ? 'bg-success/20' : 'bg-coral/10'}`}>
            <p className="font-bold text-lg">
              {resultFeedback?.correct ? '⭐ Correct!' : '💪 ' + (resultFeedback?.message || 'Almost!')}
            </p>
          </div>
          <Button variant="primary" fullWidth onClick={onAdvance} className="mt-2">
            {questionNumber < totalQuestions ? 'Next Question →' : 'See Results! 🎉'}
          </Button>
        </div>
      )}
    </div>
  );
};

// ── Router: pick which card to render ────────────────────────────────────────
const QuestionCard = (props: QuestionCardProps) => {
  const isAudioQuestion = extractTapNumber(props.question.questionText) !== null;
  return isAudioQuestion
    ? <AudioNumberQuestion {...props} />
    : <StandardQuestion {...props} />;
};

export default QuestionCard;
