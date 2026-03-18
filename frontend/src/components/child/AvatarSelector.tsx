import { AVATARS, type AvatarId } from '../../types';

interface AvatarSelectorProps {
  selected: AvatarId | null;
  onSelect: (avatarId: AvatarId) => void;
}

const AvatarSelector = ({ selected, onSelect }: AvatarSelectorProps) => {
  return (
    <div className="grid grid-cols-2 gap-4">
      {AVATARS.map((avatar) => (
        <button
          key={avatar.id}
          type="button"
          onClick={() => onSelect(avatar.id)}
          className={`
            flex flex-col items-center p-4 rounded-2xl
            transition-all duration-200
            border-4
            ${
              selected === avatar.id
                ? 'border-primary bg-primary/10 ring-4 ring-primary/30 scale-105'
                : 'border-gray-200 bg-white hover:border-primary-light hover:scale-102'
            }
            focus:outline-none focus:ring-4 focus:ring-primary/50
          `}
        >
          <span className="text-5xl mb-2">{avatar.emoji}</span>
          <span className="font-semibold text-gray-700">{avatar.name}</span>
        </button>
      ))}
    </div>
  );
};

export default AvatarSelector;
