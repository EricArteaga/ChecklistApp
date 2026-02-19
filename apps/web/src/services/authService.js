/**
 * Servicio de Autenticación
 * Maneja login, registro y gestión de tokens
 */

import apiClient from './api/client'
import { API_ENDPOINTS } from './api/config'

/**
 * Inicia sesión con email y contraseña
 * @param {string} email - Email del usuario
 * @param {string} password - Contraseña del usuario
 * @returns {Promise} Response con token y datos del usuario
 */
export const login = async (email, password) => {
  const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, {
    email,
    password
  })

  // Guardar token en localStorage
  if (response.data.token) {
    localStorage.setItem('token', response.data.token)
  }

  return response.data
}

/**
 * Registra un nuevo usuario
 * @param {string} nombre - Nombre del usuario
 * @param {string} email - Email del usuario
 * @param {string} password - Contraseña del usuario
 * @returns {Promise} Response con token y datos del usuario
 */
export const register = async (nombre, email, password) => {
  const response = await apiClient.post(API_ENDPOINTS.AUTH.REGISTER, {
    nombre,
    email,
    password
  })

  // Guardar token en localStorage
  if (response.data.token) {
    localStorage.setItem('token', response.data.token)
  }

  return response.data
}

/**
 * Obtiene los datos del usuario autenticado
 * @returns {Promise} Response con datos del usuario
 */
export const getMe = async () => {
  const response = await apiClient.get(API_ENDPOINTS.AUTH.ME)
  return response.data
}

/**
 * Cierra sesión (elimina el token)
 */
export const logout = () => {
  localStorage.removeItem('token')
  // Opcional: Redirigir al login
  // window.location.href = '/login'
}

/**
 * Verifica si el usuario está autenticado
 * @returns {boolean} True si hay token en localStorage
 */
export const isAuthenticated = () => {
  return !!localStorage.getItem('token')
}

export default {
  login,
  register,
  getMe,
  logout,
  isAuthenticated
}
