import React, { useState, useEffect, Suspense, lazy } from 'react'
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import Login from '../features/auth/Login'
import Register from '../features/auth/Register'
import Sidebar from '../components/Sidebar'
import SkipLink from '../components/common/SkipLink'
import { ToastProvider, useToast } from '../contexts/ToastContext'
import ToastContainer from '../components/ui/ToastContainer'
import authService from '../services/authService'
import { useTheme } from '../hooks/useTheme'
import sprite from '../../../../packages/lib/sprite.svg'

// Lazy load components for better performance
const Checklist = lazy(() => import('../features/checklist/Checklist'))
const Heatmap = lazy(() => import('../features/heatmap/Heatmap'))
const TypeManagement = lazy(() => import('../features/types/TypeManagement'))

// Skeleton loader component
function ViewSkeleton() {
  return (
    <div className="space-y-4">
      <div className="animate-pulse">
        <div className="h-8 bg-muted rounded w-1/3 mb-4"></div>
        <div className="h-4 bg-muted rounded w-1/4 mb-8"></div>
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-muted rounded-lg"></div>
          ))}
        </div>
      </div>
    </div>
  )
}

// Configuración de pestañas de navegación
const tabs = [
  { id: 'checklist', label: 'Checklist', icon: '✓' },
  { id: 'heatmap', label: 'Heatmap', icon: '📊' },
  { id: 'types', label: 'Tipos', icon: '🏷️' },
]

// Componente principal del layout con navegación
function MainLayout() {
  const [view, setView] = useState('checklist')
  const [mounted, setMounted] = useState(false)
  const { theme, effectiveTheme, toggleTheme } = useTheme()

  useEffect(() => {
    setMounted(true)
  }, [])

  return (
    <div className="mx-auto flex flex-col min-h-dvh bg-background text-foreground">
      {/* ═══════════════════════════════════════════════════════════════════
          SKIP LINK - Accessibility for keyboard navigation (WCAG 2.1 AA)
          ═══════════════════════════════════════════════════════════════════ */}
      <SkipLink href="#main-content">Saltar al contenido principal</SkipLink>

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
                  onClick={toggleTheme}
                  className="inline-flex items-center gap-2 px-3 py-1.5 text-sm bg-secondary text-secondary-foreground rounded-md hover:bg-secondary/80 transition-colors transition-transform hover:scale-105"
                  aria-label={`Cambiar tema. Actual: ${theme === 'auto' ? 'Automático' : theme === 'light' ? 'Claro' : 'Oscuro'}`}
                  title={`Modo actual: ${theme === 'auto' ? 'Automático (sigue al sistema)' : theme === 'light' ? 'Claro' : 'Oscuro'}. Click para cambiar.`}
                >
                  <span className="text-base" role="img" aria-label="icono de tema">
                    {effectiveTheme === 'light' ? '🌙' : '☀️'}
                    {theme === 'auto' && <span className="text-xs ml-1">🔄</span>}
                  </span>
                  <span className="hidden sm:inline">
                    {theme === 'auto' ? 'Auto' : effectiveTheme === 'light' ? 'Oscuro' : 'Claro'}
                  </span>
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
      <main
        id="main-content"
        role="main"
        aria-label="Contenido principal"
        className="container flex-grow mx-auto px-4 py-8 max-w-6xl pl-20"
      >
        <div
          id={`${view}-panel`}
          role="tabpanel"
          aria-labelledby={`${view}-tab`}
          className={mounted ? 'animate-fade-in' : ''}
        >
          <Suspense fallback={<ViewSkeleton />}>
            {view === 'checklist' && <Checklist />}
            {view === 'heatmap' && <Heatmap />}
            {view === 'types' && <TypeManagement />}
          </Suspense>
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
    <ToastProvider>
      <Routes>
        {/* Rutas de autenticación */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Ruta principal: accesible sin autenticación */}
        <Route path="/*" element={
          <>
            <MainLayout />
            <ToastContainerWrapper />
          </>
        } />
      </Routes>
    </ToastProvider>
  )
}

// Wrapper componente to render ToastContainer with context access
function ToastContainerWrapper() {
  const { toasts, removeToast } = useToast()
  return <ToastContainer toasts={toasts} onRemoveToast={removeToast} />
}
