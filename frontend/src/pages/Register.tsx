import { useState, useEffect, useMemo } from 'react';
import type { FormEvent, ChangeEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import AuthLayout from '../components/layout/AuthLayout';
import { Mail, Lock, Eye, EyeOff, ArrowLeft, Sparkles, AlertCircle, CheckCircle, Check } from 'lucide-react';

const validateEmail = (email: string): string | null => {
  if (!email) return null;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return "Please enter a valid email address";
  }
  return null;
};

const validatePassword = (password: string): string | null => {
  if (!password) return null;
  if (password.length < 6) {
    return "Password must be at least 6 characters";
  }
  return null;
};

const validateConfirmPassword = (password: string, confirmPassword: string): string | null => {
  if (!confirmPassword) return null;
  if (password !== confirmPassword) {
    return "Passwords don't match";
  }
  return null;
};

type PasswordStrength = 'weak' | 'medium' | 'strong' | null;

const getPasswordStrength = (password: string): PasswordStrength => {
  if (!password) return null;
  if (password.length < 6) return 'weak';
  
  let score = 0;
  if (password.length >= 8) score++;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[^a-zA-Z0-9]/.test(password)) score++;
  
  if (score >= 3) return 'strong';
  if (score >= 1) return 'medium';
  return 'weak';
};

const strengthConfig = {
  weak: {
    color: 'bg-coral',
    textColor: 'text-coral-dark',
    label: 'Weak',
    width: 'w-1/3',
  },
  medium: {
    color: 'bg-accent',
    textColor: 'text-amber-600',
    label: 'Medium',
    width: 'w-2/3',
  },
  strong: {
    color: 'bg-success',
    textColor: 'text-green-600',
    label: 'Strong',
    width: 'w-full',
  },
};

const Register = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [touched, setTouched] = useState({ 
    email: false, 
    password: false, 
    confirmPassword: false 
  });
  
  const [emailError, setEmailError] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [confirmPasswordError, setConfirmPasswordError] = useState<string | null>(null);
  
  const { register, loading, error } = useAuth();
  const navigate = useNavigate();

  const passwordStrength = useMemo(() => getPasswordStrength(password), [password]);

  useEffect(() => {
    if (touched.email) {
      setEmailError(validateEmail(email));
    }
  }, [email, touched.email]);

  useEffect(() => {
    if (touched.password) {
      setPasswordError(validatePassword(password));
    }
  }, [password, touched.password]);

  useEffect(() => {
    if (touched.confirmPassword) {
      setConfirmPasswordError(validateConfirmPassword(password, confirmPassword));
    }
  }, [password, confirmPassword, touched.confirmPassword]);

  const handleBlur = (field: 'email' | 'password' | 'confirmPassword') => {
    setTouched(prev => ({ ...prev, [field]: true }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    
    setTouched({ email: true, password: true, confirmPassword: true });
    
    const emailErr = validateEmail(email);
    const passwordErr = validatePassword(password);
    const confirmErr = validateConfirmPassword(password, confirmPassword);
    
    setEmailError(emailErr);
    setPasswordError(passwordErr);
    setConfirmPasswordError(confirmErr);
    
    if (emailErr || passwordErr || confirmErr) {
      return;
    }

    try {
      await register(email, password);
      setShowSuccess(true);
      setTimeout(() => {
        navigate('/select-child');
      }, 1500);
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
              Welcome to KidsMath!
            </h2>
            <p className="text-gray-600">
              Your account is ready. Let's get started!
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
          <div className="w-12 h-12 bg-gradient-to-br from-coral to-coral-dark rounded-xl flex items-center justify-center shadow-lg">
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
            Create an account
          </h2>
          <p className="text-gray-500 text-sm mt-1">
            Start your learning adventure today
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
                onChange={(e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
                onBlur={() => handleBlur('email')}
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
                onChange={(e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
                onBlur={() => handleBlur('password')}
                className={`
                  w-full pl-10 pr-10 py-2.5 rounded-lg border text-sm
                  transition-colors duration-150
                  focus:outline-none focus:ring-2 focus:ring-primary/20
                  ${passwordError && touched.password
                    ? 'border-coral bg-coral/5 focus:border-coral' 
                    : 'border-gray-200 focus:border-primary bg-white'
                  }
                `}
                placeholder="••••••••"
                autoComplete="new-password"
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
            
            {/* Password strength indicator */}
            {password && (
              <div className="mt-2">
                <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div 
                    className={`h-full ${passwordStrength ? strengthConfig[passwordStrength].color : ''} ${passwordStrength ? strengthConfig[passwordStrength].width : ''} transition-all duration-300`}
                  />
                </div>
                {passwordStrength && (
                  <p className={`mt-1 text-xs font-medium ${strengthConfig[passwordStrength].textColor}`}>
                    {strengthConfig[passwordStrength].label} password
                  </p>
                )}
              </div>
            )}
            
            {passwordError && touched.password && (
              <p className="mt-1.5 text-coral text-sm flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {passwordError}
              </p>
            )}
          </div>

          {/* Confirm Password field */}
          <div>
            <label 
              htmlFor="confirmPassword"
              className="block text-sm font-medium text-gray-700 mb-1.5"
            >
              Confirm Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                id="confirmPassword"
                type={showConfirmPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e: ChangeEvent<HTMLInputElement>) => setConfirmPassword(e.target.value)}
                onBlur={() => handleBlur('confirmPassword')}
                className={`
                  w-full pl-10 pr-10 py-2.5 rounded-lg border text-sm
                  transition-colors duration-150
                  focus:outline-none focus:ring-2 focus:ring-primary/20
                  ${confirmPasswordError && touched.confirmPassword
                    ? 'border-coral bg-coral/5 focus:border-coral' 
                    : 'border-gray-200 focus:border-primary bg-white'
                  }
                `}
                placeholder="••••••••"
                autoComplete="new-password"
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors p-1"
                aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
              >
                {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            
            {/* Match indicator */}
            {confirmPassword && password === confirmPassword && (
              <p className="mt-1.5 text-green-600 text-sm flex items-center gap-1">
                <Check className="w-4 h-4" />
                Passwords match
              </p>
            )}
            
            {confirmPasswordError && touched.confirmPassword && (
              <p className="mt-1.5 text-coral text-sm flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {confirmPasswordError}
              </p>
            )}
          </div>

          {/* API Error message */}
          {error && (
            <div className="flex items-start gap-3 rounded-lg border border-coral/30 bg-coral/5 p-3 text-coral-dark">
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <p className="text-sm">{error}</p>
            </div>
          )}

          {/* Submit button */}
          <Button 
            type="submit" 
            variant="coral" 
            fullWidth 
            loading={loading}
            size="lg"
            className="mt-2"
          >
            Create account
          </Button>
        </form>

        {/* Login link */}
        <div className="mt-6 pt-5 border-t border-gray-100">
          <p className="text-center text-gray-600 text-sm">
            Already have an account?{' '}
            <Link 
              to="/login" 
              className="text-primary font-semibold hover:text-primary-dark transition-colors"
            >
              Sign in
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

export default Register;
