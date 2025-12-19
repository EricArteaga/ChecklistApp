import React from 'react' // componente UI reutilizable

export default function Button({ children, onClick }) { // botón simple que acepta contenido y manejador
  return (
    <button onClick={onClick} style={{ padding: '8px 12px', borderRadius: 4 }}> {/* estilos inline mínimos */}
      {children} {/* contenido del botón pasado desde el padre */}
    </button>
  )
}
