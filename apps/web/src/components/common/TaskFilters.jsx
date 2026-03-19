import React, { useState, useEffect, useMemo } from 'react'
import { Card, CardContent, Button, Input } from '@checklist/ui'

/**
 * ═══════════════════════════════════════════════════════════════════
 * TASK FILTERS - Advanced filtering component for tasks
 * ═══════════════════════════════════════════════════════════════════
 *
 * Features:
 * • Date filters: Today, This Week, This Month, Custom
 * • Status filters: All, Completed, Pending
 * • Text search with debounce (300ms)
 * • Visible filter chips
 * • Clear all filters button
 * • Result counter
 *
 * @param {Object} props
 * @param {Array} props.tasks - Array of tasks to filter
 * @param {Function} props.onFilteredChange - Callback with filtered tasks
 */
export default function TaskFilters({ tasks, onFilteredChange }) {
  // Filter states
  const [searchText, setSearchText] = useState('')
  const [statusFilter, setStatusFilter] = useState('all') // 'all', 'completed', 'pending'
  const [dateFilter, setDateFilter] = useState('all') // 'all', 'today', 'week', 'month', 'custom'
  const [customDateRange, setCustomDateRange] = useState({ start: '', end: '' })

  // Debounced search text
  const [debouncedSearch, setDebouncedSearch] = useState('')
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchText)
    }, 300) // 300ms debounce

    return () => clearTimeout(timer)
  }, [searchText])

  // Filter tasks based on all active filters
  const filteredTasks = useMemo(() => {
    let result = [...tasks]

    // 1. Text search filter
    if (debouncedSearch.trim()) {
      const searchLower = debouncedSearch.toLowerCase()
      result = result.filter(task => {
        const title = (task.nombre || task.title || '').toLowerCase()
        const description = (task.descripcion || task.description || '').toLowerCase()
        return title.includes(searchLower) || description.includes(searchLower)
      })
    }

    // 2. Status filter
    if (statusFilter !== 'all') {
      result = result.filter(task => {
        const isCompleted = task.completada || task.checked || false
        return statusFilter === 'completed' ? isCompleted : !isCompleted
      })
    }

    // 3. Date filter
    if (dateFilter !== 'all') {
      const today = new Date()
      today.setHours(0, 0, 0, 0)

      result = result.filter(task => {
        const taskDate = new Date(task.createdAt || task.fechaCreacion || today)

        switch (dateFilter) {
          case 'today':
            return taskDate.toDateString() === today.toDateString()

          case 'week': {
            const weekAgo = new Date(today)
            weekAgo.setDate(weekAgo.getDate() - 7)
            return taskDate >= weekAgo && taskDate <= today
          }

          case 'month': {
            const monthAgo = new Date(today)
            monthAgo.setMonth(monthAgo.getMonth() - 1)
            return taskDate >= monthAgo && taskDate <= today
          }

          case 'custom':
            if (customDateRange.start && customDateRange.end) {
              const startDate = new Date(customDateRange.start)
              const endDate = new Date(customDateRange.end)
              endDate.setHours(23, 59, 59, 999) // End of day
              return taskDate >= startDate && taskDate <= endDate
            }
            return true

          default:
            return true
        }
      })
    }

    return result
  }, [tasks, debouncedSearch, statusFilter, dateFilter, customDateRange])

  // Notify parent component of filtered results
  useEffect(() => {
    onFilteredChange(filteredTasks)
  }, [filteredTasks, onFilteredChange])

  // Check if any filters are active
  const hasActiveFilters = useMemo(() => {
    return searchText.trim() !== '' ||
           statusFilter !== 'all' ||
           dateFilter !== 'all'
  }, [searchText, statusFilter, dateFilter])

  // Clear all filters
  const clearAllFilters = () => {
    setSearchText('')
    setStatusFilter('all')
    setDateFilter('all')
    setCustomDateRange({ start: '', end: '' })
  }

  // Get active filter count
  const activeFilterCount = useMemo(() => {
    let count = 0
    if (searchText.trim()) count++
    if (statusFilter !== 'all') count++
    if (dateFilter !== 'all') count++
    return count
  }, [searchText, statusFilter, dateFilter])

  return (
    <Card className="card-elevated">
      <CardContent className="pt-6">
        <div className="space-y-4">
          {/* Header with title and clear button */}
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-foreground">Filtros Avanzados</h3>
            {hasActiveFilters && (
              <Button
                variant="ghost"
                size="sm"
                onClick={clearAllFilters}
                className="text-muted-foreground hover:text-foreground"
              >
                Limpiar todos
              </Button>
            )}
          </div>

          {/* Search input */}
          <div>
            <label htmlFor="search-input" className="sr-only">
              Buscar tareas
            </label>
            <Input
              id="search-input"
              type="text"
              placeholder="Buscar por título o descripción..."
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              className="input-enhanced"
              aria-label="Buscar tareas"
            />
          </div>

          {/* Status filters */}
          <div>
            <span className="text-sm font-medium text-muted-foreground mb-2 block">
              Estado
            </span>
            <div className="flex flex-wrap gap-2">
              <Button
                size="sm"
                variant={statusFilter === 'all' ? 'default' : 'outline'}
                onClick={() => setStatusFilter('all')}
                className={statusFilter === 'all' ? 'btn-primary-gradient' : ''}
              >
                Todas
              </Button>
              <Button
                size="sm"
                variant={statusFilter === 'completed' ? 'default' : 'outline'}
                onClick={() => setStatusFilter('completed')}
                className={statusFilter === 'completed' ? 'btn-primary-gradient' : ''}
              >
                Completadas
              </Button>
              <Button
                size="sm"
                variant={statusFilter === 'pending' ? 'default' : 'outline'}
                onClick={() => setStatusFilter('pending')}
                className={statusFilter === 'pending' ? 'btn-primary-gradient' : ''}
              >
                Pendientes
              </Button>
            </div>
          </div>

          {/* Date filters */}
          <div>
            <span className="text-sm font-medium text-muted-foreground mb-2 block">
              Fecha
            </span>
            <div className="flex flex-wrap gap-2">
              <Button
                size="sm"
                variant={dateFilter === 'all' ? 'default' : 'outline'}
                onClick={() => setDateFilter('all')}
                className={dateFilter === 'all' ? 'btn-primary-gradient' : ''}
              >
                Todas
              </Button>
              <Button
                size="sm"
                variant={dateFilter === 'today' ? 'default' : 'outline'}
                onClick={() => setDateFilter('today')}
                className={dateFilter === 'today' ? 'btn-primary-gradient' : ''}
              >
                Hoy
              </Button>
              <Button
                size="sm"
                variant={dateFilter === 'week' ? 'default' : 'outline'}
                onClick={() => setDateFilter('week')}
                className={dateFilter === 'week' ? 'btn-primary-gradient' : ''}
              >
                Esta semana
              </Button>
              <Button
                size="sm"
                variant={dateFilter === 'month' ? 'default' : 'outline'}
                onClick={() => setDateFilter('month')}
                className={dateFilter === 'month' ? 'btn-primary-gradient' : ''}
              >
                Este mes
              </Button>
            </div>
          </div>

          {/* Custom date range (shown only when 'custom' is selected) */}
          {dateFilter === 'custom' && (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label htmlFor="start-date" className="sr-only">
                  Fecha inicio
                </label>
                <Input
                  id="start-date"
                  type="date"
                  value={customDateRange.start}
                  onChange={(e) => setCustomDateRange(prev => ({ ...prev, start: e.target.value }))}
                  className="input-enhanced"
                />
              </div>
              <div>
                <label htmlFor="end-date" className="sr-only">
                  Fecha fin
                </label>
                <Input
                  id="end-date"
                  type="date"
                  value={customDateRange.end}
                  onChange={(e) => setCustomDateRange(prev => ({ ...prev, end: e.target.value }))}
                  className="input-enhanced"
                />
              </div>
            </div>
          )}

          {/* Active filter chips */}
          {hasActiveFilters && (
            <div className="flex flex-wrap items-center gap-2 pt-2 border-t">
              <span className="text-xs text-muted-foreground">Filtros activos:</span>
              {searchText && (
                <span className="inline-flex items-center gap-1 px-2 py-1 text-xs rounded-md bg-primary/10 text-primary">
                  🔍 "{searchText.substring(0, 20)}{searchText.length > 20 ? '...' : ''}"
                </span>
              )}
              {statusFilter !== 'all' && (
                <span className="inline-flex items-center gap-1 px-2 py-1 text-xs rounded-md bg-info/10 text-info">
                  {statusFilter === 'completed' ? '✓ Completadas' : '○ Pendientes'}
                </span>
              )}
              {dateFilter !== 'all' && (
                <span className="inline-flex items-center gap-1 px-2 py-1 text-xs rounded-md bg-warning/10 text-warning">
                  📅 {dateFilter === 'today' ? 'Hoy' : dateFilter === 'week' ? 'Esta semana' : dateFilter === 'month' ? 'Este mes' : 'Personalizado'}
                </span>
              )}
            </div>
          )}

          {/* Results counter */}
          <div className="text-sm text-muted-foreground">
            Mostrando <span className="font-semibold text-foreground">{filteredTasks.length}</span> de <span className="font-semibold text-foreground">{tasks.length}</span> tareas
            {activeFilterCount > 0 && (
              <span className="ml-2">
                ({activeFilterCount} {activeFilterCount === 1 ? 'filtro activo' : 'filtros activos'})
              </span>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
