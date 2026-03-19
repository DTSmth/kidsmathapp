import { useState, useEffect } from 'react';
import type { QuestionDto } from '../../types';

interface Props {
  question: QuestionDto;
  onAnswer: (answer: string) => void;
}

const OBJECT_EMOJIS = ['🍎', '🌟', '🐶', '🎈', '🍕', '🚗', '🌸', '🦋'];

// Parse count from question text like "Put 5 apples in the basket"
const parseTarget = (text: string): number => {
  const match = text.match(/\d+/);
  return match ? parseInt(match[0]) : 3;
};

const VisualBuilderQuestion = ({ question, onAnswer }: Props) => {
  const target = parseTarget(question.questionText);
  const emoji = OBJECT_EMOJIS[question.id % OBJECT_EMOJIS.length];

  // Items in the source pool
  const totalItems = target + 3;
  const [basket, setBasket] = useState<number[]>([]);
  const [submitted, setSubmitted] = useState(false);
  const [isDragOver, setIsDragOver] = useState(false);

  useEffect(() => {
    setBasket([]);
    setSubmitted(false);
  }, [question.id]);

  const addToBasket = (itemId: number) => {
    if (submitted || basket.includes(itemId)) return;
    setBasket(prev => [...prev, itemId]);
  };

  const removeFromBasket = (itemId: number) => {
    if (submitted) return;
    setBasket(prev => prev.filter(id => id !== itemId));
  };

  const handleSubmit = () => {
    if (submitted) return;
    setSubmitted(true);
    onAnswer(String(basket.length));
  };

  const sourceItems = Array.from({ length: totalItems }, (_, i) => i).filter(
    id => !basket.includes(id)
  );

  return (
    <div className="flex flex-col gap-3 w-full">
      {/* Instructions */}
      <div className="bg-accent/20 rounded-xl px-4 py-2 text-center">
        <p className="text-sm font-semibold text-gray-700">{question.questionText}</p>
      </div>

      <div className="flex gap-3">
        {/* Source pool */}
        <div className="flex-1 bg-gray-50 rounded-2xl border-2 border-gray-200 p-3 min-h-[100px]">
          <p className="text-xs font-semibold text-gray-400 mb-2 text-center">Tap to add</p>
          <div className="flex flex-wrap gap-2 justify-center">
            {sourceItems.map(id => (
              <button
                key={id}
                onClick={() => addToBasket(id)}
                disabled={submitted}
                className="text-3xl hover:scale-110 active:scale-95 transition-transform focus:outline-none"
                aria-label={`Add ${emoji}`}
              >
                {emoji}
              </button>
            ))}
          </div>
        </div>

        {/* Basket */}
        <div
          className={`flex-1 rounded-2xl border-2 p-3 min-h-[100px] transition-colors
            ${isDragOver ? 'border-primary bg-primary/10' : 'border-primary/40 bg-primary/5'}`}
          onDragOver={e => { e.preventDefault(); setIsDragOver(true); }}
          onDragLeave={() => setIsDragOver(false)}
          onDrop={() => setIsDragOver(false)}
        >
          <div className="flex items-center justify-between mb-2">
            <p className="text-xs font-semibold text-gray-400">🧺 Basket</p>
            <span
              className={`text-lg font-bold tabular-nums ${
                basket.length === target ? 'text-green-600' : 'text-primary'
              }`}
            >
              {basket.length}
            </span>
          </div>
          <div className="flex flex-wrap gap-2 justify-center">
            {basket.map(id => (
              <button
                key={id}
                onClick={() => removeFromBasket(id)}
                disabled={submitted}
                className="text-3xl hover:scale-110 active:scale-95 transition-transform focus:outline-none"
                aria-label={`Remove ${emoji}`}
              >
                {emoji}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Submit */}
      <button
        onClick={handleSubmit}
        disabled={submitted || basket.length === 0}
        className="w-full bg-primary text-white font-bold rounded-2xl py-3 shadow-md hover:bg-primary-dark transition-colors disabled:opacity-50"
      >
        That's {basket.length}! ✓
      </button>
    </div>
  );
};

export default VisualBuilderQuestion;
