import React from 'react'

export default function EmptyState({
  icon,
  title,
  description,
  action,
  className = '',
}) {
  return (
    <div
      className={`flex flex-col items-center justify-center gap-4 text-center p-12 ${className}`.trim()}
    >
      {icon && (
        <div className="text-muted-foreground/50">
          {icon}
        </div>
      )}
      {title && (
        <h3 className="text-lg font-semibold text-foreground">
          {title}
        </h3>
      )}
      {description && (
        <p className="text-sm text-muted-foreground max-w-sm">
          {description}
        </p>
      )}
      {action && (
        <div className="mt-2">
          {action}
        </div>
      )}
    </div>
  )
}
