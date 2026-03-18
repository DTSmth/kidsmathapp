import { useState } from 'react';
import { X } from 'lucide-react';
import Button from '../common/Button';
import AvatarSelector from './AvatarSelector';
import { childService, handleApiError } from '../../services/api';
import { GRADE_LEVELS, type AvatarId, type GradeLevel } from '../../types';

interface AddChildModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const AddChildModal = ({ isOpen, onClose, onSuccess }: AddChildModalProps) => {
  const [name, setName] = useState('');
  const [avatarId, setAvatarId] = useState<AvatarId | null>(null);
  const [gradeLevel, setGradeLevel] = useState<GradeLevel>('KINDERGARTEN');
  const [birthDate, setBirthDate] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const resetForm = () => {
    setName('');
    setAvatarId(null);
    setGradeLevel('KINDERGARTEN');
    setBirthDate('');
    setError(null);
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Please enter a name');
      return;
    }

    if (!avatarId) {
      setError('Please select an avatar');
      return;
    }

    setIsSubmitting(true);

    try {
      await childService.createChild({
        name: name.trim(),
        avatarId,
        gradeLevel,
        ...(birthDate && { birthDate }),
      });
      resetForm();
      onSuccess();
      onClose();
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={handleClose}
      />

      {/* Modal */}
      <div className="relative bg-white rounded-3xl shadow-2xl w-full max-w-md max-h-[90vh] overflow-y-auto animate-in fade-in zoom-in duration-200">
        {/* Header */}
        <div className="sticky top-0 bg-white rounded-t-3xl border-b border-gray-100 p-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-gray-800">
            🎉 Add a New Learner!
          </h2>
          <button
            onClick={handleClose}
            className="p-2 rounded-full hover:bg-gray-100 transition-colors"
            aria-label="Close"
          >
            <X size={24} className="text-gray-500" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Error Message */}
          {error && (
            <div className="bg-coral/10 border-2 border-coral rounded-2xl p-4 text-coral-dark font-medium">
              {error}
            </div>
          )}

          {/* Name Input */}
          <div>
            <label htmlFor="childName" className="block text-lg font-semibold text-gray-700 mb-2">
              What's their name?
            </label>
            <input
              type="text"
              id="childName"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter name..."
              className="
                w-full px-4 py-3 rounded-2xl
                border-3 border-gray-200
                focus:border-primary focus:ring-4 focus:ring-primary/20
                outline-none transition-all
                text-lg font-medium
              "
              autoFocus
            />
          </div>

          {/* Avatar Selection */}
          <div>
            <label className="block text-lg font-semibold text-gray-700 mb-3">
              Pick a mascot friend!
            </label>
            <AvatarSelector selected={avatarId} onSelect={setAvatarId} />
          </div>

          {/* Grade Level */}
          <div>
            <label htmlFor="gradeLevel" className="block text-lg font-semibold text-gray-700 mb-2">
              Grade Level
            </label>
            <select
              id="gradeLevel"
              value={gradeLevel}
              onChange={(e) => setGradeLevel(e.target.value as GradeLevel)}
              className="
                w-full px-4 py-3 rounded-2xl
                border-3 border-gray-200
                focus:border-primary focus:ring-4 focus:ring-primary/20
                outline-none transition-all
                text-lg font-medium
                bg-white
              "
            >
              {GRADE_LEVELS.map((grade) => (
                <option key={grade.value} value={grade.value}>
                  {grade.label}
                </option>
              ))}
            </select>
          </div>

          {/* Birth Date (Optional) */}
          <div>
            <label htmlFor="birthDate" className="block text-lg font-semibold text-gray-700 mb-2">
              Birth Date <span className="text-sm text-gray-400">(optional)</span>
            </label>
            <input
              type="date"
              id="birthDate"
              value={birthDate}
              onChange={(e) => setBirthDate(e.target.value)}
              className="
                w-full px-4 py-3 rounded-2xl
                border-3 border-gray-200
                focus:border-primary focus:ring-4 focus:ring-primary/20
                outline-none transition-all
                text-lg font-medium
              "
            />
          </div>

          {/* Buttons */}
          <div className="flex gap-4 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={handleClose}
              className="flex-1"
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              className="flex-1"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="animate-spin">🌟</span>
                  Creating...
                </span>
              ) : (
                "Let's Go! 🚀"
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddChildModal;
