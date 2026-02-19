import React, { useState, useEffect } from 'react'
import { Button, Input, Card, CardHeader, CardTitle, CardDescription, CardContent, EmptyState, Badge } from '@checklist/ui'
import taskService from '../../services/taskService'
import authService from '../../services/authService'

export default function Checklist() {
  // Estados principales del componente
  const [checklists, setChecklists] = useState([])
  const [tasks, setTasks] = useState([]) // Tareas reales del backend
  const [currentUser, setCurrentUser] = useState(null) // Usuario autenticado
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [newItemTitle, setNewItemTitle] = useState('')
  const [isCreating, setIsCreating] = useState(false)
  const [expandedChecklist, setExpandedChecklist] = useState(null)

  // Efecto para cargar datos iniciales
  useEffect(() => {
    const loadData = async () => {
      setLoading(true)
      try {
        // 1. Obtener usuario autenticado
        const user = await authService.getMe()
        setCurrentUser(user)

        // 2. Obtener tareas del usuario
        const tasksData = await taskService.getTasksByUser(user.id)
        setTasks(tasksData)

        // 3. Agrupar tareas por tipo (por ahora usamos todas como un solo checklist)
        // TODO: Implementar agrupación por Type cuando esté listo
        setChecklists([
          {
            id: 'all',
            title: 'Mis Tareas',
            description: 'Todas mis tareas',
            items: tasksData.map(task => ({
              id: task.id,
              description: task.titulo,
              checked: task.completada
            })),
            createdAt: new Date().toISOString(),
          }
        ])
      } catch (err) {
        setError(err.message || 'Error al cargar los datos. Por favor, intenta nuevamente.')
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [])

  // Función para crear una nueva tarea (checklist)
  const handleCreateChecklist = async () => {
    if (!newItemTitle.trim() || !currentUser) return

    setIsCreating(true)
    try {
      // Crear tarea en el backend
      const newTask = await taskService.createTask({
        titulo: newItemTitle,
        idUsuario: currentUser.id,
        completada: false
      })

      // Actualizar estado local
      const newChecklist = {
        id: newTask.id,
        title: newTask.titulo,
        description: 'Nueva tarea',
        items: [],
        createdAt: newTask.fechaCreacion || new Date().toISOString(),
      }

      setChecklists([newChecklist, ...checklists])
      setNewItemTitle('')
      setExpandedChecklist(newChecklist.id)
    } catch (err) {
      setError(err.message || 'Error al crear la tarea. Por favor, intenta nuevamente.')
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
      setChecklists(checklists.filter(c => c.id !== checklistId))
      if (expandedChecklist === checklistId) {
        setExpandedChecklist(null)
      }
    } catch (err) {
      setError(err.message || 'Error al eliminar la tarea. Por favor, intenta nuevamente.')
    }
  }

  // Función para alternar estado completado de un ítem (tarea)
  const handleToggleItem = async (checklistId, itemId) => {
    try {
      // Buscar la tarea actual
      const task = tasks.find(t => t.id === itemId)
      if (!task) return

      // Actualizar en el backend
      await taskService.updateTask(itemId, {
        completada: !task.completada
      })

      // Actualizar estado local (optimista)
      setTasks(tasks.map(t =>
        t.id === itemId ? { ...t, completada: !t.completada } : t
      ))

      setChecklists(checklists.map(checklist => {
        if (checklist.id === checklistId) {
          const updatedItems = checklist.items.map(item =>
            item.id === itemId ? { ...item, checked: !item.checked } : item
          )
          return { ...checklist, items: updatedItems }
        }
        return checklist
      }))
    } catch (err) {
      setError(err.message || 'Error al actualizar la tarea.')
    }
  }

  // Función para agregar nuevo ítem a un checklist
  const handleAddItem = async (checklistId, description) => {
    if (!description.trim()) return // Evita agregar ítems vacíos

    setChecklists(checklists.map(checklist => {
      if (checklist.id === checklistId) {
        const newItem = {
          id: Date.now(), // ID único basado en timestamp
          description: description.trim(), // Elimina espacios extra
          checked: false, // Nuevo ítem comienza como no completado
        }
        return {
          ...checklist,
          items: [...checklist.items, newItem], // Añade nuevo ítem al array existente
        }
      }
      return checklist
    }))
  }

  // Función para eliminar un ítem específico de un checklist
  const handleDeleteItem = async (checklistId, itemId) => {
    setChecklists(checklists.map(checklist => {
      if (checklist.id === checklistId) {
        return {
          ...checklist,
          items: checklist.items.filter(item => item.id !== itemId), // Filtra el ítem a eliminar
        }
      }
      return checklist
    }))
  }

  // Función para editar descripción de un ítem existente
  const handleEditItem = async (checklistId, itemId, newDescription) => {
    setChecklists(checklists.map(checklist => {
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

  // Estado de carga con spinner animado
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
          <p className="text-muted-foreground">Cargando checklists...</p>
        </div>
      </div>
    )
  }

  // Estado de error con botón de reintentar
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
                onClick={() => window.location.reload()} // Recarga la página para reintentar
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
      {/* Sección de encabezado con título y contador */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground">
            Mis Checklists
          </h2>
          <p className="text-muted-foreground mt-1">
            Gestiona tus tareas y mantente organizado
          </p>
        </div>
        <div className="flex items-center gap-2">
          {/* Badge que muestra cantidad de checklists con gramática correcta */}
          <Badge variant="secondary" className="text-sm">
            {checklists.length} {checklists.length === 1 ? 'checklist' : 'checklists'}
          </Badge>
        </div>
      </div>

      {/* Formulario para crear nuevo checklist */}
      <Card className="border-dashed"> {/* Borde punteado para indicar elemento de creación */}
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row gap-3">
            <Input
              type="text"
              placeholder="Nombre del nuevo checklist..."
              value={newItemTitle}
              onChange={(e) => setNewItemTitle(e.target.value)} // Actualiza estado con cada escritura
              onKeyPress={(e) => e.key === 'Enter' && handleCreateChecklist()} // Permite crear con Enter
              className="flex-1"
              aria-label="Nuevo nombre de checklist" // Etiqueta para accesibilidad
            />
            <Button
              onClick={handleCreateChecklist}
              disabled={!newItemTitle.trim() || isCreating} // Deshabilita si está vacío o creando
              loading={isCreating} // Muestra spinner durante creación
            >
              {isCreating ? 'Creando...' : 'Crear Checklist'}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Lista de checklists - muestra estado vacío o lista de cards */}
      {checklists.length === 0 ? (
        <EmptyState
          icon={
            <svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
          }
          title="No hay checklists aún"
          description="Crea tu primer checklist para empezar a organizar tus tareas"
          action={
            <Button onClick={() => document.querySelector('input[aria-label="Nuevo nombre de checklist"]')?.focus()}>
              Crear Checklist
            </Button>
          }
        />
      ) : (
        <div className="space-y-4">
          {checklists.map((checklist, index) => (
            <Card
              key={checklist.id}
              className={`transition-all duration-300 hover:shadow-lg animate-slide-in`}
              style={{ animationDelay: `${index * 0.1}s` }} // Retraso animación escalonado
            >
              <CardHeader>
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0"> {/* min-w-0 permite truncado de texto */}
                    <div className="flex items-center gap-2 mb-1">
                      <CardTitle className="truncate">{checklist.title}</CardTitle>
                      <Badge variant="outline" className="shrink-0">
                        {checklist.items.length} {checklist.items.length === 1 ? 'ítem' : 'ítems'}
                      </Badge>
                    </div>
                    <CardDescription className="truncate">
                      {checklist.description}
                    </CardDescription>
                  </div>
                  {/* Botones de acción para expandir/colapsar y eliminar */}
                  <div className="flex items-center gap-2 shrink-0">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setExpandedChecklist(expandedChecklist === checklist.id ? null : checklist.id)} // Alterna estado expandido
                      aria-label={expandedChecklist === checklist.id ? 'Colapsar' : 'Expandir'}
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
                      onClick={() => handleDeleteChecklist(checklist.id)} // Elimina checklist
                      aria-label="Eliminar checklist"
                      className="text-destructive hover:text-destructive hover:bg-destructive/10"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </Button>
                  </div>
                </div>

                {/* Barra de progreso visual */}
                <div className="mt-4">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm text-muted-foreground">Progreso</span>
                    <span className="text-sm font-medium">{getProgress(checklist.items)}%</span>
                  </div>
                  <div className="w-full h-2 bg-secondary rounded-full overflow-hidden">
                    <div
                      className="h-full bg-primary transition-all duration-500 ease-out rounded-full"
                      style={{ width: `${getProgress(checklist.items)}%` }} // Ancho dinámico según progreso
                    />
                  </div>
                </div>
              </CardHeader>

              {/* Contenido expandido con lista de ítems */}
              {expandedChecklist === checklist.id && (
                <CardContent className="border-t">
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
        </div>
      )}
    </div>
  )
}

// Componente hijo para gestionar ítems individuales de un checklist
function ChecklistItems({ items, onToggle, onAdd, onDelete, onEdit }) {
  const [newItemText, setNewItemText] = useState('') // Estado local para input de nuevo ítem

  const handleAdd = () => {
    if (newItemText.trim()) { // Valida que no esté vacío
      onAdd(newItemText) // Notifica al padre para agregar ítem
      setNewItemText('') // Limpia el input
    }
  }

  return (
    <div className="space-y-3">
      {/* Sección para agregar nuevo ítem */}
      <div className="flex gap-2">
        <Input
          type="text"
          placeholder="Agregar nuevo ítem..."
          value={newItemText}
          onChange={(e) => setNewItemText(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleAdd()} // Permite agregar con Enter
          aria-label="Nuevo ítem de checklist"
        />
        <Button
          onClick={handleAdd}
          disabled={!newItemText.trim()} // Deshabilita si está vacío
          size="icon"
          variant="default"
          aria-label="Agregar ítem"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
        </Button>
      </div>

      {/* Lista de ítems existentes */}
      {items.length === 0 ? (
        <EmptyState
          icon={
            <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          }
          title="No hay ítems"
          description="Agrega ítems a este checklist para comenzar"
          className="py-8"
        />
      ) : (
        <ul className="space-y-2" role="list">
          {items.map((item) => (
            <li key={item.id}>
              <div
                className={`
                  group flex items-start gap-3 p-3 rounded-lg border
                  transition-all duration-200
                  ${item.checked
                    ? 'bg-muted/50 border-muted-foreground/20' // Estilos para ítem completado
                    : 'bg-card border-border hover:border-primary/50 hover:shadow-sm' // Estilos para ítem pendiente
                  }
                `}
              >
                {/* Checkbox personalizado */}
                <button
                  onClick={() => onToggle(item.id)} // Alterna estado completado
                  className={`
                    flex-shrink-0 w-5 h-5 mt-0.5 rounded border-2 transition-all duration-200
                    ${item.checked
                      ? 'border-primary bg-primary text-primary-foreground' // Checkbox marcado
                      : 'border-input bg-background hover:border-primary' // Checkbox sin marcar
                    }
                    focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2
                  `}
                  aria-label={item.checked ? 'Marcar como incompleto' : 'Marcar como completo'}
                >
                  {item.checked && (
                    <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </button>

                {/* Contenido del ítem con texto tachado si está completado */}
                <div className="flex-1 min-w-0">
                  <p
                    className={`
                      text-sm transition-all duration-200
                      ${item.checked
                        ? 'text-muted-foreground line-through' // Estilo para completados
                        : 'text-foreground' // Estilo para pendientes
                      }
                    `}
                  >
                    {item.description}
                  </p>
                </div>

                {/* Botón de eliminar (visible solo al hover del grupo) */}
                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                  <button
                    onClick={() => onDelete(item.id)} // Elimina ítem
                    className="p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded transition-all duration-200"
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
}
