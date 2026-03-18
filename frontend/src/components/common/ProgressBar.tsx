interface ProgressBarProps {
  progress: number;
  label?: string;
  showPercentage?: boolean;
  height?: 'sm' | 'md' | 'lg';
  color?: 'primary' | 'success' | 'accent';
}

const heightStyles = {
  sm: 'h-1.5',
  md: 'h-2',
  lg: 'h-3',
};

const colorStyles = {
  primary: 'bg-primary',
  success: 'bg-success',
  accent: 'bg-accent',
};

const ProgressBar = ({
  progress,
  label,
  showPercentage = true,
  height = 'md',
  color = 'primary',
}: ProgressBarProps) => {
  const clampedProgress = Math.min(100, Math.max(0, progress));

  return (
    <div className="w-full">
      {label && (
        <div className="flex justify-between items-center mb-1.5">
          <span className="text-sm font-medium text-gray-700">{label}</span>
          {showPercentage && (
            <span className="text-sm font-semibold text-gray-600">{Math.round(clampedProgress)}%</span>
          )}
        </div>
      )}
      <div className={`w-full bg-gray-100 rounded-full ${heightStyles[height]} overflow-hidden`}>
        <div
          className={`${heightStyles[height]} rounded-full ${colorStyles[color]} transition-all duration-300 ease-out`}
          style={{ width: `${clampedProgress}%` }}
        />
      </div>
    </div>
  );
};

export default ProgressBar;
