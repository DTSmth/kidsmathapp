interface AvatarProps {
  avatarId: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}

const avatarEmojis: Record<string, string> = {
  leo: '🦁',
  ollie: '🦉',
  bella: '🐰',
  max: '🐵',
};

const sizeStyles = {
  sm: 'w-10 h-10 text-2xl',
  md: 'w-14 h-14 text-3xl',
  lg: 'w-20 h-20 text-5xl',
  xl: 'w-28 h-28 text-6xl',
};

const Avatar = ({ avatarId, size = 'md', className = '' }: AvatarProps) => {
  const emoji = avatarEmojis[avatarId.toLowerCase()] || '👤';

  return (
    <div
      className={`
        flex items-center justify-center
        bg-gradient-to-br from-accent-light to-accent
        rounded-full
        shadow-md
        ${sizeStyles[size]}
        ${className}
      `}
    >
      <span role="img" aria-label={avatarId}>
        {emoji}
      </span>
    </div>
  );
};

export default Avatar;
