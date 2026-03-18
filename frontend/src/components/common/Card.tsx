import type { ReactNode, KeyboardEvent } from 'react';

type CardColor = 'primary' | 'accent' | 'coral' | 'purple' | 'success' | 'none';

interface CardProps {
  children: ReactNode;
  color?: CardColor;
  className?: string;
  onClick?: () => void;
  hoverable?: boolean;
}

const colorStyles: Record<CardColor, string> = {
  primary: 'bg-primary/5 border-primary/20 hover:border-primary/40',
  accent: 'bg-accent/10 border-accent/30 hover:border-accent/50',
  coral: 'bg-coral/5 border-coral/20 hover:border-coral/40',
  purple: 'bg-purple/5 border-purple/20 hover:border-purple/40',
  success: 'bg-success/10 border-success/30 hover:border-success/50',
  none: 'bg-white border-gray-200 hover:border-gray-300',
};

const Card = ({
  children,
  color = 'none',
  className = '',
  onClick,
  hoverable = false,
}: CardProps) => {
  const handleKeyDown = (e: KeyboardEvent) => {
    if (onClick && (e.key === 'Enter' || e.key === ' ')) {
      e.preventDefault();
      onClick();
    }
  };

  return (
    <div
      className={`
        rounded-2xl border p-5 shadow-sm
        transition-all duration-150
        ${colorStyles[color]}
        ${hoverable || onClick ? 'cursor-pointer hover:shadow-md hover:-translate-y-0.5' : ''}
        ${onClick ? 'focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary' : ''}
        ${className}
      `}
      onClick={onClick}
      onKeyDown={handleKeyDown}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
    >
      {children}
    </div>
  );
};

export default Card;
