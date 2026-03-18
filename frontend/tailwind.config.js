/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: '#4ECDC4', light: '#7EDDD6', dark: '#3BA99F' },
        accent: { DEFAULT: '#FFE66D', light: '#FFF0A3', dark: '#E6CF5A' },
        coral: { DEFAULT: '#FF6B6B', light: '#FF9B9B', dark: '#E65555' },
        purple: { DEFAULT: '#A084E8', light: '#C4B0F0', dark: '#8066CC' },
        success: { DEFAULT: '#95D5B2', light: '#B5E5CA', dark: '#7AC49A' },
        background: '#FFF9E8',
      },
      fontFamily: {
        sans: ["'Nunito'", "'Comic Sans MS'", 'cursive', 'sans-serif'],
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.5s ease-out',
        'slide-up-delayed': 'slideUp 0.5s ease-out 0.2s both',
        'bounce-in': 'bounceIn 0.6s ease-out',
        'shake': 'shake 0.5s ease-in-out',
        'mascot-bounce': 'mascotBounce 2s ease-in-out infinite',
        'float': 'float 6s ease-in-out infinite',
        'float-delayed': 'float 6s ease-in-out 3s infinite',
        'twinkle': 'twinkle 2s ease-in-out infinite',
        'twinkle-delayed': 'twinkle 2s ease-in-out 1s infinite',
        'slide-left-out': 'slideLeftOut 200ms ease-in forwards',
        'slide-in-right': 'slideInRight 200ms ease-out',
        'star-fly': 'starFly 600ms ease-out forwards',
        'confetti-fall': 'confettiFall 2.5s ease-in forwards',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        bounceIn: {
          '0%': { opacity: '0', transform: 'scale(0.8)' },
          '50%': { transform: 'scale(1.05)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        shake: {
          '0%, 100%': { transform: 'translateX(0)' },
          '10%, 30%, 50%, 70%, 90%': { transform: 'translateX(-4px)' },
          '20%, 40%, 60%, 80%': { transform: 'translateX(4px)' },
        },
        mascotBounce: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0) rotate(0deg)' },
          '50%': { transform: 'translateY(-20px) rotate(5deg)' },
        },
        twinkle: {
          '0%, 100%': { opacity: '0.4', transform: 'scale(1)' },
          '50%': { opacity: '1', transform: 'scale(1.2)' },
        },
        slideLeftOut: {
          '0%': { opacity: '1', transform: 'translateX(0)' },
          '100%': { opacity: '0', transform: 'translateX(-100%)' },
        },
        slideInRight: {
          '0%': { opacity: '0', transform: 'translateX(100%)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        starFly: {
          '0%': { opacity: '1', transform: 'translate(0, 0) scale(1)' },
          '100%': { opacity: '0', transform: 'translate(200px, -200px) scale(0.3)' },
        },
        confettiFall: {
          '0%': { opacity: '1', transform: 'translateY(-20px)' },
          '100%': { opacity: '0', transform: 'translateY(100vh)' },
        },
      },
    },
  },
  plugins: [],
}
