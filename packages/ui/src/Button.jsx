import React from 'react' // Importa librería principal de React

// Configuración de variantes y tamaños de botones usando objeto de configuración
// Permite estilos consistentes y mantenibles
const buttonVariants = {
  variant: {
    default: 'bg-primary text-primary-foreground hover:bg-primary/90', // Botón principal con colores del tema
    destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90', // Para acciones destructivas (eliminar, etc.)
    outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground', // Botón con borde transparente
    secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80', // Botón secundario menos prominente
    ghost: 'hover:bg-accent hover:text-accent-foreground', // Botón sin fondo, solo efecto hover
    link: 'text-primary underline-offset-4 hover:underline', // Estilo de enlace texto
  },
  size: {
    default: 'h-10 px-4 py-2', // Tamaño estándar del botón
    sm: 'h-9 rounded-md px-3', // Versión pequeña
    lg: 'h-11 rounded-md px-8', // Versión grande
    icon: 'h-10 w-10', // Botón cuadrado para iconos
  },
}

// Componente Button principal con props configurables
const Button = React.forwardRef(function Button({
  className = '', // Clases CSS adicionales (default: cadena vacía)
  variant = 'default', // Variante de estilo (default: primario)
  size = 'default', // Tamaño del botón (default: estándar)
  type = 'button', // Tipo de botón (default: 'button' para prevenir submits accidentales)
  disabled = false, // Estado deshabilitado (default: falso)
  loading = false, // Estado de carga con spinner (default: falso)
  children, // Contenido del botón (texto, iconos, etc.)
  ...props // Resto de props pasadas al elemento button nativo
}, ref) {
  // Estilos base aplicados a todos los botones
  const baseStyles = 'inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50'

  // Obtiene estilos de variante y tamaño, con fallback a valores por defecto
  const variantStyles = buttonVariants.variant[variant] || buttonVariants.variant.default
  const sizeStyles = buttonVariants.size[size] || buttonVariants.size.default

  return (
    <button
      type={type} // Pasa el tipo al elemento button (previene submits accidentales)
      // Combina todos los estilos y elimina espacios extra
      className={`${baseStyles} ${variantStyles} ${sizeStyles} ${className}`.trim()}
      disabled={disabled || loading} // Deshabilita también durante estado de carga
      aria-disabled={disabled || loading} // Estado para accesibilidad
      ref={ref}
      {...props} // Pasa resto de props como onClick, etc.
    >
      {/* Muestra spinner animado cuando está en estado de carga */}
      {loading && (
        <>
          <span className="sr-only" aria-live="polite">
            Cargando...
          </span>
          <svg
            className="mr-2 h-4 w-4 animate-spin" // Animación de rotación
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            aria-hidden="true" // Oculto para lectores de pantalla
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
        </>
      )}
      {children} {/* Renderiza contenido del botón (texto, iconos, etc.) */}
    </button>
  )
})

Button.displayName = 'Button'

export default Button
