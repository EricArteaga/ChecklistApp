import React, { useState } from 'react'
import { Button, Input, Card, CardHeader, CardTitle, CardDescription, CardContent } from '@checklist/ui'
import { useNavigate } from 'react-router-dom'
import authService from '../../services/authService'
import taskService from '../../services/taskService'
import localStorageService from '../../services/localStorageService'

/**
 * ═══════════════════════════════════════════════════════════════════
 * REGISTER - Clean Onboarding Experience
 * ═══════════════════════════════════════════════════════════════════
 *
 * Design principles:
 * • Focused onboarding flow
 * • Azul Verdoso accent for growth/creation theme
 * • Clear validation feedback
 * • Minimal cognitive load
 * • Welcoming, encouraging atmosphere
 */
export default function Register() {
  const navigate = useNavigate()

  // Estados del formulario
  const [formData, setFormData] = useState({
    nombre: '',
    email: '',
    password: '',
    confirmPassword: ''
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

  // Validar formulario
  const validateForm = () => {
    if (!formData.nombre.trim()) {
      setError('El nombre es requerido')
      return false
    }

    if (!formData.email.trim()) {
      setError('El correo electrónico es requerido')
      return false
    }

    // Validación básica de email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(formData.email)) {
      setError('Ingresa un correo electrónico válido')
      return false
    }

    if (!formData.password) {
      setError('La contraseña es requerida')
      return false
    }

    if (formData.password.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres')
      return false
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Las contraseñas no coinciden')
      return false
    }

    return true
  }

  // Manejar submit del formulario
  const handleSubmit = async (e) => {
    e.preventDefault()

    // Validar formulario
    if (!validateForm()) {
      return
    }

    setLoading(true)
    setError('')

    try {
      // Llamar al servicio de registro
      const response = await authService.register(
        formData.nombre,
        formData.email,
        formData.password
      )

      // Registro exitoso
      console.log('Registro exitoso:', response)

      // Sincronizar tareas locales si existen
      const localTasks = localStorageService.getTasks()
      if (localTasks.length > 0) {
        setSyncing(true)
        try {
          await taskService.syncLocalTasks(response.user?.id)
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
      setError(err.message || 'Error al registrar. Por favor, intenta nuevamente.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[400px] flex items-center justify-center px-4 py-8">
      <Card className="w-full max-w-md card-elevated shadow-medium animate-scale-in">
        {/* ─────────────────────────────────────────────────────────
            Header con icono de creación
            ───────────────────────────────────────────────────────── */}
        <CardHeader className="text-center pb-6">
          <div className="flex justify-center mb-5">
            {/* Icono con fondo gradiente Azul Verdoso (crecimiento) */}
            <div className="flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-success text-white shadow-soft">
              <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
              </svg>
            </div>
          </div>
          <CardTitle className="text-2xl font-bold text-foreground mb-2">
            Crea tu cuenta
          </CardTitle>
          <CardDescription className="text-base">
            Comienza tu journey de productividad hoy
          </CardDescription>
        </CardHeader>

        {/* ─────────────────────────────────────────────────────────
            Formulario de registro
            ───────────────────────────────────────────────────────── */}
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Nombre */}
            <div className="space-y-2">
              <label htmlFor="nombre" className="text-sm font-semibold text-foreground">
                Nombre completo
              </label>
              <Input
                id="nombre"
                name="nombre"
                type="text"
                placeholder="Juan Pérez"
                value={formData.nombre}
                onChange={handleChange}
                disabled={loading}
                autoComplete="name"
                aria-label="Nombre completo"
                className="input-enhanced"
              />
            </div>

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
                placeholder="Mínimo 6 caracteres"
                value={formData.password}
                onChange={handleChange}
                disabled={loading}
                autoComplete="new-password"
                aria-label="Contraseña"
                className="input-enhanced"
              />
            </div>

            {/* Confirm Password */}
            <div className="space-y-2">
              <label htmlFor="confirmPassword" className="text-sm font-semibold text-foreground">
                Confirmar contraseña
              </label>
              <Input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                placeholder="Repite tu contraseña"
                value={formData.confirmPassword}
                onChange={handleChange}
                disabled={loading}
                autoComplete="new-password"
                aria-label="Confirmar contraseña"
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

            {/* Submit button - Azul Verdoso para creación */}
            <Button
              type="submit"
              className="w-full bg-gradient-success text-white font-medium px-4 py-3 rounded-xl shadow-soft hover:shadow-medium transition-smooth hover-lift focus-ring"
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
              ) : loading ? 'Registrando...' : 'Crear Cuenta'}
            </Button>

            {/* Link to login - Azul Claro para información */}
            <div className="text-center text-sm pt-2">
              <span className="text-muted-foreground">¿Ya tienes cuenta? </span>
              <button
                type="button"
                onClick={() => navigate('/login')}
                className="text-info hover:text-primary font-medium transition-smooth focus-ring underline underline-offset-4"
              >
                Inicia sesión
              </button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
