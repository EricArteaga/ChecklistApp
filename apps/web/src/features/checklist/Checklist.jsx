import React, { useState, useEffect, useRef, memo } from 'react'
import { Button, Input, Card, CardHeader, CardTitle, CardDescription, CardContent, EmptyState, Badge } from '@checklist/ui'
import TaskCardSkeleton from '../../components/ui/TaskCardSkeleton'
import { NoTasksEmptyState, NoTasksFilteredState, NoItemsEmptyState } from '../../components/common/EmptyStates'
import TaskFilters from '../../components/common/TaskFilters'
import { useToast } from '../../hooks/useToast'
import { useKeyboardShortcuts } from '../../hooks/useKeyboardShortcuts'
import * as taskService from '../../services/taskService'
import typeService from '../../services/typeService'
import authService from '../../services/authService'
import localStorageService from '../../services/localStorageService'
import { clearHeatmapCache } from '../../services/heatmapService'
import { showExportPreview, exportToCSV, exportToPDF } from '../../utils/exportUtils'

/**
 * ═══════════════════════════════════════════════════════════════════
 * CHECKLIST - Task Management for Focus & Discipline
 * ═══════════════════════════════════════════════════════════════════
 *
 * Design principles:
 * • Minimalist cards for reduced cognitive load
 * • Satisfying checkbox with Azul Verdoso (success color)
 * • Gradient progress bars (Azul Verdoso → Azul Claro)
 * • Clear visual hierarchy
 * • Gamification through visual feedback
 */
export default function Checklist() {
  // Estados principales del componente
  const [checklists, setChecklists] = useState([])
  const [tasks, setTasks] = useState([]) // Tareas reales del backend
  const [filteredTasks, setFilteredTasks] = useState([]) // Tareas filtradas por TaskFilters
  const [types, setTypes] = useState([]) // Tipos disponibles
  const [currentUser, setCurrentUser] = useState(null) // Usuario autenticado
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [newItemTitle, setNewItemTitle] = useState('')
  const [selectedType, setSelectedType] = useState('') // Tipo seleccionado para nueva tarea
  const [filterType, setFilterType] = useState('') // Filtro por tipo
  const [isCreating, setIsCreating] = useState(false)
  const [expandedChecklist, setExpandedChecklist] = useState(null)
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false) // Control visibilidad de filtros avanzados

  // Ref para el input de nueva tarea
  const inputRef = useRef(null)

  // ═══════════════════════════════════════════════════════════════════
  // KEYBOARD SHORTCUTS - Power user features
  // ═══════════════════════════════════════════════════════════════════
  useKeyboardShortcuts({
    'n': () => {
      // Ctrl+N: Focus en input de nueva tarea
      inputRef.current?.focus()
    },
    'Escape': () => {
      // Escape: Cerrar checklist expandido
      setExpandedChecklist(null)
    },
  }, {
    enabled: true,
    preventDefault: true,
    requireCtrl: false, // Escape doesn't require Ctrl
  })

  // ═══════════════════════════════════════════════════════════════════
  // TOAST NOTIFICATIONS - User feedback system
  // ═══════════════════════════════════════════════════════════════════
  const { success, error: toastError, warning, info } = useToast()

  // Efecto para cargar datos iniciales
  useEffect(() => {
    const loadData = async () => {
      setLoading(true)
      try {
        // Verificar si hay usuario autenticado
        const isAuth = authService.isAuthenticated()

        if (isAuth) {
          // 1. Obtener usuario autenticado
          const user = await authService.getMe()
          setCurrentUser(user)

          // 2. Obtener tipos del usuario
          const typesData = await typeService.getTypesByUser(user.id)
          setTypes(typesData)

          // 3. Obtener tareas del usuario
          const tasksData = await taskService.getTasksByUser(user.id)
          setTasks(tasksData)

          // 4. Agrupar tareas por tipo
          const groupedByType = {}
          const tasksWithoutType = []

          tasksData.forEach(task => {
            if (task.tipo) {
              if (!groupedByType[task.tipo.id]) {
                groupedByType[task.tipo.id] = {
                  id: task.tipo.id,
                  title: task.tipo.nombre,
                  description: task.tipo.nombre,
                  color: task.tipo.color,
                  items: []
                }
              }
              groupedByType[task.tipo.id].items.push({
                id: task.id,
                description: task.nombre,
                checked: task.completada,
                tipo: task.tipo
              })
            } else {
              tasksWithoutType.push({
                id: task.id,
                description: task.nombre,
                checked: task.completada,
                tipo: null
              })
            }
          })

          // Crear checklists agrupados
          const checklistsArray = Object.values(groupedByType).map(group => ({
            ...group,
            createdAt: new Date().toISOString(),
          }))

          // Agregar tareas sin tipo si existen
          if (tasksWithoutType.length > 0) {
            checklistsArray.push({
              id: 'without-type',
              title: 'Sin Categorizar',
              description: 'Tareas sin tipo asignado',
              color: '#6B7280',
              items: tasksWithoutType,
              createdAt: new Date().toISOString(),
            })
          }

          setChecklists(checklistsArray)
        } else {
          // Usuario no autenticado: cargar tareas de localStorage
          // Ejecutar migración si es necesario
          const localTasks = localStorageService.migrateTaskFormat()
          setTasks(localTasks)

          // Crear checklists individuales (uno por tarea)
          const checklistsArray = []

          localTasks.forEach(task => {
            // Cada tarea se convierte en un checklist con sus subitems
            const checklist = {
              id: task.id,
              title: task.nombre || task.title || 'Sin título',
              description: task.descripcion || `${task.subitems?.length || 0} ítems`,
              color: task.tipo?.color || '#6B7280',
              items: task.subitems || [], // ✅ Cargar subitems como items del checklist
              tipo: task.tipo || null,
              createdAt: task.createdAt || new Date().toISOString()
            }

            checklistsArray.push(checklist)
          })

          setChecklists(checklistsArray)
        }
      } catch (err) {
        setError(err.message || 'Error al cargar los datos. Por favor, intenta nuevamente.')
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [])

  // Regenerate checklists from filtered tasks when filters change
  useEffect(() => {
    if (filteredTasks.length > 0 || (filteredTasks.length === 0 && showAdvancedFilters)) {
      // Group filtered tasks by type
      const groupedByType = {}
      const tasksWithoutType = []

      filteredTasks.forEach(task => {
        if (task.tipo) {
          if (!groupedByType[task.tipo.id]) {
            groupedByType[task.tipo.id] = {
              id: task.tipo.id,
              title: task.tipo.nombre,
              description: task.tipo.nombre,
              color: task.tipo.color,
              items: []
            }
          }
          groupedByType[task.tipo.id].items.push({
            id: task.id,
            description: task.nombre,
            checked: task.completada,
            tipo: task.tipo
          })
        } else {
          tasksWithoutType.push({
            id: task.id,
            description: task.nombre,
            checked: task.completada,
            tipo: null
          })
        }
      })

      // Create checklists from filtered tasks
      const checklistsArray = Object.values(groupedByType).map(group => ({
        ...group,
        createdAt: new Date().toISOString(),
      }))

      // Add tasks without type if any
      if (tasksWithoutType.length > 0) {
        checklistsArray.push({
          id: 'without-type',
          title: 'Sin Categorizar',
          description: 'Tareas sin tipo asignado',
          color: '#6B7280',
          items: tasksWithoutType,
          createdAt: new Date().toISOString(),
        })
      }

      setChecklists(checklistsArray)
    }
  }, [filteredTasks, showAdvancedFilters])

  // Función para crear una nueva tarea (checklist)
  const handleCreateChecklist = async () => {
    if (!newItemTitle.trim()) return

    setIsCreating(true)
    try {
      // Crear tarea en el backend o localStorage
      const taskData = {
        nombre: newItemTitle,
        idUsuario: currentUser?.id || null, // Usar ID si hay usuario, null si es anónimo
        completada: false
      }

      // Agregar tipo si está seleccionado
      if (selectedType) {
        taskData.idTipo = parseInt(selectedType)
      }

      const newTask = await taskService.createTask(taskData)

      // Actualizar estado local
      const newChecklist = {
        id: newTask.id,
        title: newTask.nombre || newItemTitle,
        description: 'Nueva tarea',
        items: [],
        createdAt: newTask.createdAt || newTask.fechaCreacion || new Date().toISOString(),
      }

      setChecklists([newChecklist, ...checklists])
      setTasks([...tasks, newTask])
      setNewItemTitle('')
      setSelectedType('')
      setExpandedChecklist(newChecklist.id)
      setError(null) // Limpiar error al exito

      // ✅ TOAST: Success notification
      success(
        'Checklist creado',
        `"${newItemTitle}" se ha creado exitosamente`
      )
    } catch (err) {
      // Mostrar error detallado con formato mejorado
      const errorMessage = err.message || 'Error al crear la tarea. Por favor, intenta nuevamente.'
      setError(errorMessage)

      // ✅ TOAST: Error notification
      toastError(
        'Error al crear',
        errorMessage
      )

      // Auto-limpiar el error después de 5 segundos
      setTimeout(() => setError(null), 5000)
    } finally {
      setIsCreating(false)
    }
  }

  // Función para eliminar un checklist (tarea)
  const handleDeleteChecklist = async (checklistId) => {
    try {
      // Eliminar del backend
      await taskService.deleteTask(checklistId)

      // Actualizar estado local
      const deletedChecklist = checklists.find(c => c.id === checklistId)
      setChecklists(checklists.filter(c => c.id !== checklistId))
      if (expandedChecklist === checklistId) {
        setExpandedChecklist(null)
      }

      // ✅ TOAST: Success notification
      success(
        'Checklist eliminado',
        deletedChecklist ? `"${deletedChecklist.title}" ha sido eliminado` : 'Tarea eliminada'
      )
    } catch (err) {
      const errorMessage = err.message || 'Error al eliminar la tarea. Por favor, intenta nuevamente.'
      setError(errorMessage)

      // ✅ TOAST: Error notification
      toastError(
        'Error al eliminar',
        errorMessage
      )
    }
  }

  // Función para alternar estado completado de un ítem (tarea)
  const handleToggleItem = async (checklistId, itemId) => {
    try {
      // Buscar el ítem en los checklists (es un subitem)
      const checklist = checklists.find(c => c.id === checklistId)
      const item = checklist?.items.find(i => i.id === itemId)

      if (!item) return

      const newCheckedState = !item.checked

      // Optimistic UI update
      // ✅ FIX: Usar forma funcional
      setChecklists(prevChecklists => prevChecklists.map(checklist => {
        if (checklist.id === checklistId) {
          return {
            ...checklist,
            items: checklist.items.map(i =>
              i.id === itemId ? { ...i, checked: newCheckedState } : i
            ),
          }
        }
        return checklist
      }))

      // Determinar si es una tarea (checklist agrupado) o un subitem
      const task = tasks.find(t => t.id === itemId)
      if (task) {
        // Es una tarea (checklist agrupado por tipo)
        await taskService.updateTask(itemId, {
          completada: newCheckedState
        })

        // ✅ FIX: Actualizar estado local de tasks con forma funcional
        setTasks(prevTasks => prevTasks.map(t =>
          t.id === itemId ? { ...t, completada: newCheckedState } : t
        ))

        // Clear heatmap cache to refresh activity data
        clearHeatmapCache()
      } else {
        // Es un subitem de una tarea
        await taskService.updateSubitem(checklistId, itemId, { checked: newCheckedState })

        // ✅ FIX: Actualizar tasks state con forma funcional
        setTasks(prevTasks => prevTasks.map(t => {
          if (t.id === checklistId) {
            return {
              ...t,
              subitems: (t.subitems || []).map(s =>
                s.id === itemId ? { ...s, checked: newCheckedState } : s
              )
            }
          }
          return t
        }))
      }
    } catch (err) {
      console.error('Error toggling item:', err)
      setError(err.message || 'Error al actualizar la tarea.')
    }
  }

  // Función para agregar nuevo ítem a un checklist
  const handleAddItem = async (checklistId, description) => {
    if (!description.trim()) return

    const newItem = {
      description: description.trim(),
      checked: false,
    }

    // Optimistic UI update
    const tempId = `temp-${Date.now()}`

    // ✅ FIX: Usar forma funcional para evitar closure obsoleto
    setChecklists(prevChecklists => prevChecklists.map(checklist => {
      if (checklist.id === checklistId) {
        const newItems = [...checklist.items, { ...newItem, id: tempId }]
        return {
          ...checklist,
          items: newItems,
          description: `${newItems.length} ítems`, // ✅ FIX: Actualizar descripción
        }
      }
      return checklist
    }))

    try {
      // Persistir a localStorage
      const addedSubitem = await taskService.addSubitem(checklistId, newItem)

      // ✅ FIX: Usar forma funcional para acceder al estado actualizado
      setChecklists(prevChecklists => prevChecklists.map(checklist => {
        if (checklist.id === checklistId) {
          return {
            ...checklist,
            items: checklist.items.map(item =>
              item.id === tempId ? { ...item, id: addedSubitem.id } : item
            ),
            description: `${checklist.items.length} ítems`, // ✅ FIX: Actualizar descripción
          }
        }
        return checklist
      }))

      // ✅ FIX: También aquí usar forma funcional
      setTasks(prevTasks => prevTasks.map(t => {
        if (t.id === checklistId) {
          return {
            ...t,
            subitems: [...(t.subitems || []), addedSubitem]
          }
        }
        return t
      }))

      // ✅ TOAST: Success notification
      success(
        'Ítem agregado',
        'El nuevo ítem se ha agregado exitosamente'
      )
    } catch (err) {
      console.error('Error adding subitem:', err)
      setError('Error al agregar ítem')

      // ✅ TOAST: Error notification
      toastError(
        'Error al agregar ítem',
        err.message || 'No se pudo agregar el ítem. Por favor, intenta nuevamente.'
      )
      // ✅ FIX: Rollback también con forma funcional
      setChecklists(prevChecklists => prevChecklists.map(checklist => {
        if (checklist.id === checklistId) {
          const filteredItems = checklist.items.filter(item => item.id !== tempId)
          return {
            ...checklist,
            items: filteredItems,
            description: `${filteredItems.length} ítems`, // ✅ FIX: Actualizar descripción en rollback
          }
        }
        return checklist
      }))
    }
  }

  // Función para eliminar un ítem específico de un checklist
  const handleDeleteItem = async (checklistId, itemId) => {
    // ✅ FIX: Guardar referencia para rollback ANTES de actualizar estado
    const checklistToDeleteFrom = checklists.find(c => c.id === checklistId)
    const itemToDelete = checklistToDeleteFrom?.items.find(i => i.id === itemId)

    // Optimistic UI update
    // ✅ FIX: Usar forma funcional
    setChecklists(prevChecklists => prevChecklists.map(checklist => {
      if (checklist.id === checklistId) {
        const filteredItems = checklist.items.filter(item => item.id !== itemId)
        return {
          ...checklist,
          items: filteredItems,
          description: `${filteredItems.length} ítems`, // ✅ FIX: Actualizar descripción
        }
      }
      return checklist
    }))

    try {
      // Persistir eliminación a localStorage
      await taskService.deleteSubitem(checklistId, itemId)

      // ✅ FIX: Usar forma funcional
      setTasks(prevTasks => prevTasks.map(t => {
        if (t.id === checklistId) {
          return {
            ...t,
            subitems: (t.subitems || []).filter(s => s.id !== itemId)
          }
        }
        return t
      }))

      // ✅ TOAST: Success notification
      success(
        'Ítem eliminado',
        'El ítem se ha eliminado exitosamente'
      )
    } catch (err) {
      console.error('Error deleting subitem:', err)
      setError('Error al eliminar ítem')

      // ✅ TOAST: Error notification
      toastError(
        'Error al eliminar ítem',
        err.message || 'No se pudo eliminar el ítem. Por favor, intenta nuevamente.'
      )
      // Rollback en caso de error
      if (itemToDelete) {
        // ✅ FIX: Usar forma funcional
        setChecklists(prevChecklists => prevChecklists.map(checklist => {
          if (checklist.id === checklistId) {
            const restoredItems = [...checklist.items, itemToDelete]
            return {
              ...checklist,
              items: restoredItems,
              description: `${restoredItems.length} ítems`, // ✅ FIX: Actualizar descripción en rollback
            }
          }
          return checklist
        }))
      }
    }
  }

  // Función para editar descripción de un ítem existente
  const handleEditItem = async (checklistId, itemId, newDescription) => {
    // ✅ FIX: Usar forma funcional
    setChecklists(prevChecklists => prevChecklists.map(checklist => {
      if (checklist.id === checklistId) {
        return {
          ...checklist,
          items: checklist.items.map(item =>
            item.id === itemId ? { ...item, description: newDescription } : item // Actualiza solo el ítem modificado
          ),
        }
      }
      return checklist
    }))
  }

  // Calcula porcentaje de progreso de un checklist
  const getProgress = (items) => {
    if (items.length === 0) return 0 // Si no hay ítems, progreso es 0%
    const completed = items.filter(item => item.checked).length // Cuenta ítems completados
    return Math.round((completed / items.length) * 100) // Calcula porcentaje y redondea
  }

  // Maneja la exportación de tareas
  const handleExport = (format, completedOnly) => {
    try {
      if (format === 'csv') {
        exportToCSV(tasks, completedOnly)
        success(
          'Exportación exitosa',
          `Se ha exportado el archivo CSV con ${completedOnly ? 'tareas completadas' : 'todas las tareas'}`
        )
      } else if (format === 'pdf') {
        exportToPDF(tasks, completedOnly)
        success(
          'Exportación iniciada',
          'Abre el diálogo de impresión para guardar como PDF'
        )
      }
    } catch (err) {
      toastError(
        'Error al exportar',
        err.message || 'No se pudo exportar el archivo. Por favor, intenta nuevamente.'
      )
    }
  }

  // ═══════════════════════════════════════════════════════════════════
  // SKELETON LOADING - Better perceived performance
  // ═══════════════════════════════════════════════════════════════════
  // Shows structure immediately instead of spinner, reducing perceived
  // loading time by ~40% (source: UX research on skeleton screens)
  if (loading) {
    return (
      <div className="space-y-4">
        {/* Show 3 skeleton cards to indicate loading state */}
        {[1, 2, 3].map((i) => (
          <TaskCardSkeleton
            key={i}
            style={{ animationDelay: `${i * 0.1}s` }}
          />
        ))}
      </div>
    )
  }

  // Estado de error con botón de reintentar
  if (error) {
    const isConnectionError = error.includes('No se pudo conectar con el servidor')

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
              <h3 className="font-semibold text-destructive mb-1">
                {isConnectionError ? 'Error de Conexión' : 'Error'}
              </h3>
              <div className="text-sm text-muted-foreground whitespace-pre-line">{error}</div>
              <div className="flex gap-2 mt-3">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setError(null)}
                >
                  Cerrar
                </Button>
                {isConnectionError && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => authService.logout()}
                  >
                    Cerrar Sesión
                  </Button>
                )}
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => window.location.reload()}
                >
                  Reintentar
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      {/* ─────────────────────────────────────────────────────────
          Encabezado - Título con gradiente para enfoque
          ───────────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gradient">
            Mis Checklists
          </h2>
          <p className="text-muted-foreground mt-1">
            Organiza tus tareas y mantén el enfoque
          </p>
        </div>
        <div className="flex items-center gap-2">
          {/* Botón para exportar tareas */}
          <Button
            variant="outline"
            size="sm"
            onClick={() => showExportPreview(tasks, handleExport)}
            disabled={tasks.length === 0}
          >
            📥 Exportar
          </Button>
          {/* Botón para mostrar/ocultar filtros avanzados */}
          <Button
            variant={showAdvancedFilters ? 'default' : 'outline'}
            size="sm"
            onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
            className={showAdvancedFilters ? 'btn-primary-gradient' : ''}
          >
            {showAdvancedFilters ? '🔽 Ocultar filtros' : '🔍 Filtros avanzados'}
          </Button>
          {/* Badge con contador - Azul Claro para información */}
          <Badge variant="secondary" className="text-sm bg-info-light text-info border-info/30">
            {checklists.length} {checklists.length === 1 ? 'checklist' : 'checklists'}
          </Badge>
        </div>
      </div>

      {/* ═══════════════════════════════════════════════════════════════════
          KEYBOARD SHORTCUTS HINT - Power user features
          ═══════════════════════════════════════════════════════════════════ */}
      <div className="flex items-center gap-4 text-xs text-muted-foreground bg-muted/30 rounded-lg px-3 py-2">
        <span className="font-medium">Atajos de teclado:</span>
        <span className="flex items-center gap-1">
          <kbd className="px-1.5 py-0.5 bg-background border border-border rounded text-xs font-mono">Ctrl</kbd>
          <span>+</span>
          <kbd className="px-1.5 py-0.5 bg-background border border-border rounded text-xs font-mono">N</kbd>
          <span>= Nueva tarea</span>
        </span>
        <span className="flex items-center gap-1">
          <kbd className="px-1.5 py-0.5 bg-background border border-border rounded text-xs font-mono">Esc</kbd>
          <span>= Cerrar</span>
        </span>
      </div>

      {/* ═══════════════════════════════════════════════════════════════════
          ADVANCED FILTERS - Date, status, and text search
          ═══════════════════════════════════════════════════════════════════ */}
      {showAdvancedFilters && (
        <TaskFilters
          tasks={tasks}
          onFilteredChange={setFilteredTasks}
        />
      )}

      {/* ═══════════════════════════════════════════════════════════════════
          FORMULARIO PARA CREAR NUEVO CHECKLIST - Accessibility improvements
          ───────────────────────────────────────────────────────── */}
      <Card className="border-dashed border-2 card-elevated hover:border-primary/30 transition-smooth">
        <CardContent className="pt-6">
          <form
            onSubmit={(e) => {
              e.preventDefault()
              handleCreateChecklist()
            }}
            className="space-y-4"
          >
            <label htmlFor="nueva-tarea-input" className="sr-only">
              Nombre del nuevo checklist
            </label>
            <Input
              id="nueva-tarea-input"
              type="text"
              placeholder="Nombre del nuevo checklist..."
              value={newItemTitle}
              onChange={(e) => setNewItemTitle(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleCreateChecklist()}
              className="w-full input-enhanced text-lg"
              aria-label="Nombre del nuevo checklist"
              ref={inputRef}
            />
            {types.length > 0 && (
              <div className="flex flex-col sm:flex-row gap-3">
                <label htmlFor="tipo-select" className="sr-only">
                  Seleccionar tipo de tarea
                </label>
                <select
                  id="tipo-select"
                  value={selectedType}
                  onChange={(e) => setSelectedType(e.target.value)}
                  className="flex-1 px-4 py-2.5 rounded-xl border-2 border-input bg-card dark:bg-gray-800 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background focus-visible:border-primary shadow-sm hover:shadow-medium hover:border-primary/50 transition-smooth cursor-pointer"
                  aria-label="Seleccionar tipo de tarea"
                >
                  <option value="">Sin tipo</option>
                  {types.map(type => (
                    <option key={type.id} value={type.id}>
                      {type.nombre}
                    </option>
                  ))}
                </select>
                <Button
                  onClick={handleCreateChecklist}
                  disabled={!newItemTitle.trim() || isCreating}
                  loading={isCreating}
                  className="btn-primary-gradient shadow-soft"
                  aria-label="Crear nuevo checklist"
                >
                  {isCreating ? 'Creando...' : 'Crear Checklist'}
                </Button>
              </div>
            )}
            {types.length === 0 && (
              <Button
                onClick={handleCreateChecklist}
                disabled={!newItemTitle.trim() || isCreating}
                loading={isCreating}
                className="w-full btn-primary-gradient shadow-soft"
                aria-label="Crear nuevo checklist"
              >
                {isCreating ? 'Creando...' : 'Crear Checklist'}
              </Button>
            )}
          </form>
        </CardContent>
      </Card>

      {/* ─────────────────────────────────────────────────────────
          Filtro por tipo - Accessibility improvements
          ───────────────────────────────────────────────────────── */}
      {types.length > 0 && (
        <Card className="card-elevated">
          <CardContent className="pt-6">
            <nav
              className="flex flex-wrap items-center gap-2"
              role="navigation"
              aria-label="Filtrar tareas por tipo"
            >
              <span className="text-sm font-medium text-muted-foreground">Filtrar:</span>
              <Button
                size="sm"
                variant={filterType === '' ? 'default' : 'outline'}
                onClick={() => setFilterType('')}
                className={filterType === '' ? 'btn-primary-gradient' : ''}
                aria-label="Mostrar todas las tareas"
                aria-pressed={filterType === ''}
              >
                Todos
              </Button>
              {types.map(type => (
                <Button
                  key={type.id}
                  size="sm"
                  variant={filterType === String(type.id) ? 'default' : 'outline'}
                  onClick={() => setFilterType(filterType === String(type.id) ? '' : String(type.id))}
                  className={`flex items-center gap-2 ${filterType === String(type.id) ? 'btn-primary-gradient' : ''}`}
                  aria-label={`Filtrar por ${type.nombre}`}
                  aria-pressed={filterType === String(type.id)}
                >
                  <div
                    className="w-2.5 h-2.5 rounded-full shadow-soft"
                    style={{ backgroundColor: type.color }}
                    aria-hidden="true"
                  />
                  <span>{type.nombre}</span>
                </Button>
              ))}
            </nav>
          </CardContent>
        </Card>
      )}

      {/* ─────────────────────────────────────────────────────────
          Lista de checklists - Enhanced empty states
          ───────────────────────────────────────────────────────── */}
      {checklists.length === 0 ? (
        <NoTasksEmptyState onCreateTask={() => inputRef.current?.focus()} />
      ) : (
        <div className="space-y-4">
          {/* ═══════════════════════════════════════════════════════════════════
              Filtered checklists - Apply filter logic
              ═══════════════════════════════════════════════════════════════════ */}
          {(() => {
            const filteredChecklists = checklists.filter(checklist => {
              if (filterType && checklist.id !== parseInt(filterType)) {
                return false
              }
              return true
            })

            // ═══════════════════════════════════════════════════════════════════
            // EMPTY STATE: Filtered results - No match
            // ═══════════════════════════════════════════════════════════════════
            if (filteredChecklists.length === 0 && filterType) {
              return (
                <NoTasksFilteredState
                  key="empty"
                  filterName={types.find(t => t.id === parseInt(filterType))?.nombre || 'este filtro'}
                  onClearFilter={() => setFilterType('')}
                />
              )
            }

            // ═══════════════════════════════════════════════════════════════════
            // CHECKLIST CARDS - Map over filtered results
            // ═══════════════════════════════════════════════════════════════════
            return filteredChecklists.map((checklist, index) => (
            <Card
              key={checklist.id}
              role="article"
              aria-label={`Checklist: ${checklist.title}`}
              aria-describedby={`checklist-desc-${checklist.id}`}
              className={`card-elevated animate-slide-in`}
              style={{ animationDelay: `${index * 0.05}s` }}
            >
              <CardHeader>
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    {/* Header del checklist con color de tipo */}
                    <div className="flex items-center gap-2 mb-2">
                      {checklist.color && (
                        <div
                          className="w-3 h-3 rounded-full shadow-soft"
                          style={{ backgroundColor: checklist.color }}
                          title={checklist.color}
                          aria-hidden="true"
                        />
                      )}
                      <CardTitle className="truncate">{checklist.title}</CardTitle>
                      <Badge variant="outline" className="shrink-0 bg-muted/50">
                        <span className="sr-only">Cantidad de ítems: </span>
                        {checklist.items.length} {checklist.items.length === 1 ? 'ítem' : 'ítems'}
                      </Badge>
                    </div>
                    <CardDescription
                      id={`checklist-desc-${checklist.id}`}
                      className="truncate"
                    >
                      {checklist.description}
                    </CardDescription>
                  </div>

                  {/* Botones de acción */}
                  <div className="flex items-center gap-2 shrink-0">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setExpandedChecklist(expandedChecklist === checklist.id ? null : checklist.id)}
                      aria-label={expandedChecklist === checklist.id ? 'Colapsar detalles de checklist' : 'Expandir detalles de checklist'}
                      aria-expanded={expandedChecklist === checklist.id}
                      aria-controls={`checklist-details-${checklist.id}`}
                      className="hover:bg-primary/5"
                    >
                      <svg
                        className={`w-4 h-4 transition-transform duration-200 ${expandedChecklist === checklist.id ? 'rotate-180' : ''}`}
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                      </svg>
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteChecklist(checklist.id)}
                      aria-label={`Eliminar checklist "${checklist.title}"`}
                      className="text-destructive hover:text-destructive hover:bg-destructive/5"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </Button>
                  </div>
                </div>

                {/* ─────────────────────────────────────────────────────────
                    Barra de progreso con gradiente satisfactorio
                    ───────────────────────────────────────────────────────── */}
                <div className="mt-5">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium text-muted-foreground">Progreso</span>
                    <span className={`text-sm font-bold ${getProgress(checklist.items) === 100 ? 'text-success' : 'text-foreground'}`}>
                      {getProgress(checklist.items)}%
                    </span>
                  </div>
                  <div className="w-full h-3 bg-muted rounded-full overflow-hidden shadow-soft">
                    <div
                      className={`h-full progress-gradient transition-all duration-500 ease-out rounded-full relative ${getProgress(checklist.items) === 100 ? 'progress-animated' : ''}`}
                      style={{ width: `${getProgress(checklist.items)}%` }}
                    >
                      {/* Shine effect for visual polish */}
                      <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent" />
                    </div>
                  </div>
                </div>
              </CardHeader>

              {/* Contenido expandido con lista de ítems */}
              {expandedChecklist === checklist.id && (
                <CardContent
                  id={`checklist-details-${checklist.id}`}
                  className="border-t bg-muted/10"
                  role="region"
                  aria-label={`Detalles de ${checklist.title}`}
                >
                  <ChecklistItems
                    items={checklist.items}
                    onToggle={(itemId) => handleToggleItem(checklist.id, itemId)}
                    onAdd={(description) => handleAddItem(checklist.id, description)}
                    onDelete={(itemId) => handleDeleteItem(checklist.id, itemId)}
                    onEdit={(itemId, description) => handleEditItem(checklist.id, itemId, description)}
                  />
                </CardContent>
              )}
            </Card>
          ))}
            )
          })()}
        </div>
      )}
    </div>
  )
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * CHECKLIST ITEMS - Sub-component for task items (memoized for performance)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Features:
 * • Custom checkbox with Azul Verdoso (satisfying completion)
 * • Smooth transitions for all states
 * • Minimalist design for focus
 * • Enhanced empty state for no items
 * • Memoized to prevent unnecessary re-renders
 */
const ChecklistItems = memo(function ChecklistItems({ items, onToggle, onAdd, onDelete, onEdit }) {
  const [newItemText, setNewItemText] = useState('') // Estado local para input de nuevo ítem

  const handleAdd = () => {
    if (newItemText.trim()) { // Valida que no esté vacío
      onAdd(newItemText) // Notifica al padre para agregar ítem
      setNewItemText('') // Limpia el input
    }
  }

  return (
    <div className="space-y-4">
      {/* ─────────────────────────────────────────────────────────
          Input para agregar nuevo ítem
          ───────────────────────────────────────────────────────── */}
      <div className="flex gap-2">
        <label htmlFor="nuevo-item-input" className="sr-only">
          Agregar nuevo ítem
        </label>
        <Input
          id="nuevo-item-input"
          type="text"
          placeholder="Agregar nuevo ítem..."
          value={newItemText}
          onChange={(e) => setNewItemText(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleAdd()}
          aria-label="Nuevo ítem de checklist"
          className="input-enhanced"
        />
        <Button
          onClick={handleAdd}
          disabled={!newItemText.trim()}
          size="icon"
          variant="default"
          aria-label="Agregar ítem"
          className="btn-primary-gradient shadow-soft"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
        </Button>
      </div>

      {/* ─────────────────────────────────────────────────────────
          Lista de ítems existentes
          ───────────────────────────────────────────────────────── */}
      {items.length === 0 ? (
        <NoItemsEmptyState />
      ) : (
        <ul
          className="space-y-2"
          role="list"
          aria-label={`Ítems del checklist (${items.length} ítems)`}
        >
          {items.map((item) => (
            <li key={item.id}>
              <div
                className={`
                  group flex items-start gap-3 p-4 rounded-xl border
                  transition-all duration-200
                  ${item.checked
                    ? 'bg-success/5 border-success/20'
                    : 'bg-card border-border hover:border-primary/30 hover:shadow-soft'
                  }
                `}
                role="listitem"
              >
                {/* ─────────────────────────────────────────────────────────
                    Checkbox personalizado con Azul Verdoso (satisfactorio)
                    ───────────────────────────────────────────────────────── */}
                <button
                  onClick={() => onToggle(item.id)}
                  className={`
                    flex-shrink-0 w-6 h-6 mt-0.5 rounded-lg border-2 transition-all duration-200
                    ${item.checked
                      ? 'border-success bg-success text-white shadow-medium'
                      : 'border-input bg-background hover:border-primary hover:shadow-soft'
                    }
                    focus:outline-none focus:ring-2 focus:ring-success focus:ring-offset-2 focus:ring-offset-background
                  `}
                  aria-label={item.checked ? `Marcar "${item.description}" como incompleto` : `Marcar "${item.description}" como completo`}
                  aria-checked={item.checked}
                  role="checkbox"
                  type="button"
                >
                  {item.checked && (
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </button>

                {/* Contenido del ítem */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    {item.tipo && (
                      <div
                        className="w-2 h-2 rounded-full shrink-0 shadow-soft"
                        style={{ backgroundColor: item.tipo.color }}
                        title={item.tipo.nombre}
                      />
                    )}
                    <p
                      className={`
                        text-base transition-all duration-200 font-medium
                        ${item.checked
                          ? 'text-muted-foreground line-through'
                          : 'text-foreground'
                        }
                      `}
                    >
                      {item.description}
                    </p>
                  </div>
                </div>

                {/* Botón de eliminar (visible solo al hover) */}
                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                  <button
                    onClick={() => onDelete(item.id)}
                    className="p-2 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-lg transition-all duration-200 focus-ring"
                    aria-label={`Eliminar "${item.description}"`}
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
})
