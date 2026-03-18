import { useState, useEffect } from 'react';

interface StarCounterProps {
  count: number;
  animated?: boolean;
}

const StarCounter = ({ count, animated = true }: StarCounterProps) => {
  const [displayCount, setDisplayCount] = useState(animated ? 0 : count);

  useEffect(() => {
    if (!animated) {
      setDisplayCount(count);
      return;
    }

    const duration = 1000;
    const steps = 20;
    const increment = count / steps;
    let current = 0;

    const timer = setInterval(() => {
      current += increment;
      if (current >= count) {
        setDisplayCount(count);
        clearInterval(timer);
      } else {
        setDisplayCount(Math.floor(current));
      }
    }, duration / steps);

    return () => clearInterval(timer);
  }, [count, animated]);

  return (
    <div className="flex items-center gap-2 bg-accent/30 px-4 py-2 rounded-full">
      <span className="text-2xl animate-pulse">⭐</span>
      <span className="font-bold text-xl text-gray-800">{displayCount}</span>
    </div>
  );
};

export default StarCounter;
