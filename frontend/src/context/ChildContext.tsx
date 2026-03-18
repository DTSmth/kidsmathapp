import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { Child } from '../types';
import { childService } from '../services/api';
import { useAuth } from './AuthContext';

interface ChildContextType {
  selectedChild: Child | null;
  children: Child[];
  selectChild: (child: Child) => void;
  loading: boolean;
  refreshChildren: () => Promise<void>;
  updateChildStats: (totalStars: number, currentStreak: number) => void;
}

const ChildContext = createContext<ChildContextType | undefined>(undefined);

export const ChildProvider = ({ children: childrenProp }: { children: ReactNode }) => {
  const { isAuthenticated } = useAuth();
  const [selectedChild, setSelectedChild] = useState<Child | null>(null);
  const [children, setChildren] = useState<Child[]>([]);
  const [loading, setLoading] = useState(false);

  const refreshChildren = async () => {
    if (!isAuthenticated) return;
    
    setLoading(true);
    try {
      const childrenData = await childService.getChildren();
      setChildren(childrenData);
    } catch (error) {
      console.error('Failed to fetch children:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      refreshChildren();
    } else {
      setChildren([]);
      setSelectedChild(null);
    }
  }, [isAuthenticated]);

  const selectChild = (child: Child) => {
    setSelectedChild(child);
    localStorage.setItem('selectedChildId', child.id.toString());
  };

  const updateChildStats = (totalStars: number, currentStreak: number) => {
    setSelectedChild(prev => prev ? { ...prev, totalStars, currentStreak } : prev);
  };

  useEffect(() => {
    const storedChildId = localStorage.getItem('selectedChildId');
    if (storedChildId && children.length > 0) {
      const child = children.find((c) => c.id === parseInt(storedChildId));
      if (child) {
        setSelectedChild(child);
      }
    }
  }, [children]);

  return (
    <ChildContext.Provider
      value={{
        selectedChild,
        children,
        selectChild,
        loading,
        refreshChildren,
        updateChildStats,
      }}
    >
      {childrenProp}
    </ChildContext.Provider>
  );
};

export const useChild = () => {
  const context = useContext(ChildContext);
  if (context === undefined) {
    throw new Error('useChild must be used within a ChildProvider');
  }
  return context;
};

export default ChildContext;
