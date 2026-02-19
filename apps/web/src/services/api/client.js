/**
 * Cliente Axios configurado para la API
 * Maneja autenticación, interceptores y errores
 */

import axios from 'axios'
import { API_BASE_URL } from './config'

// Crear instancia de Axios con configuración base
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000, // 10 segundos
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor de request: Agregar token JWT a cada petición
apiClient.interceptors.request.use(
  (config) => {
    // Obtener token del localStorage
    const token = localStorage.getItem('token')

    if (token) {
      // Agregar token al header Authorization
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    // Error en la configuración del request
    return Promise.reject(error)
  }
)

// Interceptor de response: Manejar errores comunes
apiClient.interceptors.response.use(
  (response) => {
    // Response exitosa
    return response
  },
  (error) => {
    // Manejo de errores
    if (error.response) {
      // El servidor respondió con un código de error
      const { status, data } = error.response

      switch (status) {
        case 401:
          // No autorizado - Token inválido o expirado
          localStorage.removeItem('token')
          window.location.href = '/login'
          return Promise.reject(new Error('Sesión expirada. Por favor, inicia sesión nuevamente.'))

        case 403:
          // Prohibido - No tienes permisos
          return Promise.reject(new Error('No tienes permisos para realizar esta acción.'))

        case 404:
          // Recurso no encontrado
          return Promise.reject(new Error(data.message || 'Recurso no encontrado'))

        case 500:
          // Error interno del servidor
          return Promise.reject(new Error('Error del servidor. Por favor, intenta más tarde.'))

        default:
          // Otros errores
          return Promise.reject(new Error(data.message || 'Ocurrió un error inesperado'))
      }
    } else if (error.request) {
      // El request se hizo pero no se recibió respuesta
      return Promise.reject(new Error('No se pudo conectar con el servidor. Verifica tu conexión.'))
    } else {
      // Error al configurar el request
      return Promise.reject(new Error('Error al configurar la petición'))
    }
  }
)

export default apiClient
