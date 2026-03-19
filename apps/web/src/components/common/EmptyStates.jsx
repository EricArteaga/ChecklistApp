import React from 'react'
import { Button, Card, CardContent } from '@checklist/ui'

/**
 * ═══════════════════════════════════════════════════════════════════
 * EMPTY STATES - Contextual empty state components
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Guide users with contextual empty states
 * • Reduce first-time user friction
 * • Provide clear CTAs for next actions
 * • Show brand personality
 *
 * @example
 * <NoTasksEmptyState onCreateTask={() => inputRef.current?.focus()} />
 * <NoTasksFilteredState filterName="Work" onClearFilter={() => setFilter('')} />
 */

/**
 * Empty state for first-time users (no tasks at all)
 */
export const NoTasksEmptyState = ({ onCreateTask }) => {
  return (
    <Card className="border-dashed border-2">
      <CardContent className="pt-12 pb-12">
        <div className="text-center space-y-6 max-w-md mx-auto">
          {/* Icon */}
          <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-success/10 mb-4">
            <svg
              className="w-10 h-10 text-success"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>

          {/* Title */}
          <h3 className="text-2xl font-bold text-foreground">
            ¡Empieza tu journey de productividad!
          </h3>

          {/* Description with benefits */}
          <div className="space-y-3 text-left bg-muted/50 rounded-xl p-6">
            <p className="text-sm font-medium text-foreground">
              Las tareas te ayudarán a:
            </p>
            <ul className="text-sm space-y-2 text-muted-foreground">
              <li className="flex items-start gap-2">
                <svg className="w-4 h-4 text-success mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                <span>Desglosar tareas complejas en pasos simples</span>
              </li>
              <li className="flex items-start gap-2">
                <svg className="w-4 h-4 text-success mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                <span>Mantener el enfoque en lo importante</span>
              </li>
              <li className="flex items-start gap-2">
                <svg className="w-4 h-4 text-success mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                <span>Celebrar pequeños logros con progreso visual</span>
              </li>
              <li className="flex items-start gap-2">
                <svg className="w-4 h-4 text-success mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                <span>Construir hábitos consistentes</span>
              </li>
            </ul>
          </div>

          {/* CTA Button */}
          <Button
            onClick={onCreateTask}
            className="btn-primary-gradient shadow-soft"
            size="lg"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Crear mi primera tarea
          </Button>

          {/* Helpful tip */}
          <p className="text-xs text-muted-foreground">
            Tip: Puedes crear tareas para cualquier cosa que quieras organizar
          </p>
        </div>
      </CardContent>
    </Card>
  )
}

/**
 * Empty state when filter has no results
 */
export const NoTasksFilteredState = ({ filterName, onClearFilter }) => {
  return (
    <Card className="border-dashed border-2">
      <CardContent className="pt-12 pb-12">
        <div className="text-center space-y-4 max-w-md mx-auto">
          {/* Icon */}
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-warning/10 mb-4">
            <svg
              className="w-8 h-8 text-warning"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
              />
            </svg>
          </div>

          {/* Title */}
          <h3 className="text-xl font-bold text-foreground">
            No hay tareas en "{filterName}"
          </h3>

          {/* Description */}
          <p className="text-muted-foreground">
            Este filtro no muestra ninguna tarea. Prueba con otro filtro o crea una nueva tarea en esta categoría.
          </p>

          {/* CTA Buttons */}
          <div className="flex gap-3 justify-center">
            <Button
              variant="outline"
              onClick={onClearFilter}
            >
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
              Limpiar filtro
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

/**
 * Empty state for no items in a checklist
 */
export const NoItemsEmptyState = ({ onAddItem }) => {
  return (
    <div className="text-center space-y-3 py-8">
      {/* Icon */}
      <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-muted">
        <svg
          className="w-6 h-6 text-muted-foreground"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
      </div>

      {/* Title */}
      <h4 className="font-semibold text-foreground">
        Sin ítems
      </h4>

      {/* Description */}
      <p className="text-sm text-muted-foreground">
        Agrega tu primer ítem para comenzar
      </p>

      {/* Helpful hint */}
      <p className="text-xs text-muted-foreground">
        Tip: Los ítems son las tareas pequeñas que completan tu tarea
      </p>
    </div>
  )
}

/**
 * Empty state for no types/categories
 */
export const NoTypesEmptyState = ({ onCreateType }) => {
  return (
    <Card className="border-dashed border-2">
      <CardContent className="pt-12 pb-12">
        <div className="text-center space-y-4 max-w-md mx-auto">
          {/* Icon */}
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-info/10 mb-4">
            <svg
              className="w-8 h-8 text-info"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
              />
            </svg>
          </div>

          {/* Title */}
          <h3 className="text-xl font-bold text-foreground">
            Crea tu primer tipo
          </h3>

          {/* Description */}
          <p className="text-muted-foreground">
            Los tipos te ayudan a categorizar tus tareas. Por ejemplo: "Trabajo", "Personal", "Proyectos", etc.
          </p>

          {/* CTA Button */}
          <Button
            onClick={onCreateType}
            className="btn-primary-gradient shadow-soft"
          >
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Crear tipo
          </Button>

          {/* Helpful tip */}
          <p className="text-xs text-muted-foreground">
            Tip: Puedes asignar colores a cada tipo para identificación visual rápida
          </p>
        </div>
      </CardContent>
    </Card>
  )
}

/**
 * Generic empty state for other scenarios
 */
export const GenericEmptyState = ({ icon, title, description, action, tip }) => {
  return (
    <Card className="border-dashed border-2">
      <CardContent className="pt-12 pb-12">
        <div className="text-center space-y-4 max-w-md mx-auto">
          {icon && (
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-muted mb-4">
              {icon}
            </div>
          )}

          {title && (
            <h3 className="text-xl font-bold text-foreground">
              {title}
            </h3>
          )}

          {description && (
            <p className="text-muted-foreground">
              {description}
            </p>
          )}

          {action && (
            <div className="flex justify-center">
              {action}
            </div>
          )}

          {tip && (
            <p className="text-xs text-muted-foreground">
              💡 {tip}
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

export default NoTasksEmptyState
