import React, { useState, useEffect } from 'react'
import { Button, Input, Card, CardHeader, CardTitle, CardDescription, CardContent, EmptyState, Badge } from '@checklist/ui'
import typeService from '../../services/typeService'
import authService from '../../services/authService'

// Colores predefinidos para elegir
const PREDEFINED_COLORS = [
  { name: 'Rojo', value: '#EF4444' },
  { name: 'Naranja', value: '#F97316' },
  { name: 'Amarillo', value: '#EAB308' },
  { name: 'Verde', value: '#22C55E' },
  { name: 'Cyan', value: '#06B6D4' },
  { name: 'Azul', value: '#3B82F6' },
  { name: 'Violeta', value: '#8B5CF6' },
  { name: 'Rosa', value: '#EC4899' },
  { name: 'Gris', value: '#6B7280' },
]

export default function TypeManagement() {
  // Estados principales del componente
  const [types, setTypes] = useState([])
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Estados para formulario de creación
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [newTypeName, setNewTypeName] = useState('')
  const [newTypeDescription, setNewTypeDescription] = useState('')
  const [newTypeColor, setNewTypeColor] = useState(PREDEFINED_COLORS[4].value) // Cyan por defecto
  const [isCreating, setIsCreating] = useState(false)

  // Estados para formulario de edición
  const [editingType, setEditingType] = useState(null)
  const [editTypeName, setEditTypeName] = useState('')
  const [editTypeDescription, setEditTypeDescription] = useState('')
  const [editTypeColor, setEditTypeColor] = useState('')
  const [isUpdating, setIsUpdating] = useState(false)

  // Efecto para cargar datos iniciales
  useEffect(() => {
    const loadData = async () => {
      setLoading(true)
      try {
        // 1. Obtener usuario autenticado
        const user = await authService.getMe()
        setCurrentUser(user)

        // 2. Obtener tipos del usuario
        const typesData = await typeService.getTypesByUser(user.id)
        setTypes(typesData)
      } catch (err) {
        setError(err.message || 'Error al cargar los tipos. Por favor, intenta nuevamente.')
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [])

  // Función para crear un nuevo tipo
  const handleCreateType = async () => {
    if (!newTypeName.trim() || !currentUser) return

    setIsCreating(true)
    try {
      const newType = await typeService.createType({
        nombre: newTypeName.trim(),
        descripcion: newTypeDescription.trim(),
        color: newTypeColor,
        idUsuario: currentUser.id
      })

      // Actualizar estado local
      setTypes([newType, ...types])

      // Resetear formulario
      setNewTypeName('')
      setNewTypeDescription('')
      setNewTypeColor(PREDEFINED_COLORS[4].value)
      setShowCreateForm(false)
    } catch (err) {
      setError(err.message || 'Error al crear el tipo. Por favor, intenta nuevamente.')
    } finally {
      setIsCreating(false)
    }
  }

  // Función para iniciar edición de un tipo
  const handleStartEdit = (type) => {
    setEditingType(type.id)
    setEditTypeName(type.nombre)
    setEditTypeDescription(type.descripcion || '')
    setEditTypeColor(type.color)
  }

  // Función para cancelar edición
  const handleCancelEdit = () => {
    setEditingType(null)
    setEditTypeName('')
    setEditTypeDescription('')
    setEditTypeColor('')
  }

  // Función para actualizar un tipo
  const handleUpdateType = async () => {
    if (!editTypeName.trim() || !editingType) return

    setIsUpdating(true)
    try {
      const updatedType = await typeService.updateType(editingType, {
        nombre: editTypeName.trim(),
        descripcion: editTypeDescription.trim(),
        color: editTypeColor
      })

      // Actualizar estado local
      setTypes(types.map(t =>
        t.id === editingType ? updatedType : t
      ))

      // Resetear formulario de edición
      setEditingType(null)
      setEditTypeName('')
      setEditTypeDescription('')
      setEditTypeColor('')
    } catch (err) {
      setError(err.message || 'Error al actualizar el tipo. Por favor, intenta nuevamente.')
    } finally {
      setIsUpdating(false)
    }
  }

  // Función para eliminar un tipo
  const handleDeleteType = async (typeId) => {
    if (!confirm('¿Estás seguro de que deseas eliminar este tipo? Esta acción no se puede deshacer.')) {
      return
    }

    try {
      await typeService.deleteType(typeId)

      // Actualizar estado local
      setTypes(types.filter(t => t.id !== typeId))

      // Si estábamos editando este tipo, cancelar edición
      if (editingType === typeId) {
        handleCancelEdit()
      }
    } catch (err) {
      setError(err.message || 'Error al eliminar el tipo. Por favor, intenta nuevamente.')
    }
  }

  // Validar formato de color hex
  const isValidHexColor = (color) => {
    return /^#[0-9A-F]{6}$/i.test(color)
  }

  // Estado de carga
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
          <p className="text-muted-foreground">Cargando tipos...</p>
        </div>
      </div>
    )
  }

  // Estado de error
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
                onClick={() => window.location.reload()}
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
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground">
            Gestión de Tipos
          </h2>
          <p className="text-muted-foreground mt-1">
            Crea y gestiona los tipos de tareas para organizar mejor tus checklists
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant="secondary" className="text-sm">
            {types.length} {types.length === 1 ? 'tipo' : 'tipos'}
          </Badge>
        </div>
      </div>

      {/* Formulario para crear nuevo tipo */}
      <Card className="border-dashed">
        <CardContent className="pt-6">
          {!showCreateForm ? (
            <Button
              onClick={() => setShowCreateForm(true)}
              className="w-full"
            >
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              Crear Nuevo Tipo
            </Button>
          ) : (
            <div className="space-y-4">
              <div>
                <label htmlFor="typeName" className="block text-sm font-medium mb-2">
                  Nombre del Tipo *
                </label>
                <Input
                  id="typeName"
                  type="text"
                  placeholder="Ej: Trabajo, Personal, Urgente..."
                  value={newTypeName}
                  onChange={(e) => setNewTypeName(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleCreateType()}
                  aria-label="Nombre del tipo"
                />
              </div>

              <div>
                <label htmlFor="typeDescription" className="block text-sm font-medium mb-2">
                  Descripción
                </label>
                <Input
                  id="typeDescription"
                  type="text"
                  placeholder="Descripción opcional del tipo"
                  value={newTypeDescription}
                  onChange={(e) => setNewTypeDescription(e.target.value)}
                  aria-label="Descripción del tipo"
                />
              </div>

              <div>
                <label htmlFor="typeColor" className="block text-sm font-medium mb-2">
                  Color *
                </label>
                <div className="flex flex-wrap gap-2 mb-2">
                  {PREDEFINED_COLORS.map((color) => (
                    <button
                      key={color.value}
                      onClick={() => setNewTypeColor(color.value)}
                      className={`
                        w-8 h-8 rounded-full border-2 transition-all
                        ${newTypeColor === color.value
                          ? 'border-primary scale-110 shadow-lg'
                          : 'border-border hover:scale-105'
                        }
                      `}
                      style={{ backgroundColor: color.value }}
                      title={color.name}
                      aria-label={`Seleccionar color ${color.name}`}
                    />
                  ))}
                </div>
                <div className="flex items-center gap-2">
                  <Input
                    id="typeColor"
                    type="color"
                    value={newTypeColor}
                    onChange={(e) => setNewTypeColor(e.target.value)}
                    className="w-20 h-10"
                    aria-label="Selector de color personalizado"
                  />
                  <Input
                    type="text"
                    value={newTypeColor}
                    onChange={(e) => setNewTypeColor(e.target.value)}
                    placeholder="#000000"
                    className="flex-1"
                    aria-label="Código de color hexadecimal"
                  />
                </div>
                {!isValidHexColor(newTypeColor) && (
                  <p className="text-sm text-destructive mt-1">
                    Formato inválido. Usa el formato #RRGGBB
                  </p>
                )}
              </div>

              <div className="flex gap-2">
                <Button
                  onClick={handleCreateType}
                  disabled={!newTypeName.trim() || !isValidHexColor(newTypeColor) || isCreating}
                  loading={isCreating}
                  className="flex-1"
                >
                  {isCreating ? 'Creando...' : 'Crear Tipo'}
                </Button>
                <Button
                  variant="outline"
                  onClick={() => {
                    setShowCreateForm(false)
                    setNewTypeName('')
                    setNewTypeDescription('')
                    setNewTypeColor(PREDEFINED_COLORS[4].value)
                  }}
                  disabled={isCreating}
                >
                  Cancelar
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Lista de tipos */}
      {types.length === 0 ? (
        <EmptyState
          icon={
            <svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
          }
          title="No hay tipos creados"
          description="Crea tu primer tipo para empezar a categorizar tus tareas"
          action={
            <Button onClick={() => setShowCreateForm(true)}>
              Crear Tipo
            </Button>
          }
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {types.map((type) => (
            <Card
              key={type.id}
              className="transition-all duration-300 hover:shadow-lg animate-slide-in"
            >
              <CardHeader>
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <div
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: type.color }}
                        title={type.color}
                      />
                      <CardTitle className="truncate">{type.nombre}</CardTitle>
                    </div>
                    {type.descripcion && (
                      <CardDescription className="truncate">
                        {type.descripcion}
                      </CardDescription>
                    )}
                  </div>
                  <Badge
                    variant="outline"
                    className="shrink-0"
                    style={{ borderColor: type.color, color: type.color }}
                  >
                    Tipo
                  </Badge>
                </div>
              </CardHeader>

              <CardContent>
                {editingType === type.id ? (
                  // Formulario de edición
                  <div className="space-y-3">
                    <div>
                      <Input
                        type="text"
                        value={editTypeName}
                        onChange={(e) => setEditTypeName(e.target.value)}
                        placeholder="Nombre del tipo"
                        aria-label="Editar nombre del tipo"
                      />
                    </div>

                    <div>
                      <Input
                        type="text"
                        value={editTypeDescription}
                        onChange={(e) => setEditTypeDescription(e.target.value)}
                        placeholder="Descripción"
                        aria-label="Editar descripción del tipo"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium mb-2">Color</label>
                      <div className="flex flex-wrap gap-2 mb-2">
                        {PREDEFINED_COLORS.map((color) => (
                          <button
                            key={color.value}
                            onClick={() => setEditTypeColor(color.value)}
                            className={`
                              w-6 h-6 rounded-full border-2 transition-all
                              ${editTypeColor === color.value
                                ? 'border-primary scale-110'
                                : 'border-border hover:scale-105'
                              }
                            `}
                            style={{ backgroundColor: color.value }}
                            title={color.name}
                          />
                        ))}
                      </div>
                      <div className="flex items-center gap-2">
                        <Input
                          type="color"
                          value={editTypeColor}
                          onChange={(e) => setEditTypeColor(e.target.value)}
                          className="w-16 h-8"
                        />
                        <Input
                          type="text"
                          value={editTypeColor}
                          onChange={(e) => setEditTypeColor(e.target.value)}
                          placeholder="#000000"
                          className="flex-1"
                        />
                      </div>
                    </div>

                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        onClick={handleUpdateType}
                        disabled={!editTypeName.trim() || !isValidHexColor(editTypeColor) || isUpdating}
                        loading={isUpdating}
                        className="flex-1"
                      >
                        Guardar
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={handleCancelEdit}
                        disabled={isUpdating}
                      >
                        Cancelar
                      </Button>
                    </div>
                  </div>
                ) : (
                  // Vista normal
                  <div className="flex gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleStartEdit(type)}
                      className="flex-1"
                    >
                      <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                      Editar
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleDeleteType(type.id)}
                      className="text-destructive hover:text-destructive hover:bg-destructive/10"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
