# Ejemplos de Uso - Componentes UI

## Importación

```javascript
import {
  Button,
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
  Input,
  LoadingSpinner,
  EmptyState,
  Badge,
  ChecklistItem
} from '@checklistapp/ui'
```

## Button

```jsx
// Variantes
<Button variant="default">Botón Principal</Button>
<Button variant="destructive">Eliminar</Button>
<Button variant="outline">Borde</Button>
<Button variant="secondary">Secundario</Button>
<Button variant="ghost">Fantasma</Button>
<Button variant="link">Enlace</Button>

// Tamaños
<Button size="sm">Pequeño</Button>
<Button size="default">Normal</Button>
<Button size="lg">Grande</Button>
<Button size="icon">🔍</Button>

// Con estado de carga
<Button loading={true}>Guardando...</Button>

// Deshabilitado
<Button disabled={true}>Deshabilitado</Button>
```

## Card

```jsx
<Card>
  <CardHeader>
    <CardTitle>Título de la Tarjeta</CardTitle>
    <CardDescription>Descripción opcional</CardDescription>
  </CardHeader>
  <CardContent>
    <p>Contenido de la tarjeta</p>
  </CardContent>
  <CardFooter>
    <Button>Acción</Button>
  </CardFooter>
</Card>
```

## Input

```jsx
<Input
  type="text"
  placeholder="Escribe algo..."
  value={value}
  onChange={(e) => setValue(e.target.value)}
/>

// Con error
<Input
  type="email"
  error={hasError}
  placeholder="correo@ejemplo.com"
/>

// Deshabilitado
<Input disabled value="Solo lectura" />
```

## LoadingSpinner

```jsx
// Tamaños
<LoadingSpinner size="sm" />
<LoadingSpinner size="default" />
<LoadingSpinner size="lg" />

// Con texto
<LoadingSpinner text="Cargando datos..." />

// Pantalla completa
<LoadingSpinner fullScreen text="Procesando..." />
```

## EmptyState

```jsx
<EmptyState
  icon={<svg>...</svg>}
  title="No hay datos"
  description="No se encontraron resultados"
  action={<Button>Crear nuevo</Button>}
/>
```

## Badge

```jsx
<Badge variant="default">Default</Badge>
<Badge variant="secondary">Secundario</Badge>
<Badge variant="destructive">Error</Badge>
<Badge variant="outline">Outline</Badge>
<Badge variant="success">Completado</Badge>
<Badge variant="warning">Advertencia</Badge>
```

## ChecklistItem

```jsx
<ChecklistItem
  item={{
    id: 1,
    description: 'Tarea de ejemplo',
    checked: false
  }}
  onToggle={(id) => console.log('Toggle:', id)}
  onDelete={(id) => console.log('Delete:', id)}
  onEdit={(id, newDesc) => console.log('Edit:', id, newDesc)}
  readOnly={false}
/>
```

## Ejemplo Completo: Formulario

```jsx
import { useState } from 'react'
import { Card, CardHeader, CardTitle, CardContent, Input, Button } from '@checklistapp/ui'

function MyForm() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    // Simular API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    setLoading(false)
    alert(`¡Formulario enviado! ${name} - ${email}`)
  }

  return (
    <Card className="max-w-md mx-auto">
      <CardHeader>
        <CardTitle>Formulario de Contacto</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="name" className="block text-sm font-medium mb-2">
              Nombre
            </label>
            <Input
              id="name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Tu nombre"
              required
            />
          </div>

          <div>
            <label htmlFor="email" className="block text-sm font-medium mb-2">
              Email
            </label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="tu@ejemplo.com"
              required
            />
          </div>

          <Button type="submit" loading={loading} className="w-full">
            Enviar
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
```

## Ejemplo Completo: Lista de Tareas

```jsx
import { useState } from 'react'
import { Card, CardContent, Button, EmptyState, Badge } from '@checklistapp/ui'

function TaskList() {
  const [tasks, setTasks] = useState([
    { id: 1, title: 'Aprender React', completed: true },
    { id: 2, title: 'Construir una app', completed: false },
  ])

  const toggleTask = (id) => {
    setTasks(tasks.map(task =>
      task.id === id ? { ...task, completed: !task.completed } : task
    ))
  }

  const deleteTask = (id) => {
    setTasks(tasks.filter(task => task.id !== id))
  }

  const completedTasks = tasks.filter(t => t.completed).length

  return (
    <Card>
      <CardContent className="pt-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold">Mis Tareas</h3>
          <Badge variant="secondary">
            {completedTasks} de {tasks.length} completadas
          </Badge>
        </div>

        {tasks.length === 0 ? (
          <EmptyState
            title="No hay tareas"
            description="Crea tu primera tarea para comenzar"
          />
        ) : (
          <ul className="space-y-2">
            {tasks.map(task => (
              <li
                key={task.id}
                className="flex items-center justify-between p-3 border rounded-lg"
              >
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={task.completed}
                    onChange={() => toggleTask(task.id)}
                    className="w-4 h-4"
                  />
                  <span className={task.completed ? 'line-through text-muted-foreground' : ''}>
                    {task.title}
                  </span>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => deleteTask(task.id)}
                >
                  Eliminar
                </Button>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}
```

## Tips de Accesibilidad

Todos los componentes incluyen:
- ARIA labels apropiados
- Navegación por teclado
- Estados focus visibles
- Roles semánticos

```jsx
// Ejemplo con accesibilidad explícita
<button
  aria-label="Cerrar modal"
  aria-pressed={isPressed}
  role="button"
>
  ×
</button>
```

## Responsive Design

Los componentes son responsive por defecto usando TailwindCSS:

```jsx
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
  {/* Columnas adaptables */}
</div>

<div className="flex flex-col sm:flex-row gap-4">
  {/* Dirección flexible */}
</div>
```

## Dark Mode

Los componentes soportan dark mode automáticamente usando las variables CSS:

```css
/* En global.css */
@media (prefers-color-scheme: dark) {
  @theme {
    --background: 222.2 84% 4.9%;
    --foreground: 210 40% 98%;
    /* ... más variables */
  }
}
```

Los componentes se adaptan automáticamente sin cambios necesarios.
