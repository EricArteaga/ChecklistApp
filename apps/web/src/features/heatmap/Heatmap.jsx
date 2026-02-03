import React, { useState, useEffect, useMemo } from 'react' // Importa hooks de React incluyendo useMemo para optimización
import { Card, CardHeader, CardTitle, CardDescription, CardContent, Badge, Button, EmptyState } from '@checklist/ui'

export default function Heatmap() {
  // Estados para gestionar la vista y datos del heatmap
  const [loading, setLoading] = useState(true) // Controla estado de carga inicial
  const [error, setError] = useState(null) // Almacena mensajes de error
  const [viewMode, setViewMode] = useState('weekly') // 'weekly' para vista de 12 meses, 'monthly' para vista mensual
  const [selectedPeriod, setSelectedPeriod] = useState(new Date()) // Período seleccionado (para navegación futura)

  // Efecto para cargar datos iniciales del heatmap
  useEffect(() => {
    const loadData = async () => {
      setLoading(true)
      try {
        // Simula llamada a API con delay de 600ms
        await new Promise(resolve => setTimeout(resolve, 600))
      } catch (err) {
        setError('Error al cargar los datos del heatmap. Por favor, intenta nuevamente.')
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [])

  // Genera datos simulados de actividad para los últimos 365 días
  // useMemo evita recalcular en cada renderizado, optimizando rendimiento
  const activityData = useMemo(() => {
    const data = []
    const today = new Date()

    // Itera desde 364 días atrás hasta hoy (365 días totales)
    for (let i = 364; i >= 0; i--) {
      const date = new Date(today)
      date.setDate(date.getDate() - i) // Calcula fecha específica

      // Genera nivel de actividad aleatorio (0-4): 0=sin actividad, 4=máxima actividad
      const level = Math.floor(Math.random() * 5)
      const count = level === 0 ? 0 : Math.floor(Math.random() * (level * 5)) + 1 // Cantidad de tareas según nivel

      data.push({
        date: date.toISOString(), // Formato ISO para almacenamiento
        dateObj: date, // Objeto Date para comparaciones
        level, // Nivel visual de actividad
        count, // Cantidad numérica de tareas
      })
    }

    return data
  }, []) // Array vacío: solo se calcula una vez al montar

  // Genera datos estructurados para vista semanal (estilo GitHub contribution graph)
  const weeklyData = useMemo(() => {
    const days = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'] // Etiquetas de días
    const today = new Date()
    const startOfWeek = new Date(today)
    startOfWeek.setDate(today.getDate() - today.getDay()) // Domingo como inicio de semana

    return {
      days,
      // Genera 52 semanas (aproximadamente 1 año)
      weeks: Array.from({ length: 12 }, (_, i) => {
        const weekStart = new Date(startOfWeek)
        weekStart.setDate(startOfWeek.getDate() - (11 - i) * 7) // Calcula inicio de cada semana

        // Genera 7 días para cada semana
        return Array.from({ length: 7 }, (_, j) => {
          const day = new Date(weekStart)
          day.setDate(weekStart.getDate() + j) // Día específico de la semana

          // Busca datos de actividad para este día o usa valores por defecto
          const activity = activityData.find(d =>
            d.dateObj.toDateString() === day.toDateString()
          ) || { level: 0, count: 0 }

          return {
            date: day,
            ...activity,
          }
        })
      }),
    }
  }, [activityData]) // Recalcula solo si cambian los datos de actividad

  // Genera datos estructurados para vista mensual (calendario tradicional)
  const monthlyData = useMemo(() => {
    const today = new Date()
    const year = today.getFullYear()
    const month = today.getMonth()

    // Calcula días del mes y día de la semana del primer día
    const daysInMonth = new Date(year, month + 1, 0).getDate()
    const firstDayOfMonth = new Date(year, month, 1).getDay()

    const calendar = []

    // Añade días vacíos del mes anterior para alineación
    for (let i = 0; i < firstDayOfMonth; i++) {
      calendar.push({ day: null, level: 0, count: 0 })
    }

    // Añade todos los días del mes actual
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day)
      const activity = activityData.find(d =>
        d.dateObj.toDateString() === date.toDateString()
      ) || { level: 0, count: 0 }

      calendar.push({
        day,
        date,
        ...activity,
      })
    }

    return {
      monthName: today.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' }), // Formato: "enero 2026"
      calendar,
    }
  }, [activityData])

  // Calcula estadísticas agregadas de la actividad
  const stats = useMemo(() => {
    const totalDays = activityData.length
    const activeDays = activityData.filter(d => d.level > 0).length // Días con alguna actividad
    const totalTasks = activityData.reduce((sum, d) => sum + d.count, 0) // Suma total de tareas
    const currentStreak = calculateStreak(activityData) // Racha actual de días consecutivos

    return {
      totalDays,
      activeDays,
      totalTasks,
      currentStreak,
      activityRate: Math.round((activeDays / totalDays) * 100), // Porcentaje de días activos
    }
  }, [activityData])

  // Función para calcular racha actual de días con actividad
  function calculateStreak(data) {
    let streak = 0
    const today = new Date()

    // Recorre desde hoy hacia atrás contando días consecutivos con actividad
    for (let i = 0; i < data.length; i++) {
      const item = data[i]
      const diffTime = Math.abs(today - item.dateObj)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) // Diferencia en días

      if (diffDays === i && item.level > 0) {
        streak++ // Incrementa si es día consecutivo con actividad
      } else if (diffDays === i && item.level === 0) {
        break // Rompe racha al encontrar día sin actividad
      }
    }

    return streak
  }

// Mapea niveles de actividad a clases de color CSS
  function getActivityColor(level) {
    const colors = {
      0: 'bg-muted', // Sin actividad: gris neutro
      1: 'bg-green-200 dark:bg-green-900', // Baja: verde claro/oscuro según tema
      2: 'bg-green-400 dark:bg-green-700', // Media: verde medio
      3: 'bg-green-600 dark:bg-green-500', // Alta: verde fuerte
      4: 'bg-green-700 dark:bg-green-400', // Muy alta: verde más intenso
    }
    return colors[level] || colors[0] // Retorna color por defecto si level inválido
  }

// Formatea fecha en español para tooltips
  function formatDate(date) {
    return date.toLocaleDateString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  }

// Estado de carga con spinner
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary/10 mb-4">
            <svg className="w-8 h-8 text-primary animate-spin" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          </div>
          <p className="text-muted-foreground">Cargando estadísticas...</p>
        </div>
      </div>
    )
  }

// Estado de error con opciones de reintentar
  if (error) {
    return (
      <Card className="border-destructive">
        <CardContent className="pt-6">
          <div className="flex items-start gap-3">
            <div className="flex-shrink-0 w-10 h-10 rounded-full bg-destructive/10 flex items-center justify-center">
              <svg className="w-5 h-5 text-destructive" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div className="flex-1">
              <h3 className="font-semibold text-destructive mb-1">Error</h3>
              <p className="text-sm text-muted-foreground">{error}</p>
              <Button
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => window.location.reload()} // Recarga página para reintentar
              >
                Reintentar
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      {/* Encabezado con título y selector de vista */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground">
            Actividad
          </h2>
          <p className="text-muted-foreground mt-1">
            Visualiza tu actividad y progreso a lo largo del tiempo
          </p>
        </div>
        {/* Botones para alternar entre modos de vista */}
        <div className="flex items-center gap-2">
          <Button
            variant={viewMode === 'weekly' ? 'default' : 'outline'} // Estilo activo/inactivo
            size="sm"
            onClick={() => setViewMode('weekly')}
          >
            Últimos 12 meses
          </Button>
          <Button
            variant={viewMode === 'monthly' ? 'default' : 'outline'}
            size="sm"
            onClick={() => setViewMode('monthly')}
          >
            Este mes
          </Button>
        </div>
      </div>

      {/* Tarjetas de estadísticas principales */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Tarjeta de racha actual */}
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Racha actual</p>
                <p className="text-2xl font-bold text-foreground mt-1">
                  {stats.currentStreak} <span className="text-sm font-normal text-muted-foreground">días</span>
                </p>
              </div>
              <div className="w-12 h-12 rounded-full bg-orange-100 dark:bg-orange-900/30 flex items-center justify-center">
                <svg className="w-6 h-6 text-orange-600 dark:text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.975 7.975 0 0120 13a7.975 7.975 0 01-2.343 5.657z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.879 16.121A3 3 0 1012.015 11L11 14H9c0 .768.293 1.536.879 2.121z" />
                </svg>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Tarjeta de total de tareas */}
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Total tareas</p>
                <p className="text-2xl font-bold text-foreground mt-1">
                  {stats.totalTasks}
                </p>
              </div>
              <div className="w-12 h-12 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <svg className="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Tarjeta de días activos */}
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Días activos</p>
                <p className="text-2xl font-bold text-foreground mt-1">
                  {stats.activeDays} <span className="text-sm font-normal text-muted-foreground">de {stats.totalDays}</span>
                </p>
              </div>
              <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center">
                <svg className="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Tarjeta de tasa de actividad */}
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Tasa de actividad</p>
                <p className="text-2xl font-bold text-foreground mt-1">
                  {stats.activityRate}%
                </p>
              </div>
              <div className="w-12 h-12 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                <svg className="w-6 h-6 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Visualización principal del heatmap */}
      <Card>
        <CardHeader>
          <CardTitle>
            {viewMode === 'weekly' ? 'Mapa de actividad (12 meses)' : monthlyData.monthName}
          </CardTitle>
          <CardDescription>
            {viewMode === 'weekly'
              ? 'Visualización de tu actividad diaria en los últimos 12 meses'
              : 'Visualización detallada de tu actividad este mes'
            }
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Renderiza componente según modo de vista seleccionado */}
          {viewMode === 'weekly' ? (
            <WeeklyView data={weeklyData} getActivityColor={getActivityColor} formatDate={formatDate} />
          ) : (
            <MonthlyView data={monthlyData} getActivityColor={getActivityColor} formatDate={formatDate} />
          )}

          {/* Leyenda de colores del heatmap */}
          <div className="flex items-center justify-end gap-2 mt-4 pt-4 border-t">
            <span className="text-xs text-muted-foreground">Menos</span>
            <div className="flex gap-1">
              {[0, 1, 2, 3, 4].map(level => (
                <div
                  key={level}
                  className={`w-3 h-3 rounded-sm ${getActivityColor(level)}`}
                  aria-label={`Nivel ${level}`}
                />
              ))}
            </div>
            <span className="text-xs text-muted-foreground">Más</span>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

// Componente para vista semanal estilo GitHub contribution graph
function WeeklyView({ data, getActivityColor, formatDate }) {
  return (
    <div className="overflow-x-auto"> {/* Permite scroll horizontal en pantallas pequeñas */}
      <div className="inline-flex gap-1 min-w-max">
        {/* Etiquetas de días de la semana */}
        <div className="flex flex-col gap-1 mr-2">
          <div className="h-3" /> {/* Espacio vacío alineado con semanas */}
          {data.days.map(day => (
            <div key={day} className="h-3 flex items-center">
              <span className="text-xs text-muted-foreground w-8">{day}</span>
            </div>
          ))}
        </div>

        {/* Columna de semanas con sus días */}
        <div className="flex gap-1">
          {data.weeks.map((week, weekIndex) => (
            <div key={weekIndex} className="flex flex-col gap-1">
              {week.map((day, dayIndex) => (
                <div
                  key={dayIndex}
                  className={`
                    w-3 h-3 rounded-sm transition-all duration-200
                    ${getActivityColor(day.level)}
                    hover:ring-2 hover:ring-ring hover:ring-offset-1
                    cursor-pointer
                  `}
                  title={`${formatDate(day.date)}: ${day.count} tareas`} // Tooltip al hover
                  aria-label={`${formatDate(day.date)}: ${day.count} tareas`} // Accesibilidad
                />
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

// Componente para vista mensual estilo calendario tradicional
function MonthlyView({ data, getActivityColor, formatDate }) {
  return (
    <div className="space-y-2">
      {/* Encabezado con nombres de días de la semana */}
      <div className="grid grid-cols-7 gap-2 text-center">
        {['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'].map(day => (
          <div key={day} className="text-xs font-medium text-muted-foreground">
            {day}
          </div>
        ))}
      </div>

      {/* Grid del calendario con días del mes */}
      <div className="grid grid-cols-7 gap-2">
        {data.calendar.map((cell, index) => (
          <div
            key={index}
            className={`
              aspect-square rounded-lg p-2 flex flex-col items-center justify-center
              transition-all duration-200
              ${cell.day
                ? `${getActivityColor(cell.level)} hover:ring-2 hover:ring-ring hover:ring-offset-2 cursor-pointer` // Días del mes
                : 'bg-transparent' // Espacios vacíos de otros meses
              }
            `}
            title={cell.day ? `${formatDate(cell.date)}: ${cell.count} tareas` : ''}
            aria-label={cell.day ? `${formatDate(cell.date)}: ${cell.count} tareas` : ''}
          >
            {cell.day && (
              <>
                <span className="text-sm font-medium text-foreground">
                  {cell.day} {/* Número del día */}
                </span>
                {cell.count > 0 && (
                  <Badge variant="secondary" className="mt-1 text-xs px-1.5 py-0 h-5">
                    {cell.count} {/* Cantidad de tareas si hay actividad */}
                  </Badge>
                )}
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
