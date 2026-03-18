import { CheckCircle, ArrowRight, Calendar } from 'lucide-react';

interface DailyChallengeBannerProps {
  complete: boolean;
  onStartChallenge?: () => void;
}

const DailyChallengeBanner = ({ complete, onStartChallenge }: DailyChallengeBannerProps) => {
  if (complete) {
    return (
      <div className="bg-success/10 border border-success/30 rounded-xl px-4 py-3 flex items-center gap-3">
        <div className="w-10 h-10 bg-success/20 rounded-full flex items-center justify-center">
          <CheckCircle className="w-5 h-5 text-success-dark" />
        </div>
        <div>
          <p className="font-semibold text-gray-800 text-sm">You practiced today!</p>
          <p className="text-xs text-gray-600">Keep the streak going</p>
        </div>
      </div>
    );
  }

  return (
    <button
      onClick={onStartChallenge}
      className="w-full bg-accent/10 border border-accent/30 hover:bg-accent/20 rounded-xl px-4 py-3 flex items-center gap-3 transition-colors duration-150 text-left"
    >
      <div className="w-10 h-10 bg-accent/30 rounded-full flex items-center justify-center">
        <Calendar className="w-5 h-5 text-amber-600" />
      </div>
      <div className="flex-1">
        <p className="font-semibold text-gray-800 text-sm">Start today's practice</p>
        <p className="text-xs text-gray-600">Pick any topic to begin</p>
      </div>
      <ArrowRight className="w-5 h-5 text-gray-400" />
    </button>
  );
};

export default DailyChallengeBanner;
