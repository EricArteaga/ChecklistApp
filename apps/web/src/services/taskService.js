/**
 * Servicio de Tareas
 * Maneja todas las operaciones CRUD de tareas
 * Soporta modo anónimo con localStorage y modo autenticado con API
 */

import apiClient from './api/client'
import { API_ENDPOINTS } from './api/config'
import localStorageService from './localStorageService'
import authService from './authService'

/**
 * Obtiene todas las tareas
 * @returns {Promise} Lista de tareas
 */
export const getAllTasks = async () => {
  const response = await apiClient.get(API_ENDPOINTS.TASKS.LIST)
  return response.data
}

/**
 * Obtiene una tarea por ID
 * @param {number} id - ID de la tarea
 * @returns {Promise} Datos de la tarea
 */
export const getTaskById = async (id) => {
  const response = await apiClient.get(API_ENDPOINTS.TASKS.BY_ID(id))
  return response.data
}

/**
 * Obtiene tareas de un usuario
 * @param {number} userId - ID del usuario
 * @returns {Promise} Lista de tareas del usuario
 */
export const getTasksByUser = async (userId) => {
  // Si no hay usuario autenticado, usar localStorage
  if (!authService.isAuthenticated()) {
    return localStorageService.getTasks()
  }

  // Usuario autenticado: usar API
  const response = await apiClient.get(API_ENDPOINTS.TASKS.BY_USER(userId))
  return response.data
}

/**
 * Obtiene tareas pendientes de un usuario
 * @param {number} userId - ID del usuario
 * @returns {Promise} Lista de tareas pendientes
 */
export const getPendingTasks = async (userId) => {
  const response = await apiClient.get(API_ENDPOINTS.TASKS.PENDING(userId))
  return response.data
}

/**
 * Obtiene tareas completadas de un usuario
 * @param {number} userId - ID del usuario
 * @returns {Promise} Lista de tareas completadas
 */
export const getCompletedTasks = async (userId) => {
  const response = await apiClient.get(API_ENDPOINTS.TASKS.COMPLETED(userId))
  return response.data
}

/**
 * Crea una nueva tarea
 * @param {Object} taskData - Datos de la tarea
 * @returns {Promise} Tarea creada
 */
export const createTask = async (taskData) => {
  // Si no hay usuario autenticado, usar localStorage
  if (!authService.isAuthenticated()) {
    const anonymousTask = {
      ...taskData,
      idUsuario: null, // Tarea anónima
      createdAt: new Date().toISOString()
    }
    return localStorageService.addTask(anonymousTask)
  }

  // Usuario autenticado: usar API
  const response = await apiClient.post(API_ENDPOINTS.TASKS.LIST, taskData)
  return response.data
}

/**
 * Actualiza una tarea
 * @param {number} id - ID de la tarea
 * @param {Object} taskData - Datos a actualizar
 * @returns {Promise} Tarea actualizada
 */
export const updateTask = async (id, taskData) => {
  // Si no hay usuario autenticado, usar localStorage
  if (!authService.isAuthenticated()) {
    return localStorageService.updateTask(id, taskData)
  }

  // Usuario autenticado: usar API
  const response = await apiClient.patch(API_ENDPOINTS.TASKS.BY_ID(id), taskData)
  return response.data
}

/**
 * Elimina una tarea
 * @param {number} id - ID de la tarea
 * @returns {Promise} Respuesta del servidor
 */
export const deleteTask = async (id) => {
  // Si no hay usuario autenticado, usar localStorage
  if (!authService.isAuthenticated()) {
    return localStorageService.deleteTask(id)
  }

  // Usuario autenticado: usar API
  const response = await apiClient.delete(API_ENDPOINTS.TASKS.BY_ID(id))
  return response.data
}

/**
 * Sincroniza tareas locales con el backend cuando el usuario se autentica
 * @param {number} userId - ID del usuario autenticado
 * @returns {Promise<Object>} Objeto con { syncedTasks, failedTasks, syncedSubitems, failedSubitems }
 */
export const syncLocalTasks = async (userId) => {
  const localTasks = localStorageService.getTasks()

  if (localTasks.length === 0) {
    return { syncedTasks: [], failedTasks: [], syncedSubitems: 0, failedSubitems: 0 }
  }

  const syncedTasks = []
  const failedTasks = []
  let syncedSubitems = 0
  let failedSubitems = 0

  for (const task of localTasks) {
    try {
      // Crear tarea en el backend con el ID del usuario
      const taskData = {
        nombre: task.nombre || task.title || task.description,
        descripcion: task.descripcion || task.description,
        idUsuario: userId,
        idTipo: task.idTipo || task.tipo?.id || null,
        completada: task.completada || task.checked || false,
        fechaProgramacion: task.fechaProgramacion || null,
        fechaRealizacion: task.fechaRealizacion || null
      }

      const createdTaskResponse = await apiClient.post(API_ENDPOINTS.TASKS.LIST, taskData)
      const createdTask = createdTaskResponse.data
      syncedTasks.push(createdTask)

      // Sincronizar subitems si existen
      if (task.subitems && Array.isArray(task.subitems) && task.subitems.length > 0) {
        for (const subitem of task.subitems) {
          try {
            const subitemData = {
              description: subitem.description || subitem.text || '',
              checked: subitem.checked || subitem.completed || false
            }

            await apiClient.post(
              API_ENDPOINTS.TASKS.SUBITEMS.LIST(createdTask.id),
              subitemData
            )
            syncedSubitems++
          } catch (subitemError) {
            console.error(`Error syncing subitem for task ${task.id}:`, subitemError)
            failedSubitems++
          }
        }
      }
    } catch (error) {
      console.error(`Error syncing task ${task.id}:`, error)
      failedTasks.push(task)
    }
  }

  // Solo limpiar localStorage si TODAS las tareas se sincronizaron exitosamente
  if (failedTasks.length === 0) {
    localStorageService.clearTasks()
    localStorageService.clearSyncedMarks()
    console.log(`Todas las tareas (${syncedTasks.length}) y subitems (${syncedSubitems}) se sincronizaron correctamente`)
  } else {
    console.warn(`${failedTasks.length} tareas fallaron al sincronizar. No se limpió localStorage.`)
  }

  return { syncedTasks, failedTasks, syncedSubitems, failedSubitems }
}

/**
 * Agrega un subitem a una tarea
 * @param {string} taskId - ID de la tarea
 * @param {Object} subitem - { description, checked }
 * @returns {Promise<Object>} Subitem agregado
 */
export const addSubitem = async (taskId, subitem) => {
  // En modo anónimo: usar localStorage
  if (!authService.isAuthenticated()) {
    return localStorageService.addSubitem(taskId, subitem)
  }
  // En modo autenticado: usar API
  const response = await apiClient.post(
    API_ENDPOINTS.TASKS.SUBITEMS.LIST(taskId),
    subitem
  )
  return response.data
}

/**
 * Actualiza un subitem
 * @param {string} taskId - ID de la tarea
 * @param {string} subitemId - ID del subitem
 * @param {Object} updates - { checked, description }
 * @returns {Promise<Object>} Subitem actualizado
 */
export const updateSubitem = async (taskId, subitemId, updates) => {
  if (!authService.isAuthenticated()) {
    return localStorageService.updateSubitem(taskId, subitemId, updates)
  }
  // En modo autenticado: usar API
  const response = await apiClient.patch(
    API_ENDPOINTS.TASKS.SUBITEMS.BY_ID(taskId, subitemId),
    updates
  )
  return response.data
}

/**
 * Elimina un subitem
 * @param {string} taskId - ID de la tarea
 * @param {string} subitemId - ID del subitem
 * @returns {Promise<boolean>} true si se eliminó
 */
export const deleteSubitem = async (taskId, subitemId) => {
  if (!authService.isAuthenticated()) {
    return localStorageService.deleteSubitem(taskId, subitemId)
  }
  // En modo autenticado: usar API
  await apiClient.delete(API_ENDPOINTS.TASKS.SUBITEMS.BY_ID(taskId, subitemId))
  return true
}

export default {
  getAllTasks,
  getTaskById,
  getTasksByUser,
  getPendingTasks,
  getCompletedTasks,
  createTask,
  updateTask,
  deleteTask,
  syncLocalTasks,
  addSubitem,
  updateSubitem,
  deleteSubitem
}
