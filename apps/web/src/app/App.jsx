import React, { useState, useEffect } from 'react'
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import Checklist from '../features/checklist/Checklist'
import Heatmap from '../features/heatmap/Heatmap'
import Login from '../features/auth/Login'
import Register from '../features/auth/Register'
import { authService } from '../services/authService'
import sprite from '../../../../packages/lib/sprite.svg'

// Configuración de pestañas de navegación
const tabs = [
  { id: 'checklist', label: 'Checklist', icon: '✓' },
  { id: 'heatmap', label: 'Heatmap', icon: '📊' },
]

const themes = ["light", "dark"]

// Componente protegido: Solo accesible si está autenticado
function ProtectedRoute({ children }) {
  const isAuth = authService.isAuthenticated()
  return isAuth ? children : <Navigate to="/login" replace />
}

// Componente principal del layout con navegación
function MainLayout() {
  const navigate = useNavigate()
  const [view, setView] = useState('checklist')
  const [mounted, setMounted] = useState(false)
  const [theme, setTheme] = useState(themes[0])

  useEffect(() => {
    setMounted(true)
  }, [])

  const handleLogout = () => {
    authService.logout()
    navigate('/login')
  }

  return (
    <div className={`mx-auto flex flex-col min-h-dvh bg-gradient-to-br from-gray-900 via-gray-700 to-gray-900 ${theme}`}>
      {/* Header */}
      <header className="border-b bg-card/70 backdrop-blur-sm sticky top-0 z-10">
        <div className="container mx-auto px-4 py-6 max-w-6xl">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            {/* Branding */}
            <div className="flex items-center gap-3 animate-fade-in">
              <div className="flex items-center justify-center w-10 h-10 rounded-lg bg-primary text-primary-foreground">
                <svg className="w-10 h-10" fill="none" stroke="currentColor">
                  <use href={`${sprite}#icon-checklist`} />
                </svg>
              </div>
              <div>
                <h1 className="text-2xl font-bold text-foreground">
                  ChecklistApp
                </h1>
                <p className="text-xs text-muted-foreground">Gestión eficiente de tareas</p>
              </div>

              {/* Theme toggle */}
              <div className='flex items-center'>
                <button
                  onClick={() => setTheme(theme === themes[0] ? themes[1] : themes[0])}
                  className="px-3 py-1.5 text-sm bg-secondary text-secondary-foreground rounded-md hover:bg-secondary/80 transition-colors"
                >
                  {theme === themes[0] ? '🌙' : '☀️'}
                </button>
              </div>
            </div>

            {/* Navigation */}
            <nav
              className="inline-flex p-1 bg-muted rounded-lg animate-slide-in"
              role="tablist"
              aria-label="Vistas de la aplicación"
            >
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setView(tab.id)}
                  role="tab"
                  aria-selected={view === tab.id}
                  aria-controls={`${tab.id}-panel`}
                  className={`
                    relative px-4 py-2 rounded-md text-sm font-medium
                    transition-all duration-200
                    flex items-center gap-2
                    ${view === tab.id
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:text-foreground hover:bg-background/50'
                    }
                  `}
                >
                  <span className="text-base" role="img" aria-label={tab.label}>
                    {tab.icon}
                  </span>
                  <span>{tab.label}</span>
                  {view === tab.id && (
                    <span className="absolute inset-0 rounded-md ring-2 ring-ring ring-offset-2 pointer-events-none" />
                  )}
                </button>
              ))}
            </nav>

            {/* Logout button */}
            <button
              onClick={handleLogout}
              className="px-3 py-1.5 text-sm bg-destructive/10 text-destructive rounded-md hover:bg-destructive/20 transition-colors"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container flex-grow mx-auto px-4 py-8 max-w-6xl">
        <div
          id={`${view}-panel`}
          role="tabpanel"
          aria-labelledby={`${view}-tab`}
          className={mounted ? 'animate-fade-in' : ''}
        >
          {view === 'checklist' && <Checklist />}
          {view === 'heatmap' && <Heatmap />}
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t bg-card/70 backdrop-blur-sm mt-auto">
        <div className="container mx-auto px-4 py-6 max-w-6xl">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-muted-foreground">
            <p>© 2026 ChecklistApp. Todos los derechos reservados.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}

// App principal con rutas
export default function App() {
  return (
    <Routes>
      {/* Rutas públicas */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Rutas protegidas */}
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}
