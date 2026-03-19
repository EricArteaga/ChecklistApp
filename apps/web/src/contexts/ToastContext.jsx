import React, { createContext, useContext, useState, useCallback } from 'react'

/**
 * ═══════════════════════════════════════════════════════════════════
 * TOAST CONTEXT - Global state for toast notifications
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Provides global toast notification system
 * • Accessible from any component via useToast hook
 * • Manages toast lifecycle (add, remove, auto-dismiss)
 *
 * @example
 * const { addToast } = useToast()
 *
 * addToast({
 *   type: 'success',
 *   title: 'Task created',
 *   message: 'Your new task is ready'
 * })
 */
const ToastContext = createContext()

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([])

  const addToast = useCallback((toast) => {
    const id = Date.now()
    const newToast = {
      id,
      type: 'info',
      title: '',
      message: '',
      duration: 5000,
      ...toast,
    }

    setToasts((prev) => [...prev, newToast])
    return id
  }, [])

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id))
  }, [])

  const value = {
    toasts,
    addToast,
    removeToast,
  }

  return (
    <ToastContext.Provider value={value}>
      {children}
    </ToastContext.Provider>
  )
}

export const useToast = () => {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider')
  }
  return context
}

export default ToastContext
