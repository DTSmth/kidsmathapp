import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { useChild } from './context/ChildContext';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ChildSelect from './pages/ChildSelect';
import Dashboard from './pages/Dashboard';
import LessonList from './pages/LessonList';
import LessonQuiz from './pages/LessonQuiz';
import LessonComplete from './pages/LessonComplete';
import ParentDashboard from './pages/ParentDashboard';
import Achievements from './pages/Achievements';
import TopicList from './pages/TopicList';
import Play from './pages/Play';
import GamePlay from './pages/GamePlay';
import GameComplete from './pages/GameComplete';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-6xl animate-bounce">🦁</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

const ChildProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { selectedChild, loading } = useChild();

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-6xl animate-bounce">🦁</div>
      </div>
    );
  }

  if (!selectedChild) {
    return <Navigate to="/select-child" replace />;
  }

  return <>{children}</>;
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/select-child"
          element={
            <ProtectedRoute>
              <ChildSelect />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <Dashboard />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/topics/:topicId/lessons"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <LessonList />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/lessons/:lessonId/quiz"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <LessonQuiz />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/lessons/:lessonId/complete"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <LessonComplete />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/topics"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <TopicList />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/play"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <Play />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/games/:gameId/play"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <GamePlay />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/games/:gameId/complete"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <GameComplete />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/achievements"
          element={
            <ProtectedRoute>
              <ChildProtectedRoute>
                <Achievements />
              </ChildProtectedRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/parent"
          element={
            <ProtectedRoute>
              <ParentDashboard />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
