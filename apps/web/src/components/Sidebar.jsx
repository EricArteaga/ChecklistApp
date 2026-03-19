import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import authService from '../services/authService'
import typeService from '../services/typeService'
import { useTheme } from '../hooks/useTheme'

/**
 * ═══════════════════════════════════════════════════════════════════
 * SIDEBAR - Todoist-style navigation drawer
 * ═══════════════════════════════════════════════════════════════════
 *
 * Features:
 * • User dropdown with real name (authenticated) or login button (anonymous)
 * • Theme toggle in user menu (no auto option)
 * • Navigation: Tareas, Buscador, Inbox, Hoy, Próximo, Heatmap
 * • Types list with "+" button
 */
export default function Sidebar({ currentView, onViewChange }) {
  const navigate = useNavigate()
  const [isOpen, setIsOpen] = useState(false)
  const [user, setUser] = useState(null)
  const [userMenuOpen, setUserMenuOpen] = useState(false)
  const [types, setTypes] = useState([])
  const [loading, setLoading] = useState(true)
  const { theme, toggleTheme } = useTheme()

  // Load user and types
  useEffect(() => {
    loadUserAndTypes()
  }, [])

  // Reload when sidebar opens
  useEffect(() => {
    if (isOpen) {
      loadUserAndTypes()
    }
  }, [isOpen])

  const loadUserAndTypes = async () => {
    if (authService.isAuthenticated()) {
      try {
        const userData = await authService.getMe()
        setUser(userData)

        // Load types
        const typesData = await typeService.getTypesByUser(userData.id)
        setTypes(typesData)
      } catch (error) {
        console.error('Error loading data:', error)
        const cachedUser = localStorage.getItem('userCache')
        if (cachedUser) {
          try {
            setUser(JSON.parse(cachedUser))
          } catch (parseError) {
            setUser(null)
          }
        } else {
          setUser(null)
        }
      }
    } else {
      setUser(null)
    }
    setLoading(false)
  }

  const handleLogin = () => {
    navigate('/login')
  }

  const handleLogout = () => {
    authService.logout()
    setUser(null)
    setUserMenuOpen(false)
    setIsOpen(false)
    navigate('/login')
  }

  const handleViewChange = (view) => {
    onViewChange(view)
    setIsOpen(false)
  }

  const handleTypeClick = (typeId) => {
    onViewChange('type-detail')
    // Store selected type ID in localStorage for TypeDetailView to read
    localStorage.setItem('selectedTypeId', typeId)
    setIsOpen(false)
  }

  const handleCreateType = () => {
    // Navigate to types management view
    handleViewChange('types')
  }

  return (
    <>
      {/* Toggle button */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed left-4 top-4 z-50 p-2.5 bg-gradient-primary text-white rounded-xl shadow-soft hover:shadow-strong hover-lift focus-ring-offset transition-smooth"
        aria-label="Abrir menú"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      {/* Overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-background/80 backdrop-blur-sm z-40 transition-opacity animate-fade-in"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed left-0 top-0 h-full w-80 bg-card shadow-strong z-50
          transform transition-transform duration-300 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full'}
        `}
      >
        <div className="flex flex-col h-full">
          {/* ═══════════════════════════════════════════════════════════
              HEADER - Unified dropdown (theme toggle always accessible)
              ═══════════════════════════════════════════════════════════ */}
          <div className="p-4 border-b">
            {loading ? (
              <div className="flex items-center justify-center py-4">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
              </div>
            ) : (
              // Unified dropdown for both authenticated and anonymous users
              <div className="relative">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="w-full flex items-center gap-3 p-3 hover:bg-muted rounded-xl transition-smooth"
                >
                  {user ? (
                    // Authenticated: Show avatar and name
                    <>
                      <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-primary text-white text-lg font-bold shadow-soft">
                        {user.nombre?.charAt(0).toUpperCase() || 'U'}
                      </div>
                      <div className="flex-1 text-left">
                        <p className="font-semibold text-foreground">{user.nombre || 'Usuario'}</p>
                      </div>
                    </>
                  ) : (
                    // Anonymous: Show menu icon
                    <>
                      <svg className="w-6 h-6 text-muted-foreground" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                      </svg>
                      <span className="font-medium text-foreground">Menú</span>
                    </>
                  )}

                  <svg
                    className={`w-4 h-4 text-muted-foreground transition-transform ${userMenuOpen ? 'rotate-180' : ''}`}
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                {/* Unified dropdown menu */}
                {userMenuOpen && (
                  <>
                    {/* Backdrop */}
                    <div
                      className="fixed inset-0 z-10"
                      onClick={() => setUserMenuOpen(false)}
                    />

                    {/* Menu */}
                    <div className="absolute top-full left-0 right-0 mt-2 bg-card border rounded-xl shadow-lg z-20 overflow-hidden">
                      {/* Theme toggle - ALWAYS VISIBLE */}
                      <button
                        onClick={() => {
                          toggleTheme()
                          setUserMenuOpen(false)
                        }}
                        className="w-full px-4 py-3 flex items-center gap-3 hover:bg-muted transition-smooth text-left"
                      >
                        <span className="text-lg">
                          {theme === 'light' ? '🌙' : '☀️'}
                        </span>
                        <span>
                          {theme === 'light' ? 'Modo Oscuro' : 'Modo Claro'}
                        </span>
                      </button>

                      <div className="border-t" />

                      {user ? (
                        // Authenticated user options
                        <>
                          {/* User info */}
                          <div className="p-4 bg-muted/30 border-b">
                            <div className="flex items-center gap-3">
                              <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-primary text-white text-lg font-bold">
                                {user.nombre?.charAt(0).toUpperCase() || 'U'}
                              </div>
                              <div>
                                <p className="font-semibold text-foreground">{user.nombre || 'Usuario'}</p>
                                <p className="text-xs text-muted-foreground">{user.correo || user.email || ''}</p>
                              </div>
                            </div>
                          </div>

                          {/* Settings button */}
                          <button className="w-full px-4 py-3 flex items-center gap-3 hover:bg-muted transition-smooth text-left">
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            </svg>
                            <span>Configuración</span>
                          </button>

                          {/* Logout */}
                          <button
                            onClick={handleLogout}
                            className="w-full px-4 py-3 flex items-center gap-3 hover:bg-destructive/10 transition-smooth text-left text-destructive"
                          >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                            </svg>
                            <span>Cerrar Sesión</span>
                          </button>
                        </>
                      ) : (
                        // Anonymous user: Login button
                        <button
                          onClick={handleLogin}
                          className="w-full px-4 py-3 flex items-center gap-3 hover:bg-muted transition-smooth text-left"
                        >
                          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                          </svg>
                          <span className="font-medium">Iniciar Sesión</span>
                        </button>
                      )}
                    </div>
                  </>
                )}
              </div>
            )}
          </div>

          {/* ═══════════════════════════════════════════════════════════
              NAVIGATION - Main menu
              ═══════════════════════════════════════════════════════════ */}
          <nav className="flex-1 overflow-y-auto p-4 space-y-1">
            {/* Tareas (renamed from Checklist) */}
            <NavItem
              icon="✅"
              label="Tareas"
              active={currentView === 'tareas'}
              onClick={() => handleViewChange('tareas')}
            />

            {/* Buscador */}
            <NavItem
              icon="🔍"
              label="Buscador"
              active={currentView === 'search'}
              onClick={() => handleViewChange('search')}
            />

            {/* Bandeja de Entrada */}
            <NavItem
              icon="📥"
              label="Bandeja de Entrada"
              active={currentView === 'inbox'}
              onClick={() => handleViewChange('inbox')}
            />

            <div className="border-t my-3" />

            {/* Hoy */}
            <NavItem
              icon="📅"
              label="Hoy"
              active={currentView === 'today'}
              onClick={() => handleViewChange('today')}
            />

            {/* Próximo */}
            <NavItem
              icon="📆"
              label="Próximo"
              active={currentView === 'upcoming'}
              onClick={() => handleViewChange('upcoming')}
            />

            {/* Heatmap */}
            <NavItem
              icon="📊"
              label="Heatmap"
              active={currentView === 'heatmap'}
              onClick={() => handleViewChange('heatmap')}
            />

            {/* ═══════════════════════════════════════════════════════════
                TYPES - User's project types
                ═══════════════════════════════════════════════════════════ */}
            {user && types.length > 0 && (
              <>
                <div className="border-t my-3" />

                {/* Types header with add button */}
                <div className="flex items-center justify-between mb-2 px-3">
                  <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">
                    Tipos
                  </h3>
                  <button
                    onClick={handleCreateType}
                    className="p-1 hover:bg-muted rounded transition-smooth"
                    aria-label="Crear nuevo tipo"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                  </button>
                </div>

                {/* Types list */}
                <ul className="space-y-1">
                  {types.map(type => (
                    <li key={type.id}>
                      <button
                        onClick={() => handleTypeClick(type.id)}
                        className={`
                          w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-smooth text-left
                          ${currentView === 'type-detail' && localStorage.getItem('selectedTypeId') === String(type.id)
                            ? 'bg-primary/10 text-primary font-medium'
                            : 'hover:bg-muted text-foreground'
                          }
                        `}
                      >
                        <div
                          className="w-3 h-3 rounded-full shadow-soft flex-shrink-0"
                          style={{ backgroundColor: type.color }}
                        />
                        <span className="truncate">{type.nombre}</span>
                      </button>
                    </li>
                  ))}
                </ul>
              </>
            )}
          </nav>

          {/* ═══════════════════════════════════════════════════════════
              FOOTER - Minimalist
              ═══════════════════════════════════════════════════════════ */}
          <div className="p-4 border-t bg-muted/20">
            <p className="text-xs text-center text-muted-foreground">
              © 2026 ChecklistApp
            </p>
          </div>
        </div>
      </aside>
    </>
  )
}

/**
 * Nav Item Component
 */
function NavItem({ icon, label, active, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`
        w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-smooth text-left
        ${active
          ? 'bg-primary/10 text-primary font-medium'
          : 'hover:bg-muted text-foreground'
        }
      `}
    >
      <span className="text-lg">{icon}</span>
      <span>{label}</span>
    </button>
  )
}
