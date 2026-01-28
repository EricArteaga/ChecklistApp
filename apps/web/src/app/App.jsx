import React from 'react' // núcleo de React para crear componentes y hooks
import Checklist from '../features/checklist/Checklist' // componente de la vista de checklist
import Heatmap from '../features/heatmap/Heatmap' // componente de la vista de heatmap

export default function App() { // componente raíz de la aplicación web
  const [view, setView] = React.useState('checklist') // estado que controla la vista activa
  return (
<div className="font-sans p-5 bg-gray-50 min-h-screen"> {/* contenedor principal con clases Tailwind */}
      <h1 className="text-3xl font-bold text-gray-900 mb-6">ChecklistApp — Web</h1> {/* título con estilos Tailwind */}
      <nav className="mb-5 space-x-4"> {/* navegación con espaciado y layout */}
        <button 
          onClick={() => setView('checklist')}
          className={`px-4 py-2 rounded-lg font-medium transition-colors ${
            view === 'checklist' 
              ? 'bg-primary-500 text-white' 
              : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
          }`}
        >
          Checklist
        </button> {/* botón activo/desactivado con estilos dinámicos */}
        <button 
          onClick={() => setView('heatmap')}
          className={`px-4 py-2 rounded-lg font-medium transition-colors ${
            view === 'heatmap' 
              ? 'bg-primary-500 text-white' 
              : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
          }`}
        >
          Heatmap
        </button> {/* botón activo/desactivado con estilos dinámicos */}
      </nav>
      <main className="max-w-4xl mx-auto"> {/* área principal con ancho máximo y centrado */}
        {view === 'checklist' && <Checklist />} {/* renderiza `Checklist` cuando `view` es 'checklist' */}
        {view === 'heatmap' && <Heatmap />} {/* renderiza `Heatmap` cuando `view` es 'heatmap' */}
      </main>
    </div>
  )
}
