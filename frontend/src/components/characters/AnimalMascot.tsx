interface AnimalMascotProps {
  animal: 'leo' | 'ollie' | 'bella' | 'max';
  mood?: 'happy' | 'excited' | 'thinking' | 'celebrating';
  size?: 'sm' | 'md' | 'lg';
  message?: string;
}

const animalEmojis = {
  leo: '🦁',
  ollie: '🦉',
  bella: '🐰',
  max: '🐵',
};

const moodAnimations = {
  happy: 'animate-bounce',
  excited: 'animate-pulse',
  thinking: '',
  celebrating: 'animate-bounce',
};

const sizeStyles = {
  sm: 'text-4xl',
  md: 'text-6xl',
  lg: 'text-8xl',
};

const AnimalMascot = ({
  animal,
  mood = 'happy',
  size = 'md',
  message,
}: AnimalMascotProps) => {
  return (
    <div className="flex flex-col items-center gap-3">
      <div className={`${sizeStyles[size]} ${moodAnimations[mood]}`}>
        {animalEmojis[animal]}
        {mood === 'celebrating' && <span className="ml-2">🎉</span>}
      </div>
      {message && (
        <div className="bg-white rounded-2xl px-4 py-2 shadow-md border-2 border-primary relative">
          <div className="absolute -top-2 left-1/2 -translate-x-1/2 w-0 h-0 border-l-8 border-r-8 border-b-8 border-transparent border-b-primary" />
          <p className="font-semibold text-gray-700">{message}</p>
        </div>
      )}
    </div>
  );
};

export default AnimalMascot;
