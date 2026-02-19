/**
 * Configuración de URLs de la API
 */

// URL base de la API backend
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080'

// Endpoints de la API
export const API_ENDPOINTS = {
  // Auth
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
    ME: '/api/auth/me',
    REFRESH: '/api/auth/refresh'
  },

  // Tasks
  TASKS: {
    LIST: '/api/tasks',
    BY_ID: (id) => `/api/tasks/${id}`,
    BY_USER: (userId) => `/api/tasks/usuario/${userId}`,
    PENDING: (userId) => `/api/tasks/usuario/${userId}/pendientes`,
    COMPLETED: (userId) => `/api/tasks/usuario/${userId}/completadas`
  },

  // Users
  USERS: {
    LIST: '/api/usuarios',
    BY_ID: (id) => `/api/usuarios/${id}`
  },

  // Types
  TYPES: {
    LIST: '/api/tipos',
    BY_ID: (id) => `/api/tipos/${id}`,
    BY_USER: (userId) => `/api/tipos/usuario/${userId}`
  }
}
