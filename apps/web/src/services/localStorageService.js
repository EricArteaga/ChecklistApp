/**
 * LocalStorage Service para tareas anónimas
 * Maneja el almacenamiento local de tareas cuando no hay usuario autenticado
 */

const STORAGE_KEY = 'checklistapp_anonymous_tasks'
const SYNCED_KEY = 'checklistapp_synced_tasks'

/**
 * Guarda tareas en localStorage
 * @param {Array} tasks - Array de tareas a guardar
 */
export const saveTasks = (tasks) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks))
  } catch (error) {
    console.error('Error saving tasks to localStorage:', error)
    // Si el almacenamiento está lleno, intentar guardar solo las tareas más recientes
    if (error.name === 'QuotaExceededError') {
      console.warn('LocalStorage quota exceeded, keeping only recent tasks')
      const recentTasks = tasks.slice(-50) // Mantener solo las últimas 50
      localStorage.setItem(STORAGE_KEY, JSON.stringify(recentTasks))
    }
  }
}

/**
 * Obtiene tareas del localStorage
 * @returns {Array} Array de tareas
 */
export const getTasks = () => {
  try {
    const tasks = localStorage.getItem(STORAGE_KEY)
    return tasks ? JSON.parse(tasks) : []
  } catch (error) {
    console.error('Error reading tasks from localStorage:', error)
    return []
  }
}

/**
 * Agrega una nueva tarea al localStorage
 * @param {Object} task - Tarea a agregar
 */
export const addTask = (task) => {
  const tasks = getTasks()
  const newTask = {
    ...task,
    id: task.id || `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`, // ID único con timestamp + random
    createdAt: task.createdAt || new Date().toISOString(),
    subitems: task.subitems || [] // ✅ AGREGAR: Inicializar array vacío de subitems
  }

  // Validar que el ID sea único antes de guardar
  if (tasks.some(t => t.id === newTask.id)) {
    console.error(`ID duplicado detectado: ${newTask.id}. Regenerando...`)
    // Reintentar con un nuevo ID
    return addTask({
      ...task,
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    })
  }

  tasks.push(newTask)
  saveTasks(tasks)
  return newTask
}

/**
 * Actualiza una tarea en localStorage
 * @param {number} taskId - ID de la tarea
 * @param {Object} updates - Campos a actualizar
 */
export const updateTask = (taskId, updates) => {
  const tasks = getTasks()
  const index = tasks.findIndex(t => t.id === taskId)
  if (index !== -1) {
    tasks[index] = { ...tasks[index], ...updates }
    saveTasks(tasks)
    return tasks[index]
  }
  return null
}

/**
 * Elimina una tarea del localStorage
 * @param {number} taskId - ID de la tarea
 */
export const deleteTask = (taskId) => {
  const tasks = getTasks()
  const filtered = tasks.filter(t => t.id !== taskId)
  saveTasks(filtered)
}

/**
 * Limpia todas las tareas del localStorage
 */
export const clearTasks = () => {
  localStorage.removeItem(STORAGE_KEY)
}

/**
 * Marca tareas como sincronizadas con el backend
 * @param {Array} taskIds - Array de IDs de tareas sincronizadas
 */
export const markAsSynced = (taskIds) => {
  try {
    localStorage.setItem(SYNCED_KEY, JSON.stringify(taskIds))
  } catch (error) {
    console.error('Error marking tasks as synced:', error)
  }
}

/**
 * Obtiene IDs de tareas sincronizadas
 * @returns {Array} Array de IDs
 */
export const getSyncedTaskIds = () => {
  try {
    const synced = localStorage.getItem(SYNCED_KEY)
    return synced ? JSON.parse(synced) : []
  } catch (error) {
    console.error('Error reading synced task IDs:', error)
    return []
  }
}

/**
 * Limpia marcas de sincronización
 */
export const clearSyncedMarks = () => {
  localStorage.removeItem(SYNCED_KEY)
}

/**
 * Agrega un subitem a una tarea
 * @param {string} taskId - ID de la tarea
 * @param {Object} subitem - Subitem a agregar { description, checked }
 * @returns {Object} Subitem agregado con ID generado
 */
export const addSubitem = (taskId, subitem) => {
  const tasks = getTasks()
  const index = tasks.findIndex(t => t.id === taskId)
  if (index === -1) {
    console.error(`Tarea ${taskId} no encontrada`)
    return null
  }

  const newSubitem = {
    id: subitem.id || `sub-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    description: subitem.description?.trim() || '',
    checked: subitem.checked || false
  }

  tasks[index].subitems = [...(tasks[index].subitems || []), newSubitem]
  saveTasks(tasks)
  return newSubitem
}

/**
 * Actualiza un subitem de una tarea
 * @param {string} taskId - ID de la tarea
 * @param {string} subitemId - ID del subitem
 * @param {Object} updates - Campos a actualizar { checked, description }
 * @returns {Object|null} Subitem actualizado o null si no se encontró
 */
export const updateSubitem = (taskId, subitemId, updates) => {
  const tasks = getTasks()
  const taskIndex = tasks.findIndex(t => t.id === taskId)
  if (taskIndex === -1) return null

  const subitemIndex = tasks[taskIndex].subitems?.findIndex(s => s.id === subitemId)
  if (subitemIndex === -1) return null

  tasks[taskIndex].subitems[subitemIndex] = {
    ...tasks[taskIndex].subitems[subitemIndex],
    ...updates
  }
  saveTasks(tasks)
  return tasks[taskIndex].subitems[subitemIndex]
}

/**
 * Elimina un subitem de una tarea
 * @param {string} taskId - ID de la tarea
 * @param {string} subitemId - ID del subitem
 * @returns {boolean} true si se eliminó, false si no se encontró
 */
export const deleteSubitem = (taskId, subitemId) => {
  const tasks = getTasks()
  const taskIndex = tasks.findIndex(t => t.id === taskId)
  if (taskIndex === -1) return false

  const originalLength = tasks[taskIndex].subitems?.length || 0
  tasks[taskIndex].subitems = (tasks[taskIndex].subitems || []).filter(s => s.id !== subitemId)

  if (tasks[taskIndex].subitems.length < originalLength) {
    saveTasks(tasks)
    return true
  }
  return false
}

/**
 * Migra tareas antiguas al nuevo formato con subitems
 * Se ejecuta automáticamente al iniciar la app
 * @returns {Array} Tareas migradas
 */
export const migrateTaskFormat = () => {
  const tasks = getTasks()
  let needsMigration = false

  const migratedTasks = tasks.map(task => {
    if (!task.subitems) {
      needsMigration = true
      return {
        ...task,
        subitems: [] // Agregar array vacío
      }
    }
    return task
  })

  if (needsMigration) {
    saveTasks(migratedTasks)
    console.log('[localStorageService] Migradas', tasks.length, 'tareas al nuevo formato con subitems')
  }

  return migratedTasks
}

export default {
  saveTasks,
  getTasks,
  addTask,
  updateTask,
  deleteTask,
  clearTasks,
  markAsSynced,
  getSyncedTaskIds,
  clearSyncedMarks,
  addSubitem,
  updateSubitem,
  deleteSubitem,
  migrateTaskFormat
}
