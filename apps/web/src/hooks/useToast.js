import { useToast as useToastContext } from '../contexts/ToastContext'

/**
 * ═══════════════════════════════════════════════════════════════════
 * USE TOAST HOOK - Convenience hook for toast notifications
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Provides convenient methods for common toast types
 * • Simplifies toast usage in components
 * • Auto-generates titles and messages for common actions
 *
 * @example
 * const { success, error, warning, info } = useToast()
 *
 * success('Task created', 'Your new task is ready')
 * error('Failed to save', 'Please try again')
 */
export const useToast = () => {
  const { addToast, removeToast } = useToastContext()

  const success = (title, message, options = {}) => {
    return addToast({ type: 'success', title, message, ...options })
  }

  const error = (title, message, options = {}) => {
    return addToast({ type: 'error', title, message, duration: 7000, ...options })
  }

  const warning = (title, message, options = {}) => {
    return addToast({ type: 'warning', title, message, ...options })
  }

  const info = (title, message, options = {}) => {
    return addToast({ type: 'info', title, message, ...options })
  }

  return {
    addToast,
    removeToast,
    success,
    error,
    warning,
    info,
  }
}

export default useToast
