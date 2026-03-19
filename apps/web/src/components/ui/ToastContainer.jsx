import React from 'react'
import Toast from './Toast'

/**
 * ═══════════════════════════════════════════════════════════════════
 * TOAST CONTAINER - Displays multiple toast notifications
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Container for all toast notifications
 * • Positions toasts in top-right corner
 * • Stacks multiple toasts vertically
 * • Handles animations and transitions
 *
 * @example
 * <ToastContainer
 *   toasts={[
 *     { id: 1, type: 'success', title: 'Success', message: 'Task created' },
 *     { id: 2, type: 'error', title: 'Error', message: 'Failed to save' }
 *   ]}
 *   onRemoveToast={(id) => removeToast(id)}
 * />
 */
const ToastContainer = ({ toasts = [], onRemoveToast }) => {
  if (toasts.length === 0) return null

  return (
    <div
      className="fixed top-4 right-4 z-50 flex flex-col gap-3 max-w-md w-full"
      role="region"
      aria-label="Notificaciones"
      aria-live="polite"
    >
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          {...toast}
          onClose={() => onRemoveToast(toast.id)}
        />
      ))}
    </div>
  )
}

export default ToastContainer
