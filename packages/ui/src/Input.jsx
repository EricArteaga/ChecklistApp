import React from 'react'

export default function Input({
  className = '',
  type = 'text',
  disabled = false,
  error = false,
  ...props
}) {
  const baseStyles = 'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 transition-all duration-200'

  const errorStyles = error ? 'border-destructive focus-visible:ring-destructive' : ''

  return (
    <input
      type={type}
      className={`${baseStyles} ${errorStyles} ${className}`.trim()}
      disabled={disabled}
      aria-invalid={error}
      {...props}
    />
  )
}
