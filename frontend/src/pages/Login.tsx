import { useState, useEffect } from 'react';
import type { FormEvent, ChangeEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import AuthLayout from '../components/layout/AuthLayout';
import { Mail, Lock, Eye, EyeOff, ArrowLeft, Sparkles, AlertCircle, CheckCircle } from 'lucide-react';

const validateEmail = (email: string): string | null => {
  if (!email) return null;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return "Please enter a valid email address";
  }
  return null;
};

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);
  const [touched, setTouched] = useState({ email: false, password: false });
  const { login, loading, error } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (touched.email) {
      setEmailError(validateEmail(email));
    }
  }, [email, touched.email]);

  const handleEmailChange = (e: ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
  };

  const handleEmailBlur = () => {
    setTouched(prev => ({ ...prev, email: true }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    
    const emailErr = validateEmail(email);
    if (emailErr) {
      setEmailError(emailErr);
      setTouched({ email: true, password: true });
      return;
    }

    try {
      await login(email, password);
      setShowSuccess(true);
      setTimeout(() => {
        navigate('/select-child');
      }, 1200);
    } catch {
      // Error handled by AuthContext
    }
  };

  if (showSuccess) {
    return (
      <AuthLayout>
        <div className="text-center animate-fade-in">
          <Card color="success" className="py-12">
            <div className="flex justify-center mb-4">
              <div className="w-16 h-16 bg-success/20 rounded-full flex items-center justify-center">
                <CheckCircle className="w-8 h-8 text-success-dark" />
              </div>
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">
              Welcome back!
            </h2>
            <p className="text-gray-600">
              Let's continue your learning journey
            </p>
          </Card>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      {/* Logo */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center gap-2 mb-2">
          <div className="w-12 h-12 bg-gradient-to-br from-primary to-primary-dark rounded-xl flex items-center justify-center shadow-lg">
            <Sparkles className="w-6 h-6 text-white" />
          </div>
        </div>
        <h1 className="text-3xl font-bold text-gray-800">
          <span className="text-primary">Kids</span>
          <span className="text-coral">Math</span>
        </h1>
      </div>

      <Card className="animate-fade-in">
        <div className="text-center mb-6">
          <h2 className="text-xl font-semibold text-gray-800">
            Welcome back
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            Sign in to continue learning
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Email field */}
          <div>
            <label 
              htmlFor="email"
              className="block text-sm font-medium text-gray-700 mb-1.5"
            >
              Email
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                id="email"
                type="email"
                value={email}
                onChange={handleEmailChange}
                onBlur={handleEmailBlur}
                className={`
                  w-full pl-10 pr-4 py-2.5 rounded-lg border text-sm
                  transition-colors duration-150
                  focus:outline-none focus:ring-2 focus:ring-primary/20
                  ${emailError && touched.email
                    ? 'border-coral bg-coral/5 focus:border-coral' 
                    : 'border-gray-200 focus:border-primary bg-white'
                  }
                `}
                placeholder="you@example.com"
                autoComplete="email"
              />
            </div>
            {emailError && touched.email && (
              <p className="mt-1.5 text-coral text-sm flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {emailError}
              </p>
            )}
          </div>

          {/* Password field */}
          <div>
            <label 
              htmlFor="password"
              className="block text-sm font-medium text-gray-700 mb-1.5"
            >
              Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full pl-10 pr-10 py-2.5 rounded-lg border border-gray-200 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 transition-colors duration-150 bg-white"
                placeholder="••••••••"
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors p-1"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Error message */}
          {error && (
            <div className="flex items-start gap-3 rounded-lg border border-coral/30 bg-coral/5 p-3 text-coral-dark">
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <p className="text-sm">{error}</p>
            </div>
          )}

          {/* Submit button */}
          <Button 
            type="submit" 
            variant="primary" 
            fullWidth 
            loading={loading}
            size="lg"
            className="mt-2"
          >
            Sign in
          </Button>
        </form>

        {/* Register link */}
        <div className="mt-6 pt-5 border-t border-gray-100">
          <p className="text-center text-gray-600 text-sm">
            Don't have an account?{' '}
            <Link 
              to="/register" 
              className="text-primary font-semibold hover:text-primary-dark transition-colors"
            >
              Create one
            </Link>
          </p>
        </div>
      </Card>

      {/* Back to home */}
      <div className="text-center mt-6">
        <Link 
          to="/" 
          className="inline-flex items-center gap-2 text-gray-500 hover:text-gray-700 text-sm font-medium transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to home
        </Link>
      </div>
    </AuthLayout>
  );
};

export default Login;
