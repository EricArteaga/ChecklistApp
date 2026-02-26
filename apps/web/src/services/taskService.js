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
 * @returns {Promise} Tareas sincronizadas
 */
export const syncLocalTasks = async (userId) => {
  const localTasks = localStorageService.getTasks()

  if (localTasks.length === 0) {
    return []
  }

  const syncedTasks = []

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

      const createdTask = await apiClient.post(API_ENDPOINTS.TASKS.LIST, taskData)
      syncedTasks.push(createdTask.data)
    } catch (error) {
      console.error(`Error syncing task ${task.id}:`, error)
    }
  }

  // Limpiar tareas locales después de sincronizar
  localStorageService.clearTasks()
  localStorageService.clearSyncedMarks()

  return syncedTasks
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
  syncLocalTasks
}
