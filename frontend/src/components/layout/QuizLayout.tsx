import { useNavigate } from 'react-router-dom';
import { X } from 'lucide-react';
import type { ReactNode } from 'react';

interface QuizLayoutProps {
  children: ReactNode;
  totalQuestions: number;
  currentQuestion: number; // 0-indexed
  onExit?: () => void;
}

const QuizLayout = ({ children, totalQuestions, currentQuestion, onExit }: QuizLayoutProps) => {
  const navigate = useNavigate();

  const handleExit = () => {
    if (onExit) onExit();
    else navigate(-1);
  };

  return (
    <div className="min-h-screen bg-background flex flex-col">
      {/* Minimal quiz bar */}
      <div className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-2xl mx-auto px-4 py-3 flex items-center gap-4">
          <button
            onClick={handleExit}
            className="text-gray-500 hover:text-coral transition-colors p-1 rounded-lg hover:bg-coral/10"
            aria-label="Exit quiz"
          >
            <X size={24} />
          </button>

          {/* Progress dots */}
          <div
            className="flex gap-2 flex-1 justify-center"
            aria-label={`Question ${currentQuestion + 1} of ${totalQuestions}`}
          >
            {Array.from({ length: totalQuestions }).map((_, i) => (
              <div
                key={i}
                className={`w-3 h-3 rounded-full transition-all duration-300 ${
                  i < currentQuestion
                    ? 'bg-primary'
                    : i === currentQuestion
                    ? 'bg-accent animate-pulse'
                    : 'bg-gray-200'
                }`}
              />
            ))}
          </div>

          {/* Spacer to balance the X button */}
          <div className="w-8" />
        </div>
      </div>

      {/* Page content */}
      <div className="flex-1 max-w-2xl mx-auto w-full px-4 py-6">
        {children}
      </div>
    </div>
  );
};

export default QuizLayout;
