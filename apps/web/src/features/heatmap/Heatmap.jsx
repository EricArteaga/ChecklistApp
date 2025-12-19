import React from 'react' // componente para la vista de heatmap

export default function Heatmap() { // vista placeholder para la visualización de calor (usage heatmap)
  // Pendiente: recibir datos de uso y renderizar una visualización (canvas/SVG/d3)
  return (
    <section>
      <h2>Heatmap</h2> {/* título de la vista */}
      <p>Visualización heatmap (placeholder).</p> {/* nota que esta es una representación temporal */}
      <div style={{ width: 400, height: 200, background: '#eee', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <span>TODO: Renderizar heatmap</span> {/* marcador visual indicando trabajo pendiente */}
      </div>
    </section>
  )
}
