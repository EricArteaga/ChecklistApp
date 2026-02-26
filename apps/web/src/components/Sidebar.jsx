import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import authService from '../services/authService'

/**
 * ═══════════════════════════════════════════════════════════════════
 * SIDEBAR - Navigation & User Profile
 * ═══════════════════════════════════════════════════════════════════
 *
 * Design principles:
 * • Premium header with gradient (Azul Oscuro → Azul Claro)
 * • Calm, focused user card (Azul Claro accents)
 * • Satisfying micro-interactions
 * • Clear visual hierarchy
 */
export default function Sidebar() {
  const navigate = useNavigate()
  const [isOpen, setIsOpen] = useState(false)
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // Cargar usuario cuando el componente se monta
  useEffect(() => {
    loadUser()
  }, [])

  // Recargar usuario cuando el sidebar se abre
  useEffect(() => {
    if (isOpen) {
      loadUser()
    }
  }, [isOpen])

  const loadUser = async () => {
    if (authService.isAuthenticated()) {
      try {
        const userData = await authService.getMe()
        setUser(userData)
      } catch (error) {
        console.error('Error loading user:', error)
        // Si hay error con el token, limpiarlo
        authService.logout()
        setUser(null)
      }
    } else {
      setUser(null)
    }
    setLoading(false)
  }

  const handleLogin = () => {
    navigate('/login')
  }

  const handleRegister = () => {
    navigate('/register')
  }

  const handleLogout = () => {
    authService.logout()
    setUser(null)
    setLoading(false)
    setIsOpen(false)
    navigate('/login')
  }

  return (
    <>
      {/* Botón toggle para abrir sidebar - Azul Oscuro para enfoque */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed left-4 top-4 z-50 p-2.5 bg-gradient-primary text-white rounded-xl shadow-soft hover:shadow-strong hover-lift focus-ring-offset transition-smooth"
        aria-label="Abrir menú"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      {/* Overlay oscuro cuando sidebar está abierto */}
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
          {/* ─────────────────────────────────────────────────────────
              Header del sidebar con gradiente premium
              ───────────────────────────────────────────────────────── */}
          <div className="bg-gradient-primary p-6 shadow-soft">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                {/* Icono con fondo blanco para contraste */}
                <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-white/20 backdrop-blur-sm text-white shadow-soft">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                  </svg>
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">ChecklistApp</h2>
                  <p className="text-xs text-white/80">Tu espacio de enfoque</p>
                </div>
              </div>
              <button
                onClick={() => setIsOpen(false)}
                className="p-2 hover:bg-white/10 rounded-lg transition-smooth hover-lift focus-ring text-white/90 hover:text-white"
                aria-label="Cerrar menú"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          {/* ─────────────────────────────────────────────────────────
              Contenido del sidebar
              ───────────────────────────────────────────────────────── */}
          <div className="flex-1 overflow-y-auto p-6 scrollbar-thin">
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              </div>
            ) : user ? (
              // ─────────────────────────────────────────────────────────
              // USUARIO AUTENTICADO - Tarjeta premium
              // ─────────────────────────────────────────────────────────
              <div className="space-y-6">
                {/* Información del usuario - Card premium con borde gradiente */}
                <div className="card-premium p-5 rounded-xl bg-gradient-subtle">
                  <div className="flex items-center gap-4 mb-4">
                    {/* Avatar con gradiente Azul Verdoso (logro) */}
                    <div className="flex items-center justify-center w-14 h-14 rounded-xl bg-gradient-success text-white text-xl font-bold shadow-soft">
                      {user.nombre?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-muted-foreground truncate">{user.correo || user.email || ''}</p>
                      <h3 className="font-semibold text-foreground text-lg truncate">{user.nombre || 'Usuario'}</h3>
                    </div>
                  </div>

                  {/* Badge de estado - Azul Verdoso para éxito */}
                  <div className="flex items-center gap-2 badge-success justify-center py-2">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <span>Autenticado</span>
                  </div>
                </div>

                {/* Mensaje informativo - Azul Claro para calma */}
                <div className="p-4 bg-info-light/50 border border-info/30 rounded-xl">
                  <div className="flex items-start gap-3">
                    <div className="flex-shrink-0 w-8 h-8 rounded-lg bg-info/10 flex items-center justify-center">
                      <svg className="w-4 h-4 text-info" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-foreground">
                        Tus tareas están guardadas de forma segura
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">
                        Accede desde cualquier dispositivo
                      </p>
                    </div>
                  </div>
                </div>

                {/* Botón de logout - Minimalista pero claro */}
                <button
                  onClick={handleLogout}
                  className="w-full px-4 py-3 text-destructive hover:bg-destructive/10 rounded-xl transition-smooth hover-lift focus-ring flex items-center justify-center gap-2 font-medium border border-destructive/20"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  Cerrar Sesión
                </button>
              </div>
            ) : (
              // ─────────────────────────────────────────────────────────
              // USUARIO NO AUTENTICADO - Enfoque en conversión
              // ─────────────────────────────────────────────────────────
              <div className="space-y-6">
                {/* Mensaje de advertencia - Ambar para atención */}
                <div className="p-4 bg-warning-light/50 border border-warning/30 rounded-xl">
                  <div className="flex items-start gap-3">
                    <div className="flex-shrink-0 w-8 h-8 rounded-lg bg-warning/10 flex items-center justify-center">
                      <svg className="w-4 h-4 text-warning" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                      </svg>
                    </div>
                    <div>
                      <h4 className="font-semibold text-warning mb-1">
                        Sesión no iniciada
                      </h4>
                      <p className="text-sm text-foreground">
                        Tus tareas no se guardarán permanentemente. Inicia sesión para respaldo en la nube.
                      </p>
                    </div>
                  </div>
                </div>

                {/* Icono de usuario anónimo - Sutil, no intrusivo */}
                <div className="flex justify-center">
                  <div className="flex items-center justify-center w-20 h-20 rounded-2xl bg-muted/30 shadow-soft">
                    <svg className="w-10 h-10 text-muted-foreground" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                </div>

                {/* Botones de autenticación - Jerarquía clara */}
                <div className="space-y-3">
                  {/* Primario: Iniciar Sesión (Azul Oscuro) */}
                  <button
                    onClick={handleLogin}
                    className="w-full px-4 py-3 bg-gradient-primary text-white rounded-xl hover:shadow-strong transition-smooth hover-lift focus-ring flex items-center justify-center gap-2 font-medium shadow-soft"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                    </svg>
                    Iniciar Sesión
                  </button>

                  {/* Secundario: Crear Cuenta (Azul Claro) */}
                  <button
                    onClick={handleRegister}
                    className="w-full px-4 py-3 bg-secondary text-white rounded-xl hover:bg-secondary/90 transition-smooth hover-lift focus-ring flex items-center justify-center gap-2 font-medium shadow-soft"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                    </svg>
                    Crear Cuenta
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* ─────────────────────────────────────────────────────────
              Footer del sidebar - Minimalista
              ───────────────────────────────────────────────────────── */}
          <div className="p-6 border-t bg-muted/20">
            <p className="text-xs text-center text-muted-foreground">
              © 2026 ChecklistApp
            </p>
          </div>
        </div>
      </aside>
    </>
  )
}
