import { useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useChild } from '../context/ChildContext';
import Confetti from '../components/gamification/Confetti';
import Button from '../components/common/Button';
import api from '../services/api';

const GRADE_LABELS: Record<string, string> = {
  KINDERGARTEN: 'Kindergarten',
  GRADE_1: 'Grade 1',
  GRADE_2: 'Grade 2',
  GRADE_3: 'Grade 3',
  GRADE_4: 'Grade 4',
  GRADE_5: 'Grade 5',
};

const NEXT_GRADE: Record<string, string> = {
  KINDERGARTEN: 'GRADE_1',
  GRADE_1: 'GRADE_2',
  GRADE_2: 'GRADE_3',
  GRADE_3: 'GRADE_4',
  GRADE_4: 'GRADE_5',
};

const GraduationScreen = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { selectedChild } = useChild();

  const grade = location.state?.grade as string | undefined;
  const childName = location.state?.childName as string | undefined;

  const [advancing, setAdvancing] = useState(false);
  const displayName = childName || selectedChild?.name || 'Star Student';
  const gradeName = grade ? GRADE_LABELS[grade] || grade : 'this grade';
  const nextGrade = grade ? NEXT_GRADE[grade] : null;
  const nextGradeName = nextGrade ? GRADE_LABELS[nextGrade] || nextGrade : null;

  if (!grade) {
    navigate('/dashboard');
    return null;
  }

  const handleAdvanceGrade = async () => {
    if (!selectedChild || !nextGrade) return;
    setAdvancing(true);
    try {
      await api.post(`/children/${selectedChild.id}/advance-grade`);
      navigate('/dashboard', { replace: true });
    } catch {
      navigate('/dashboard', { replace: true });
    } finally {
      setAdvancing(false);
    }
  };

  const handleDownloadDiploma = () => {
    const canvas = document.createElement('canvas');
    canvas.width = 800;
    canvas.height = 600;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Background
    ctx.fillStyle = '#FFF9E8';
    ctx.fillRect(0, 0, 800, 600);

    // Border
    ctx.strokeStyle = '#4ECDC4';
    ctx.lineWidth = 12;
    ctx.strokeRect(20, 20, 760, 560);

    // Inner border
    ctx.strokeStyle = '#FFE66D';
    ctx.lineWidth = 4;
    ctx.strokeRect(35, 35, 730, 530);

    // Title
    ctx.fillStyle = '#4ECDC4';
    ctx.font = 'bold 48px serif';
    ctx.textAlign = 'center';
    ctx.fillText('Certificate of Achievement', 400, 120);

    // Stars
    ctx.font = '36px serif';
    ctx.fillText('⭐ ⭐ ⭐', 400, 170);

    // Child name
    ctx.fillStyle = '#333';
    ctx.font = '32px serif';
    ctx.fillText('This certifies that', 400, 230);

    ctx.fillStyle = '#FF6B6B';
    ctx.font = 'bold 52px serif';
    ctx.fillText(displayName, 400, 300);

    // Grade
    ctx.fillStyle = '#333';
    ctx.font = '28px serif';
    ctx.fillText(`has completed ${gradeName}`, 400, 360);
    ctx.fillText('with flying colors! 🎉', 400, 410);

    // Date
    ctx.fillStyle = '#888';
    ctx.font = '20px serif';
    ctx.fillText(new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }), 400, 520);

    const link = document.createElement('a');
    link.download = `diploma-${displayName.toLowerCase().replace(/\s/g, '-')}-${grade.toLowerCase()}.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-primary/10 flex flex-col items-center justify-center px-4 py-8 gap-6">
      <Confetti />

      {/* Trophy */}
      <div className="text-7xl animate-bounce">🎓</div>

      {/* Diploma card */}
      <div className="bg-white rounded-3xl border-4 border-primary shadow-2xl p-8 max-w-md w-full text-center">
        <div className="text-3xl mb-2">🏆</div>
        <h1 className="text-2xl font-extrabold text-primary mb-1">Certificate of Achievement</h1>
        <p className="text-gray-500 text-sm mb-4">⭐ ⭐ ⭐</p>
        <p className="text-gray-600 mb-1">This certifies that</p>
        <p className="text-3xl font-extrabold text-coral mb-2">{displayName}</p>
        <p className="text-gray-700">has completed <strong>{gradeName}</strong></p>
        <p className="text-gray-700">with flying colors! 🎉</p>
        <p className="text-gray-400 text-xs mt-4">{new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</p>
      </div>

      {/* Share button */}
      <button
        onClick={handleDownloadDiploma}
        className="flex items-center gap-2 bg-accent text-gray-800 font-bold px-6 py-3 rounded-2xl hover:bg-accent/80 transition-colors"
      >
        Share 📤
      </button>

      {/* Grade advancement prompt */}
      {nextGradeName && (
        <div className="bg-white rounded-3xl border-2 border-gray-200 p-6 max-w-md w-full text-center">
          <p className="text-lg font-bold text-gray-800 mb-4">
            Ready for {nextGradeName}? 🚀
          </p>
          <div className="flex flex-col gap-3">
            <Button
              variant="primary"
              fullWidth
              size="lg"
              onClick={handleAdvanceGrade}
              disabled={advancing}
            >
              {advancing ? 'Advancing...' : `Start ${nextGradeName} 🎯`}
            </Button>
            <button
              onClick={() => navigate('/dashboard')}
              className="text-gray-500 hover:text-gray-700 font-medium py-2 transition-colors text-sm"
            >
              Not Yet — Stay in {gradeName}
            </button>
          </div>
        </div>
      )}

      {!nextGradeName && (
        <Button variant="primary" size="lg" onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </Button>
      )}
    </div>
  );
};

export default GraduationScreen;
