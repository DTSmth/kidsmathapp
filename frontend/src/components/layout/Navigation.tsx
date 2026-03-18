import { NavLink } from 'react-router-dom';
import { Home, BookOpen, Trophy, User } from 'lucide-react';

const navItems = [
  { to: '/dashboard', icon: Home, label: 'Home' },
  { to: '/learn', icon: BookOpen, label: 'Learn' },
  { to: '/achievements', icon: Trophy, label: 'Trophies' },
  { to: '/profile', icon: User, label: 'Profile' },
];

const Navigation = () => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 md:relative md:border-0">
      <div className="max-w-md mx-auto px-4">
        <ul className="flex justify-around py-2">
          {navItems.map(({ to, icon: Icon, label }) => (
            <li key={to}>
              <NavLink
                to={to}
                className={({ isActive }) =>
                  `flex flex-col items-center gap-0.5 px-4 py-1.5 rounded-lg transition-colors ${
                    isActive
                      ? 'text-primary'
                      : 'text-gray-400 hover:text-gray-600'
                  }`
                }
              >
                <Icon className="w-5 h-5" />
                <span className="text-xs font-medium">{label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </div>
    </nav>
  );
};

export default Navigation;
