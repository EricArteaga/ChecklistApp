/**
 * Servicio de Tipos
 * Maneja todas las operaciones CRUD de tipos
 */

import apiClient from './api/client'
import { API_ENDPOINTS } from './api/config'

/**
 * Obtiene todos los tipos
 * @returns {Promise} Lista de tipos
 */
export const getAllTypes = async () => {
  const response = await apiClient.get(API_ENDPOINTS.TYPES.LIST)
  return response.data
}

/**
 * Obtiene un tipo por ID
 * @param {number} id - ID del tipo
 * @returns {Promise} Datos del tipo
 */
export const getTypeById = async (id) => {
  const response = await apiClient.get(API_ENDPOINTS.TYPES.BY_ID(id))
  return response.data
}

/**
 * Obtiene tipos de un usuario
 * @param {number} userId - ID del usuario
 * @returns {Promise} Lista de tipos del usuario
 */
export const getTypesByUser = async (userId) => {
  const response = await apiClient.get(API_ENDPOINTS.TYPES.BY_USER(userId))
  return response.data
}

/**
 * Crea un nuevo tipo
 * @param {Object} typeData - Datos del tipo
 * @param {string} typeData.nombre - Nombre del tipo (requerido)
 * @param {string} typeData.descripcion - Descripción del tipo
 * @param {string} typeData.color - Color en formato hex (ej: #FF5733)
 * @param {number} typeData.idUsuario - ID del usuario (requerido)
 * @returns {Promise} Tipo creado
 */
export const createType = async (typeData) => {
  const response = await apiClient.post(API_ENDPOINTS.TYPES.LIST, typeData)
  return response.data
}

/**
 * Actualiza un tipo
 * @param {number} id - ID del tipo
 * @param {Object} typeData - Datos a actualizar
 * @param {string} typeData.nombre - Nombre del tipo
 * @param {string} typeData.descripcion - Descripción del tipo
 * @param {string} typeData.color - Color en formato hex (ej: #FF5733)
 * @returns {Promise} Tipo actualizado
 */
export const updateType = async (id, typeData) => {
  const response = await apiClient.patch(API_ENDPOINTS.TYPES.BY_ID(id), typeData)
  return response.data
}

/**
 * Elimina un tipo
 * @param {number} id - ID del tipo
 * @returns {Promise} Respuesta del servidor
 */
export const deleteType = async (id) => {
  const response = await apiClient.delete(API_ENDPOINTS.TYPES.BY_ID(id))
  return response.data
}

export default {
  getAllTypes,
  getTypeById,
  getTypesByUser,
  createType,
  updateType,
  deleteType
}
