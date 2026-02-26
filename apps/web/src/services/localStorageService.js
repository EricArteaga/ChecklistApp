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
    id: task.id || Date.now(), // Generar ID si no tiene
    createdAt: task.createdAt || new Date().toISOString()
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

export default {
  saveTasks,
  getTasks,
  addTask,
  updateTask,
  deleteTask,
  clearTasks,
  markAsSynced,
  getSyncedTaskIds,
  clearSyncedMarks
}
