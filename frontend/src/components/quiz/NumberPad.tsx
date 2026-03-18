import { useState } from 'react';

interface NumberPadProps {
  onSubmit: (value: string) => void;
  disabled?: boolean;
}

const NumberPad = ({ onSubmit, disabled = false }: NumberPadProps) => {
  const [input, setInput] = useState('');

  const handleDigit = (digit: string) => {
    if (input.length >= 4) return;
    setInput(prev => prev + digit);
  };

  const handleBackspace = () => setInput(prev => prev.slice(0, -1));

  const handleSubmit = () => {
    if (!input) return;
    onSubmit(input);
    setInput('');
  };

  // Keyboard support
  const handleKey = (e: React.KeyboardEvent) => {
    if (e.key >= '0' && e.key <= '9') handleDigit(e.key);
    else if (e.key === 'Backspace') handleBackspace();
    else if (e.key === 'Enter') handleSubmit();
  };

  const btnBase = 'rounded-2xl font-bold text-2xl active:scale-95 transition-all duration-150 flex items-center justify-center';
  const minSize = 'min-h-[56px] min-w-[56px]';

  return (
    <div className="flex flex-col items-center gap-3" onKeyDown={handleKey} tabIndex={0}>
      {/* Input display */}
      <div className={`w-full rounded-xl bg-gray-100 h-14 flex items-center justify-center text-2xl font-bold text-gray-800 ${!input ? 'text-gray-400' : ''}`}>
        {input || '?'}
      </div>

      {/* Digit grid */}
      <div className="grid grid-cols-3 gap-3 w-full max-w-[240px]">
        {['1','2','3','4','5','6','7','8','9'].map(d => (
          <button
            key={d}
            onClick={() => handleDigit(d)}
            disabled={disabled}
            aria-label={`Number ${d}`}
            className={`${btnBase} ${minSize} bg-primary text-white hover:bg-primary-dark disabled:opacity-50`}
          >
            {d}
          </button>
        ))}
        <button
          onClick={handleBackspace}
          disabled={disabled || !input}
          aria-label="Backspace"
          className={`${btnBase} ${minSize} bg-coral/20 text-coral hover:bg-coral/30 disabled:opacity-50`}
        >
          ⌫
        </button>
        <button
          onClick={() => handleDigit('0')}
          disabled={disabled}
          aria-label="Number 0"
          className={`${btnBase} ${minSize} bg-primary text-white hover:bg-primary-dark disabled:opacity-50`}
        >
          0
        </button>
        <button
          onClick={handleSubmit}
          disabled={disabled || !input}
          aria-label="Submit answer"
          className={`${btnBase} ${minSize} bg-success text-white hover:bg-success-dark disabled:opacity-50 disabled:bg-gray-200 disabled:text-gray-400`}
        >
          ✓
        </button>
      </div>
    </div>
  );
};

export default NumberPad;
