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
    correo: email,  // Backend espera 'correo', no 'email'
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
    correo: email,  // Backend espera 'correo', no 'email'
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
 * Maneja el error 401 Unauthorized de forma centralizada
 * @param {string} reason - Razón del logout (ej: 'Sesión expirada', 'Logout manual')
 */
export const handleUnauthorized = (reason = 'Sesión expirada') => {
  localStorage.removeItem('token')
  localStorage.removeItem('userCache')
  // Solo redirigir si no estamos ya en login o register
  if (!window.location.pathname.match(/\/(login|register)/)) {
    window.location.href = '/login'
  }
  console.log(`Logout: ${reason}`)
}

/**
 * Cierra sesión (elimina el token)
 */
export const logout = () => {
  handleUnauthorized('Logout manual')
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
