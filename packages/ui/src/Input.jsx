import React from 'react'

const Input = React.forwardRef(function Input({
  className = '',
  type = 'text',
  disabled = false,
  error = false,
  required = false,
  errorMessage = '',
  'aria-labelledby': ariaLabelledby,
  ...props
}, ref) {
  const baseStyles = 'flex h-10 w-full rounded-md border-2 border-input bg-white dark:bg-gray-800 px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50 transition-all duration-200 shadow-sm hover:shadow-md'

  const errorStyles = error ? 'border-destructive focus-visible:ring-destructive focus-visible:border-destructive' : ''

  return (
    <>
      <input
        type={type}
        required={required}
        aria-labelledby={ariaLabelledby}
        className={`${baseStyles} ${errorStyles} ${className}`.trim()}
        disabled={disabled}
        aria-invalid={error}
        ref={ref}
        {...props}
      />
      {error && errorMessage && (
        <p className="text-destructive text-sm mt-1" role="alert">
          {errorMessage}
        </p>
      )}
    </>
  )
})

Input.displayName = 'Input'

export default Input
