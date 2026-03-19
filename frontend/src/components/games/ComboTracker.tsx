interface ComboTrackerProps {
  combo: number;
  visible: boolean;
  signatureColor?: string;
}

const ComboTracker = ({ combo, visible, signatureColor = '#FFE66D' }: ComboTrackerProps) => {
  if (!visible || combo < 3) return null;

  return (
    <div className="fixed bottom-24 left-1/2 -translate-x-1/2 z-50 pointer-events-none animate-combo-burst">
      <div
        className="px-5 py-2 rounded-full font-bold text-white text-lg shadow-lg flex items-center gap-2"
        style={{ backgroundColor: signatureColor === '#FFE66D' ? '#F59E0B' : signatureColor }}
      >
        <span>🔥</span>
        <span>COMBO x{combo}!</span>
      </div>
    </div>
  );
};

export default ComboTracker;
