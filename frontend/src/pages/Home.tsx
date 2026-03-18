import { Link } from 'react-router-dom';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import { Sparkles, Gamepad2, Trophy, BarChart3, ArrowRight } from 'lucide-react';

const Home = () => {
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-primary/5 relative overflow-hidden">
      {/* Subtle background pattern */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-primary/10 rounded-full blur-3xl" />
        <div className="absolute top-1/2 -left-40 w-80 h-80 bg-coral/10 rounded-full blur-3xl" />
        <div className="absolute bottom-0 right-1/4 w-72 h-72 bg-accent/10 rounded-full blur-3xl" />
      </div>

      {/* Main content */}
      <div className="relative z-10 flex flex-col min-h-screen">
        {/* Header */}
        <header className="px-4 py-6">
          <div className="max-w-6xl mx-auto flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 bg-gradient-to-br from-primary to-primary-dark rounded-xl flex items-center justify-center shadow-md">
                <Sparkles className="w-5 h-5 text-white" />
              </div>
              <span className="text-xl font-bold">
                <span className="text-primary">Kids</span>
                <span className="text-coral">Math</span>
              </span>
            </div>
            <div className="flex items-center gap-3">
              <Link to="/login">
                <Button variant="ghost" size="sm">
                  Sign in
                </Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="sm">
                  Get started
                </Button>
              </Link>
            </div>
          </div>
        </header>

        {/* Hero */}
        <main className="flex-1 flex flex-col items-center justify-center px-4 py-12">
          <div className="text-center max-w-3xl mx-auto">
            {/* Mascots */}
            <div className="flex justify-center items-end gap-3 mb-8">
              <span className="text-5xl md:text-6xl transform -rotate-6 hover:scale-110 transition-transform cursor-default">🦉</span>
              <span className="text-6xl md:text-7xl hover:scale-110 transition-transform cursor-default">🦁</span>
              <span className="text-5xl md:text-6xl transform rotate-6 hover:scale-110 transition-transform cursor-default">🐰</span>
            </div>

            {/* Title */}
            <h1 className="text-4xl md:text-6xl font-bold text-gray-800 mb-4 tracking-tight">
              Make math{' '}
              <span className="text-primary">fun</span>
              {' '}for kids
            </h1>

            {/* Subtitle */}
            <p className="text-lg md:text-xl text-gray-600 mb-8 max-w-xl mx-auto leading-relaxed">
              Join our friendly animal guides on an exciting journey through numbers, 
              shapes, and puzzles designed for young learners.
            </p>

            {/* CTA */}
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <Link to="/register">
                <Button variant="primary" size="lg" className="min-w-[200px]">
                  Start learning free
                  <ArrowRight className="w-5 h-5 ml-1" />
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="ghost" size="lg" className="min-w-[200px]">
                  I have an account
                </Button>
              </Link>
            </div>
          </div>

          {/* Features */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-5 max-w-4xl mx-auto mt-16 px-4">
            <Card hoverable className="text-center">
              <div className="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center mx-auto mb-4">
                <Gamepad2 className="w-6 h-6 text-primary" />
              </div>
              <h3 className="text-lg font-semibold text-gray-800 mb-2">Fun Games</h3>
              <p className="text-gray-600 text-sm">
                Learn through exciting games that make math feel like playtime.
              </p>
            </Card>

            <Card hoverable className="text-center">
              <div className="w-12 h-12 bg-accent/20 rounded-xl flex items-center justify-center mx-auto mb-4">
                <Trophy className="w-6 h-6 text-amber-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-800 mb-2">Earn Rewards</h3>
              <p className="text-gray-600 text-sm">
                Collect stars and badges as you master new skills.
              </p>
            </Card>

            <Card hoverable className="text-center">
              <div className="w-12 h-12 bg-coral/10 rounded-xl flex items-center justify-center mx-auto mb-4">
                <BarChart3 className="w-6 h-6 text-coral" />
              </div>
              <h3 className="text-lg font-semibold text-gray-800 mb-2">Track Progress</h3>
              <p className="text-gray-600 text-sm">
                Parents can see how their child is growing and improving.
              </p>
            </Card>
          </div>
        </main>

        {/* Footer */}
        <footer className="py-6 text-center">
          <p className="text-gray-400 text-sm">
            Perfect for ages 4-12 · Aligned with school curriculum · Loved by families
          </p>
        </footer>
      </div>
    </div>
  );
};

export default Home;
