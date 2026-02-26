import React, { useState, useEffect } from 'react'
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import Checklist from '../features/checklist/Checklist'
import Heatmap from '../features/heatmap/Heatmap'
import TypeManagement from '../features/types/TypeManagement'
import Login from '../features/auth/Login'
import Register from '../features/auth/Register'
import Sidebar from '../components/Sidebar'
import authService from '../services/authService'
import sprite from '../../../../packages/lib/sprite.svg'

// Configuración de pestañas de navegación
const tabs = [
  { id: 'checklist', label: 'Checklist', icon: '✓' },
  { id: 'heatmap', label: 'Heatmap', icon: '📊' },
  { id: 'types', label: 'Tipos', icon: '🏷️' },
]

const themes = ["light", "dark"]

// Componente principal del layout con navegación
function MainLayout() {
  const [view, setView] = useState('checklist')
  const [mounted, setMounted] = useState(false)
  const [theme, setTheme] = useState(themes[0])

  useEffect(() => {
    setMounted(true)
  }, [])

  return (
    <div className={`mx-auto flex flex-col min-h-dvh bg-background text-foreground ${theme === 'dark' ? 'dark' : ''}`}>
      {/* Sidebar */}
      <Sidebar />

      {/* Header */}
      <header className="border-b bg-card/70 backdrop-blur-sm sticky top-0 z-10 pl-16">
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
                  aria-label="Cambiar tema"
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
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container flex-grow mx-auto px-4 py-8 max-w-6xl pl-20">
        <div
          id={`${view}-panel`}
          role="tabpanel"
          aria-labelledby={`${view}-tab`}
          className={mounted ? 'animate-fade-in' : ''}
        >
          {view === 'checklist' && <Checklist />}
          {view === 'heatmap' && <Heatmap />}
          {view === 'types' && <TypeManagement />}
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

// App principal con rutas (autenticación opcional)
export default function App() {
  return (
    <Routes>
      {/* Rutas de autenticación */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Ruta principal: accesible sin autenticación */}
      <Route path="/*" element={<MainLayout />} />
    </Routes>
  )
}
