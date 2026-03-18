import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { AuthProvider } from './context/AuthContext'
import { ChildProvider } from './context/ChildContext'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <ChildProvider>
        <App />
      </ChildProvider>
    </AuthProvider>
  </StrictMode>,
)
