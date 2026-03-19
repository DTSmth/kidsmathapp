import { useState, useEffect } from 'react';
import type { QuestionDto } from '../../types';
import { useSoundEffects } from '../../hooks/useSoundEffects';

const SHAPE_EMOJIS: Record<string, string> = {
  triangle: '▲',
  circle: '●',
  square: '■',
  rectangle: '▬',
  hexagon: '⬡',
  star: '★',
  diamond: '◆',
  oval: '⬭',
};

const ALL_SHAPES = Object.keys(SHAPE_EMOJIS);

interface SceneShape {
  id: string;
  shape: string;
  emoji: string;
  x: number;
  y: number;
  size: number;
  color: string;
  found: boolean;
  wrong: boolean;
}

const COLORS = ['#FF6B6B', '#4ECDC4', '#A084E8', '#FFE66D', '#95D5B2', '#F97316'];

interface Props {
  question: QuestionDto;
  onCorrect: (questionId: number) => void;
  onWrong?: (questionId: number) => void;
}

const ShapeSafariGame = ({ question, onCorrect, onWrong }: Props) => {
  const [shapes, setShapes] = useState<SceneShape[]>([]);
  const [found, setFound] = useState(0);
  const { playDing, playBoing } = useSoundEffects();

  // Parse target shape from question text
  const targetShape = ALL_SHAPES.find(s =>
    question.questionText.toLowerCase().includes(s)
  ) || question.correctAnswer.toLowerCase();
  const targetEmoji = SHAPE_EMOJIS[targetShape] || '▲';

  // Count how many targets in options or derive from question
  const targetCount = question.options?.length > 0 ? 3 : 3;

  useEffect(() => {
    // Place target shapes + distractors randomly
    const sceneShapes: SceneShape[] = [];
    // Add target shapes
    for (let i = 0; i < targetCount; i++) {
      sceneShapes.push({
        id: `target-${i}`,
        shape: targetShape,
        emoji: targetEmoji,
        x: 5 + Math.random() * 85,
        y: 5 + Math.random() * 80,
        size: 40 + Math.random() * 20,
        color: COLORS[Math.floor(Math.random() * COLORS.length)],
        found: false,
        wrong: false,
      });
    }
    // Add distractors
    const distractors = ALL_SHAPES.filter(s => s !== targetShape);
    for (let i = 0; i < 8; i++) {
      const shape = distractors[Math.floor(Math.random() * distractors.length)];
      sceneShapes.push({
        id: `distract-${i}`,
        shape,
        emoji: SHAPE_EMOJIS[shape],
        x: 5 + Math.random() * 85,
        y: 5 + Math.random() * 80,
        size: 32 + Math.random() * 16,
        color: COLORS[Math.floor(Math.random() * COLORS.length)],
        found: false,
        wrong: false,
      });
    }
    setShapes(sceneShapes.sort(() => Math.random() - 0.5));
    setFound(0);
  }, [question.id]);

  const handleTap = (shape: SceneShape) => {
    if (shape.found || shape.wrong) return;
    if (shape.shape === targetShape) {
      playDing();
      setShapes(prev => prev.map(s => s.id === shape.id ? { ...s, found: true } : s));
      const newFound = found + 1;
      setFound(newFound);
      if (newFound >= targetCount) {
        setTimeout(() => onCorrect(question.id), 400);
      }
    } else {
      playBoing();
      // Wrong tap: shake animation only — do NOT advance to next question.
      // ShapeSafari is find-all-targets style; player stays on same scene.
      setShapes(prev => prev.map(s => s.id === shape.id ? { ...s, wrong: true } : s));
      setTimeout(() => setShapes(prev => prev.map(s => s.id === shape.id ? { ...s, wrong: false } : s)), 500);
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Target banner */}
      <div className="px-4 pt-2 pb-1 shrink-0">
        <div className="bg-purple/10 border-2 border-purple rounded-xl px-4 py-2 flex items-center justify-between">
          <p className="font-bold text-purple text-sm">{question.questionText}</p>
          <div className="flex items-center gap-1">
            <span className="text-2xl">{targetEmoji}</span>
            <span className="text-xs font-semibold text-purple">{found}/{targetCount}</span>
          </div>
        </div>
      </div>

      {/* Safari scene */}
      <div className="flex-1 relative bg-green-100 rounded-2xl mx-4 mb-4 overflow-hidden border-2 border-green-300">
        {shapes.map(shape => (
          <button
            key={shape.id}
            onClick={() => handleTap(shape)}
            className={`absolute flex items-center justify-center rounded focus:outline-none transition-transform
              ${shape.found ? 'opacity-30 cursor-default scale-75' : ''}
              ${shape.wrong ? 'animate-shake' : 'hover:scale-110 active:scale-95'}`}
            style={{
              left: `${shape.x}%`,
              top: `${shape.y}%`,
              fontSize: shape.size,
              color: shape.found ? '#9CA3AF' : shape.color,
              transform: `translate(-50%, -50%)`,
              minWidth: 48,
              minHeight: 48,
            }}
            aria-label={`${shape.shape} shape`}
            disabled={shape.found}
          >
            {shape.emoji}
          </button>
        ))}
      </div>
    </div>
  );
};

export default ShapeSafariGame;
