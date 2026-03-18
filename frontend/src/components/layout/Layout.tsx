import type { ReactNode } from 'react';
import Header from './Header';
import Navigation from './Navigation';

interface LayoutProps {
  children: ReactNode;
  showNavigation?: boolean;
}

const Layout = ({ children, showNavigation = true }: LayoutProps) => {
  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />
      <main className="flex-1 max-w-6xl mx-auto w-full px-4 py-6 pb-24 md:pb-6">
        {children}
      </main>
      {showNavigation && <Navigation />}
    </div>
  );
};

export default Layout;
