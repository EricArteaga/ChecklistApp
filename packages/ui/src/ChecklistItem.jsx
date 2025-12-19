import React from 'react' // pequeño componente que muestra un ítem de checklist

export default function ChecklistItem({ item }) { // recibe un `item` con `checked` y `description`
  return (
    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}> {/* layout horizontal del ítem */}
      <input type="checkbox" checked={item.checked} readOnly /> {/* estado de completado (solo lectura aquí) */}
      <span>{item.description || 'TODO: descripción'}</span> {/* muestra la descripción o marcador si falta */}
    </div>
  )
}
