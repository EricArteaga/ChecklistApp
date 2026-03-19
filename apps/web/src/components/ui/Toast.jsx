import React, { useEffect } from 'react'

/**
 * ═══════════════════════════════════════════════════════════════════
 * TOAST - Individual notification component
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Display single notification with auto-dismiss
 * • Provides immediate feedback for user actions
 * • Reduces uncertainty with clear status updates
 *
 * Types:
 * • success - Green checkmark for successful actions
 * • error - Red warning for errors
 * • warning - Yellow caution for warnings
 * • info - Blue info for informational messages
 *
 * @example
 * <Toast
 *   type="success"
 *   title="Task created"
 *   message="Your new task is ready"
 *   onClose={() => removeToast(id)}
 * />
 */
const Toast = ({ id, type = 'info', title, message, duration = 5000, onClose }) => {
  useEffect(() => {
    // Auto-dismiss after duration
    const timer = setTimeout(() => {
      onClose()
    }, duration)

    return () => clearTimeout(timer)
  }, [duration, onClose])

  const typeStyles = {
    success: {
      container: 'border-success bg-success/5',
      icon: 'text-success',
      iconBg: 'bg-success/10',
      iconPath: 'M5 13l4 4L19 7',
    },
    error: {
      container: 'border-destructive bg-destructive/5',
      icon: 'text-destructive',
      iconBg: 'bg-destructive/10',
      iconPath: 'M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    },
    warning: {
      container: 'border-warning bg-warning/5',
      icon: 'text-warning',
      iconBg: 'bg-warning/10',
      iconPath: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
    },
    info: {
      container: 'border-info bg-info/5',
      icon: 'text-info',
      iconBg: 'bg-info/10',
      iconPath: 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    },
  }

  const styles = typeStyles[type] || typeStyles.info

  return (
    <div
      className={`
        flex items-start gap-3 p-4 rounded-xl border-2 shadow-lg
        animate-slide-in-right transition-all duration-300
        ${styles.container}
      `}
      role="alert"
      aria-live="polite"
      aria-atomic="true"
    >
      {/* Icon */}
      <div className={`flex-shrink-0 w-10 h-10 rounded-full ${styles.iconBg} flex items-center justify-center`}>
        <svg
          className={`w-5 h-5 ${styles.icon}`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d={styles.iconPath}
          />
        </svg>
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        {title && (
          <h4 className="font-semibold text-foreground mb-1">
            {title}
          </h4>
        )}
        {message && (
          <p className="text-sm text-muted-foreground">
            {message}
          </p>
        )}
      </div>

      {/* Close button */}
      <button
        onClick={onClose}
        className="flex-shrink-0 p-1 text-muted-foreground hover:text-foreground transition-colors rounded-lg hover:bg-black/5 dark:hover:bg-white/5"
        aria-label="Cerrar notificación"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  )
}

export default Toast
