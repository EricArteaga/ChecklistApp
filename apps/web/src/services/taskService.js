/**
 * Servicio de Tareas
 * Maneja todas las operaciones CRUD de tareas
 */

import apiClient from './api/client'
import { API_ENDPOINTS } from './api/config'

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
  const response = await apiClient.patch(API_ENDPOINTS.TASKS.BY_ID(id), taskData)
  return response.data
}

/**
 * Elimina una tarea
 * @param {number} id - ID de la tarea
 * @returns {Promise} Respuesta del servidor
 */
export const deleteTask = async (id) => {
  const response = await apiClient.delete(API_ENDPOINTS.TASKS.BY_ID(id))
  return response.data
}

export default {
  getAllTasks,
  getTaskById,
  getTasksByUser,
  getPendingTasks,
  getCompletedTasks,
  createTask,
  updateTask,
  deleteTask
}
