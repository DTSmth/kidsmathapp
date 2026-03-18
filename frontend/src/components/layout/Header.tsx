import { Link } from 'react-router-dom';
import { LogOut, Users, LayoutDashboard, ChevronDown, Sparkles, Star, Flame } from 'lucide-react';
import { Menu, MenuButton, MenuItems, MenuItem } from '@headlessui/react';
import { useAuth } from '../../context/AuthContext';
import { useChild } from '../../context/ChildContext';
import Avatar from '../common/Avatar';

const Header = () => {
  const { logout } = useAuth();
  const { selectedChild } = useChild();

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
        {/* Logo */}
        <Link to="/dashboard" className="flex items-center gap-2 hover:opacity-80 transition-opacity">
          <div className="w-8 h-8 bg-gradient-to-br from-primary to-primary-dark rounded-lg flex items-center justify-center">
            <Sparkles className="w-4 h-4 text-white" />
          </div>
          <span className="text-xl font-bold">
            <span className="text-primary">Kids</span>
            <span className="text-coral">Math</span>
          </span>
        </Link>

        {selectedChild && (
          <div className="flex items-center gap-4">
            {/* Stats pills */}
            <div className="hidden sm:flex items-center gap-2">
              <div className="flex items-center gap-1.5 bg-amber-50 px-2.5 py-1 rounded-full text-sm">
                <Star className="w-4 h-4 text-amber-500 fill-current" />
                <span className="font-semibold text-gray-700">{selectedChild.totalStars}</span>
              </div>
              {selectedChild.currentStreak > 0 && (
                <div className="flex items-center gap-1.5 bg-orange-50 px-2.5 py-1 rounded-full text-sm">
                  <Flame className="w-4 h-4 text-orange-500" />
                  <span className="font-semibold text-gray-700">{selectedChild.currentStreak}</span>
                </div>
              )}
            </div>

            {/* Profile menu */}
            <Menu as="div" className="relative">
              <MenuButton className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-gray-50 transition-colors">
                <Avatar avatarId={selectedChild.avatarId} size="sm" />
                <span className="font-medium text-gray-700 text-sm hidden sm:block">{selectedChild.name}</span>
                <ChevronDown className="w-4 h-4 text-gray-400" />
              </MenuButton>

              <MenuItems className="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-50">
                <MenuItem>
                  {({ focus }) => (
                    <Link
                      to="/select-child"
                      className={`flex items-center gap-2.5 px-3 py-2 text-sm ${
                        focus ? 'bg-gray-50' : ''
                      }`}
                    >
                      <Users className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-700">Switch profile</span>
                    </Link>
                  )}
                </MenuItem>
                <MenuItem>
                  {({ focus }) => (
                    <Link
                      to="/parent"
                      className={`flex items-center gap-2.5 px-3 py-2 text-sm ${
                        focus ? 'bg-gray-50' : ''
                      }`}
                    >
                      <LayoutDashboard className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-700">Parent dashboard</span>
                    </Link>
                  )}
                </MenuItem>
                <div className="border-t border-gray-100 my-1" />
                <MenuItem>
                  {({ focus }) => (
                    <button
                      onClick={logout}
                      className={`flex items-center gap-2.5 px-3 py-2 w-full text-left text-sm ${
                        focus ? 'bg-gray-50' : ''
                      }`}
                    >
                      <LogOut className="w-4 h-4 text-coral" />
                      <span className="text-coral">Sign out</span>
                    </button>
                  )}
                </MenuItem>
              </MenuItems>
            </Menu>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
