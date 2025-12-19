import React from 'react' // núcleo de React para crear componentes y hooks
import Checklist from '../features/checklist/Checklist' // componente de la vista de checklist
import Heatmap from '../features/heatmap/Heatmap' // componente de la vista de heatmap

export default function App() { // componente raíz de la aplicación web
  const [view, setView] = React.useState('checklist') // estado que controla la vista activa
  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: 20 }}> {/* contenedor principal con estilos inline básicos */}
      <h1>ChecklistApp — Web</h1> {/* título de la app */}
      <nav style={{ marginBottom: 20 }}> {/* navegación simple para cambiar vistas */}
        <button onClick={() => setView('checklist')}>Checklist</button> {/* activa la vista de checklist */}
        <button onClick={() => setView('heatmap')}>Heatmap</button> {/* activa la vista de heatmap */}
      </nav>
      <main> {/* área principal donde se renderizan las vistas según el estado `view` */}
        {view === 'checklist' && <Checklist />} {/* renderiza `Checklist` cuando `view` es 'checklist' */}
        {view === 'heatmap' && <Heatmap />} {/* renderiza `Heatmap` cuando `view` es 'heatmap' */}
      </main>
    </div>
  )
}
