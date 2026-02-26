import React, { useState } from 'react'
import { Button, Input, Card, CardHeader, CardTitle, CardDescription, CardContent } from '@checklist/ui'
import { useNavigate } from 'react-router-dom'
import authService from '../../services/authService'
import taskService from '../../services/taskService'
import localStorageService from '../../services/localStorageService'

/**
 * ═══════════════════════════════════════════════════════════════════
 * LOGIN - Focused Authentication Experience
 * ═══════════════════════════════════════════════════════════════════
 *
 * Design principles:
 * • Clean, centered layout for focus
 * • Azul Oscuro gradient header for brand presence
 * • Clear visual hierarchy
 * • Subtle animations for polish
 * • Minimal distractions
 */
export default function Login() {
  const navigate = useNavigate()

  // Estados del formulario
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  })
  const [loading, setLoading] = useState(false)
  const [syncing, setSyncing] = useState(false)
  const [error, setError] = useState('')

  // Manejar cambios en los inputs
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    // Limpiar error cuando el usuario escribe
    if (error) setError('')
  }

  // Manejar submit del formulario
  const handleSubmit = async (e) => {
    e.preventDefault()

    // Validaciones básicas
    if (!formData.email || !formData.password) {
      setError('Por favor, completa todos los campos')
      return
    }

    setLoading(true)
    setError('')

    try {
      // Llamar al servicio de login
      const response = await authService.login(formData.email, formData.password)

      // Login exitoso
      console.log('Login exitoso:', response)

      // Sincronizar tareas locales si existen
      const localTasks = localStorageService.getTasks()
      if (localTasks.length > 0) {
        setSyncing(true)
        try {
          await taskService.syncLocalTasks(response.id || response.usuario?.id)
          console.log('Tareas sincronizadas exitosamente')
        } catch (syncError) {
          console.error('Error sincronizando tareas:', syncError)
          // Continuar aunque falle la sincronización
        } finally {
          setSyncing(false)
        }
      }

      // Redirigir a la página principal
      navigate('/')
    } catch (err) {
      // Manejar error
      setError(err.message || 'Error al iniciar sesión. Por favor, intenta nuevamente.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[400px] flex items-center justify-center px-4 py-8">
      <Card className="w-full max-w-md card-elevated shadow-medium animate-scale-in">
        {/* ─────────────────────────────────────────────────────────
            Header con icono y gradiente
            ───────────────────────────────────────────────────────── */}
        <CardHeader className="text-center pb-6">
          <div className="flex justify-center mb-5">
            {/* Icono con fondo gradiente Azul Oscuro → Azul Claro */}
            <div className="flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-primary text-white shadow-soft">
              <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
          </div>
          <CardTitle className="text-2xl font-bold text-foreground mb-2">
            Bienvenido de nuevo
          </CardTitle>
          <CardDescription className="text-base">
            Inicia sesión para continuar con tu progreso
          </CardDescription>
        </CardHeader>

        {/* ─────────────────────────────────────────────────────────
            Formulario de login
            ───────────────────────────────────────────────────────── */}
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Email */}
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-semibold text-foreground">
                Correo electrónico
              </label>
              <Input
                id="email"
                name="email"
                type="email"
                placeholder="tu@email.com"
                value={formData.email}
                onChange={handleChange}
                disabled={loading}
                autoComplete="email"
                aria-label="Correo electrónico"
                className="input-enhanced"
              />
            </div>

            {/* Password */}
            <div className="space-y-2">
              <label htmlFor="password" className="text-sm font-semibold text-foreground">
                Contraseña
              </label>
              <Input
                id="password"
                name="password"
                type="password"
                placeholder="••••••••"
                value={formData.password}
                onChange={handleChange}
                disabled={loading}
                autoComplete="current-password"
                aria-label="Contraseña"
                className="input-enhanced"
              />
            </div>

            {/* Error message - Ambar para atención */}
            {error && (
              <div className="p-3 rounded-xl bg-warning-light/50 border border-warning/30 shadow-soft animate-fade-in">
                <p className="text-sm font-medium text-warning flex items-center gap-2">
                  <svg className="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {error}
                </p>
              </div>
            )}

            {/* Submit button - Azul Oscuro para enfoque */}
            <Button
              type="submit"
              className="w-full btn-primary-gradient shadow-soft hover:shadow-medium"
              disabled={loading || syncing}
              loading={loading || syncing}
            >
              {syncing ? (
                <>
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Sincronizando tus tareas...
                </>
              ) : loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </Button>

            {/* Link to register - Azul Claro para información */}
            <div className="text-center text-sm pt-2">
              <span className="text-muted-foreground">¿No tienes cuenta? </span>
              <button
                type="button"
                onClick={() => navigate('/register')}
                className="text-info hover:text-primary font-medium transition-smooth focus-ring underline underline-offset-4"
              >
                Regístrate gratis
              </button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
