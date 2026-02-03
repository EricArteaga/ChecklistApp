import React, { useState, useEffect } from 'react' // Importa hooks de React para gestión de estado y efectos
import { Button, Input, Card, CardHeader, CardTitle, CardDescription, CardContent, EmptyState, Badge } from '@checklist/ui' // Componentes UI reutilizables del paquete interno

export default function Checklist() {
  // Estados principales del componente
  const [checklists, setChecklists] = useState([]) // Almacena la lista de checklists
  const [loading, setLoading] = useState(true) // Controla estado de carga durante fetch de datos
  const [error, setError] = useState(null) // Almacena mensajes de error si ocurren
  const [newItemTitle, setNewItemTitle] = useState('') // Input para crear nuevo checklist
  const [isCreating, setIsCreating] = useState(false) // Controla estado de carga durante creación
  const [expandedChecklist, setExpandedChecklist] = useState(null) // ID del checklist expandido (null = ninguno)

  // Efecto para cargar datos iniciales de checklists
  useEffect(() => {
    const loadData = async () => {
      setLoading(true) // Activa indicador de carga
      try {
        // Simula llamada a API con delay de 800ms para demostración
        await new Promise(resolve => setTimeout(resolve, 800))

        // Datos mockeados para desarrollo - reemplazar con llamada real a API
        setChecklists([
          {
            id: 1,
            title: 'Tareas Diarias',
            description: 'Tareas rutinarias para completar cada día',
            items: [
              { id: 1, description: 'Revisar correos electrónicos', checked: true },
              { id: 2, description: 'Reunión de equipo a las 10:00 AM', checked: false },
              { id: 3, description: 'Actualizar documentación del proyecto', checked: false },
            ],
            createdAt: new Date().toISOString(),
          },
          {
            id: 2,
            title: 'Proyecto Web',
            description: 'Tareas relacionadas con el desarrollo web',
            items: [
              { id: 4, description: 'Configurar TailwindCSS v4', checked: true },
              { id: 5, description: 'Implementar componentes UI reutilizables', checked: true },
              { id: 6, description: 'Mejorar accesibilidad de la aplicación', checked: false },
              { id: 7, description: 'Agregar soporte de modo oscuro', checked: false },
            ],
            createdAt: new Date().toISOString(),
          },
        ])
      } catch (err) {
        setError('Error al cargar los checklists. Por favor, intenta nuevamente.')
      } finally {
        setLoading(false) // Desactiva indicador de carga independientemente del resultado
      }
    }

    loadData() // Ejecuta la carga de datos
  }, []) // Array vacío: solo se ejecuta al montar el componente

  // Función para crear un nuevo checklist
  const handleCreateChecklist = async () => {
    if (!newItemTitle.trim()) return // Evita crear checklists vacíos

    setIsCreating(true) // Activa indicador de carga
    try {
      // Simula llamada a API con delay
      await new Promise(resolve => setTimeout(resolve, 500))

      // Crea nuevo objeto checklist con timestamp actual
      const newChecklist = {
        id: Date.now(), // ID único basado en timestamp
        title: newItemTitle,
        description: 'Descripción del checklist',
        items: [], // Lista vacía inicial
        createdAt: new Date().toISOString(),
      }

      // Actualiza estado añadiendo nuevo checklist al inicio del array
      setChecklists([newChecklist, ...checklists])
      setNewItemTitle('') // Limpia el input
      setExpandedChecklist(newChecklist.id) // Expande automáticamente el nuevo checklist
    } catch (err) {
      setError('Error al crear el checklist. Por favor, intenta nuevamente.')
    } finally {
      setIsCreating(false)
    }
  }

  // Función para eliminar un checklist
  const handleDeleteChecklist = async (checklistId) => {
    try {
      // Simula llamada a API
      await new Promise(resolve => setTimeout(resolve, 300))

      // Filtra el checklist a eliminar y actualiza estado
      setChecklists(checklists.filter(c => c.id !== checklistId))
      // Si el checklist eliminado estaba expandido, colapsa la vista
      if (expandedChecklist === checklistId) {
        setExpandedChecklist(null)
      }
    } catch (err) {
      setError('Error al eliminar el checklist. Por favor, intenta nuevamente.')
    }
  }

  // Función para alternar estado completado/no completado de un ítem
  const handleToggleItem = async (checklistId, itemId) => {
    // Actualiza el estado local de forma inmutable
    setChecklists(checklists.map(checklist => {
      if (checklist.id === checklistId) {
        // Encuentra el checklist y modifica el estado del ítem específico
        const updatedItems = checklist.items.map(item =>
          item.id === itemId ? { ...item, checked: !item.checked } : item
        )
        return { ...checklist, items: updatedItems }
      }
      return checklist // Devuelve checklist sin cambios si no coincide
    }))
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
