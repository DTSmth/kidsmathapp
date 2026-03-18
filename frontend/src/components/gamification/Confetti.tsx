import { useState } from 'react';

const COLORS = ['#4ECDC4', '#FFE66D', '#FF6B6B', '#A084E8', '#ffffff'];

interface Particle {
  id: number;
  color: string;
  left: number;
  delay: number;
  duration: number;
  size: number;
  isRound: boolean;
}

const Confetti = () => {
  const [particles] = useState<Particle[]>(() =>
    Array.from({ length: 60 }, (_, i) => ({
      id: i,
      color: COLORS[i % COLORS.length],
      left: Math.random() * 100,
      delay: Math.random() * 0.8,
      duration: 2 + Math.random() * 1,
      size: 6 + Math.random() * 8,
      isRound: Math.random() > 0.4,
    }))
  );

  return (
    <div className="fixed inset-0 pointer-events-none z-40 overflow-hidden" aria-hidden="true">
      {particles.map(p => (
        <div
          key={p.id}
          className="absolute top-0"
          style={{
            left: `${p.left}%`,
            width: p.size,
            height: p.size,
            backgroundColor: p.color,
            borderRadius: p.isRound ? '50%' : '2px',
            transform: p.isRound ? 'none' : 'rotate(45deg)',
            animation: `confettiFall ${p.duration}s ease-in ${p.delay}s forwards`,
          }}
        />
      ))}
    </div>
  );
};

export default Confetti;
