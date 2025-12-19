import React from 'react' // componente funcional simple para la vista de checklist

export default function Checklist() { // muestra una lista estática como placeholder
  // Lógica pendiente: obtener datos reales desde la API backend y manejar estados (loading/error)
  const placeholder = [{ id: 1, title: 'Ejemplo checklist', items: [] }] // datos de ejemplo para renderizar

  return (
    <section> {/* sección que contiene la UI de lista */}
      <h2>Checklist</h2> {/* subtítulo de la vista */}
      <p>Listado de checklists (placeholder).</p> {/* explicación breve del contenido actual */}
      <ul>
        {/* renderiza cada checklist con un marcador de items pendiente */}
        {placeholder.map(c => (
          <li key={c.id}>
            {c.title} — <em>TODO: mostrar items</em>
            </li> 
          
        ))}
      </ul>
      <p>Acciones: <strong>TODO</strong> crear, editar, eliminar.</p> {/* recordatorio de funcionalidades faltantes */}
    </section>
  )
}
