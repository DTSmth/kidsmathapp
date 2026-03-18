import type { ChildSummaryDto } from '../../types';
import Avatar from '../common/Avatar';
import { Star, Flame } from 'lucide-react';

interface ChildCardProps {
  child: ChildSummaryDto;
  onClick: () => void;
}

const ChildCard = ({ child, onClick }: ChildCardProps) => {
  return (
    <div
      onClick={onClick}
      onKeyDown={(e) => e.key === 'Enter' && onClick()}
      role="button"
      tabIndex={0}
      className="
        bg-white rounded-2xl border border-gray-200 p-5 shadow-sm
        transition-all duration-150
        hover:shadow-md hover:border-primary/40 hover:-translate-y-0.5
        cursor-pointer
        flex flex-col items-center
        focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary
      "
    >
      <Avatar avatarId={child.avatarId} size="xl" className="mb-3" />
      <h2 className="text-lg font-semibold text-gray-800 mb-2">{child.name}</h2>
      <div className="flex items-center gap-3 text-sm">
        <div className="flex items-center gap-1 text-amber-600">
          <Star className="w-4 h-4 fill-current" />
          <span className="font-medium">{child.totalStars}</span>
        </div>
        {child.currentStreak > 0 && (
          <div className="flex items-center gap-1 text-orange-500">
            <Flame className="w-4 h-4" />
            <span className="font-medium">{child.currentStreak}d</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChildCard;
