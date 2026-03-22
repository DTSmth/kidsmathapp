import { useState } from 'react';
import { createCheckoutSession } from '../services/subscription';

interface PaywallGateProps {
  childName: string;
  onDismiss: () => void;
}

const PaywallGate = ({ childName, onDismiss }: PaywallGateProps) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleUpgrade = async () => {
    setLoading(true);
    setError(null);
    try {
      const url = await createCheckoutSession();
      window.location.href = url;
    } catch {
      setError('Could not open checkout. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
      <div className="bg-white rounded-3xl shadow-2xl p-8 max-w-sm w-full text-center relative">
        {/* Mascot */}
        <div className="relative inline-block mb-4">
          <span className="text-6xl">🦁</span>
          <span className="absolute -bottom-1 -right-1 text-2xl">😕</span>
        </div>

        <h2 className="text-xl font-extrabold text-gray-800 mb-2">
          {childName} has more lessons waiting!
        </h2>

        <p className="text-gray-600 text-sm mb-4">
          Plus get {childName}'s weekly progress report straight to your inbox.
        </p>

        <div className="bg-primary/5 rounded-2xl p-4 mb-4">
          <p className="text-2xl font-extrabold text-primary">$4.99<span className="text-base font-normal text-gray-500"> / month</span></p>
          <p className="text-xs text-gray-500 mt-1">Cancel anytime</p>
        </div>

        {error && <p className="text-coral text-sm mb-3">{error}</p>}

        <button
          onClick={handleUpgrade}
          disabled={loading}
          className="w-full bg-primary text-white font-bold py-4 rounded-2xl hover:bg-primary/90 transition-colors disabled:opacity-60 mb-3"
        >
          {loading ? 'Opening checkout...' : `Unlock ${childName}'s lessons`}
        </button>

        <button
          onClick={onDismiss}
          className="text-gray-400 hover:text-gray-600 text-sm transition-colors"
        >
          🌙 {childName} gets 3 more free lessons tomorrow
        </button>
      </div>
    </div>
  );
};

export default PaywallGate;
