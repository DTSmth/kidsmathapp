import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import { ChildCard, AddChildModal } from '../components/child';
import Button from '../components/common/Button';
import { Plus, RefreshCw, UserPlus, Sparkles } from 'lucide-react';
import type { Child } from '../types';

const SkeletonCard = () => (
  <div className="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm animate-pulse">
    <div className="w-24 h-24 rounded-full bg-gray-100 mx-auto mb-3" />
    <div className="h-5 bg-gray-100 rounded-lg w-2/3 mx-auto mb-2" />
    <div className="h-4 bg-gray-100 rounded-lg w-1/2 mx-auto" />
  </div>
);

const ChildSelect = () => {
  const { children, selectChild, loading, refreshChildren } = useChild();
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isRetrying, setIsRetrying] = useState(false);

  const handleSelectChild = (child: Child) => {
    selectChild(child);
    navigate('/dashboard');
  };

  const handleAddChildSuccess = async () => {
    await refreshChildren();
  };

  const handleRetry = async () => {
    setIsRetrying(true);
    setError(null);
    try {
      await refreshChildren();
    } catch {
      setError('Failed to load children. Please try again.');
    } finally {
      setIsRetrying(false);
    }
  };

  // Loading state
  if (loading && !isRetrying) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-primary/5 px-4 py-8">
        <div className="max-w-3xl mx-auto">
          <div className="text-center mb-10">
            <h1 className="text-2xl font-bold text-gray-800 mb-2">
              Loading profiles...
            </h1>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-primary/5 px-4 py-8 flex items-center justify-center">
        <div className="text-center max-w-md">
          <div className="w-16 h-16 bg-coral/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <RefreshCw className="w-8 h-8 text-coral" />
          </div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">Something went wrong</h2>
          <p className="text-gray-600 text-sm mb-6">{error}</p>
          <Button
            variant="primary"
            onClick={handleRetry}
            loading={isRetrying}
          >
            Try again
          </Button>
        </div>
      </div>
    );
  }

  // Empty state
  if (children.length === 0) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-primary/5 px-4 py-8 flex items-center justify-center">
        <div className="text-center max-w-md">
          <div className="w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-6">
            <Sparkles className="w-10 h-10 text-primary" />
          </div>
          <h1 className="text-2xl font-bold text-gray-800 mb-3">
            Welcome to KidsMath!
          </h1>
          <p className="text-gray-600 mb-8">
            Add your first learner to start the adventure.
          </p>
          <Button
            variant="primary"
            size="lg"
            onClick={() => setIsModalOpen(true)}
          >
            <UserPlus className="w-5 h-5" />
            Add your first child
          </Button>
        </div>

        <AddChildModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          onSuccess={handleAddChildSuccess}
        />
      </div>
    );
  }

  // Main view
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-primary/5 px-4 py-8">
      <div className="max-w-3xl mx-auto">
        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-2xl md:text-3xl font-bold text-gray-800 mb-2">
            Who's learning today?
          </h1>
          <p className="text-gray-600">
            Select a profile to continue
          </p>
        </div>

        {/* Children Grid */}
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {children.map((child) => (
            <ChildCard
              key={child.id}
              child={child}
              onClick={() => handleSelectChild(child)}
            />
          ))}

          {/* Add Child Card */}
          <div
            onClick={() => setIsModalOpen(true)}
            onKeyDown={(e) => e.key === 'Enter' && setIsModalOpen(true)}
            role="button"
            tabIndex={0}
            className="
              bg-white rounded-2xl border-2 border-dashed border-gray-200 p-5
              transition-all duration-150
              hover:border-primary/50 hover:bg-primary/5
              cursor-pointer
              flex flex-col items-center justify-center
              min-h-[180px]
              focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary
            "
          >
            <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mb-3">
              <Plus className="w-6 h-6 text-gray-400" />
            </div>
            <span className="text-sm font-medium text-gray-600">Add child</span>
          </div>
        </div>
      </div>

      <AddChildModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleAddChildSuccess}
      />
    </div>
  );
};

export default ChildSelect;
