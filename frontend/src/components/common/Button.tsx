import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { Loader2 } from 'lucide-react';

type ButtonVariant = 'primary' | 'secondary' | 'success' | 'coral' | 'purple' | 'ghost';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  children: ReactNode;
  fullWidth?: boolean;
  loading?: boolean;
}

const variantStyles: Record<ButtonVariant, string> = {
  primary: 'bg-primary text-white shadow-sm hover:bg-primary-dark hover:shadow-md',
  secondary: 'bg-accent text-gray-900 shadow-sm hover:bg-accent-dark hover:shadow-md',
  success: 'bg-success text-gray-900 shadow-sm hover:bg-success-dark hover:shadow-md',
  coral: 'bg-coral text-white shadow-sm hover:bg-coral-dark hover:shadow-md',
  purple: 'bg-purple text-white shadow-sm hover:bg-purple-dark hover:shadow-md',
  ghost: 'bg-transparent text-gray-700 hover:bg-gray-100',
};

const sizeStyles: Record<ButtonSize, string> = {
  sm: 'px-3 py-1.5 text-sm gap-1.5',
  md: 'px-5 py-2.5 text-base gap-2',
  lg: 'px-6 py-3 text-lg gap-2.5',
};

const Button = ({
  variant = 'primary',
  size = 'md',
  children,
  fullWidth = false,
  loading = false,
  className = '',
  disabled,
  ...props
}: ButtonProps) => {
  const isDisabled = disabled || loading;
  
  return (
    <button
      className={`
        inline-flex items-center justify-center font-semibold rounded-xl
        transition-all duration-150
        focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary
        active:scale-[0.98]
        disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100
        ${variantStyles[variant]}
        ${sizeStyles[size]}
        ${fullWidth ? 'w-full' : ''}
        ${className}
      `}
      disabled={isDisabled}
      {...props}
    >
      {loading && <Loader2 className="h-4 w-4 animate-spin" />}
      {children}
    </button>
  );
};

export default Button;
