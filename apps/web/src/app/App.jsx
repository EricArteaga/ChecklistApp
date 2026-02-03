import React, { useState, useEffect } from 'react' // Importa hooks de React para manejo de estado y efectos secundarios
import Checklist from '../features/checklist/Checklist' // Componente para gestión de checklists
import Heatmap from '../features/heatmap/Heatmap' // Componente para visualización de actividad

// Configuración de pestañas de navegación disponibles en la aplicación
const tabs = [
  { id: 'checklist', label: 'Checklist', icon: '✓' }, // Vista principal de gestión de tareas
  { id: 'heatmap', label: 'Heatmap', icon: '📊' }, // Vista de estadísticas y actividad
]

// Temas de la app
const themes = ["light", "dark"];

export default function App() {
  const [view, setView] = useState('checklist') // Estado que controla la vista activa (checklist o heatmap)
  const [mounted, setMounted] = useState(false) // Estado para controlar animación inicial de montaje
  const [theme, setTheme] = useState(themes[0]) // Estado que controla el tema de la aplicación 

  // Efecto para marcar que el componente se ha montado completamente
  // Esto evita parpadeos en animaciones durante el primer renderizado
  useEffect(() => {
    setMounted(true)
  }, []) // Array vacío indica que solo se ejecuta una vez al montar

  return (
    <div className={ `mx-auto min-h-dvh bg-gradient-to-br from-gray-700 via-gray-800 to-gray-900 ${theme}`}>
      {/* Header - Contiene branding y navegación principal */}
      <header className="border-b bg-card/50 backdrop-blur-sm sticky top-0 z-10">
        <div className="container mx-auto px-4 py-6 max-w-6xl">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            {/* Sección de branding con logo y título */}
            <div className="flex items-center gap-3 animate-fade-in">
              <div className="flex items-center justify-center w-10 h-10 rounded-lg bg-primary text-primary-foreground">
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                </svg>
              </div>
              <div>
                <h1 className="text-2xl font-bold text-foreground ">
                  ChecklistApp
                </h1>
                <p className="text-xs text-muted-foreground">Gestión eficiente de tareas</p>
              </div>
            </div>

            {/* Botón para cambiar de tema */}
            <div className='flex items-center gap-1'>
              <button
                onClick={() => setTheme(theme === themes[0] ? themes[1] : themes[0])}
                className="px-3 py-1.5 text-sm bg-secondary text-secondary-foreground rounded-md hover:bg-secondary/80 transition-colors"
              >
                {theme === themes[0] ? '🌙' : '☀️'}
              </button>
            </div>

            {/* Navegación por pestañas con accesibilidad ARIA */}
            <nav
              className="inline-flex p-1 bg-muted rounded-lg animate-slide-in"
              role="tablist" // Indica que es una lista de pestañas para lectores de pantalla
              aria-label="Vistas de la aplicación" // Descripción para accesibilidad
            >
              {tabs.map((tab) => (
                <button
                  key={tab.id} // Identificador único para React reconciliation
                  onClick={() => setView(tab.id)} // Cambia la vista activa al hacer clic
                  role="tab" // Rol ARIA para pestaña
                  aria-selected={view === tab.id} // Indica si la pestaña está seleccionada
                  aria-controls={`${tab.id}-panel`} // Conecta la pestaña con su panel de contenido
                  className={`
                    relative px-4 py-2 rounded-md text-sm font-medium
                    transition-all duration-200
                    flex items-center gap-2
                    ${view === tab.id
                      ? 'bg-background text-foreground shadow-sm' // Estilos para pestaña activa
                      : 'text-muted-foreground hover:text-foreground hover:bg-background/50' // Estilos para pestaña inactiva
                    }
                  `}
                >
                  <span className="text-base" role="img" aria-label={tab.label}>
                    {tab.icon}
                  </span>
                  <span>{tab.label}</span>
                  {view === tab.id && (
                    <span className="absolute inset-0 rounded-md ring-2 ring-ring ring-offset-2 pointer-events-none" />
                  )}
                </button>
              ))}
            </nav>
          </div>
        </div>
      </header>

      {/* Área de contenido principal - Renderiza el componente correspondiente a la vista activa */}
      <main className="container mx-auto px-4 py-8 max-w-6xl">
        <div
          id={`${view}-panel`} // ID que conecta con aria-labelledby de las pestañas
          role="tabpanel" // Rol ARIA para panel de contenido
          aria-labelledby={`${view}-tab`} // Conecta con su pestaña correspondiente
          className={mounted ? 'animate-fade-in' : ''} // Aplica animación solo después del montaje
        >
          {view === 'checklist' && <Checklist />} // Renderiza componente de checklist si la vista es 'checklist'
          {view === 'heatmap' && <Heatmap />} // Renderiza componente de heatmap si la vista es 'heatmap'
        </div>
      </main>

      {/* Footer - Información institucional y enlaces de ayuda */}
      <footer className="border-t bg-card/30 backdrop-blur-sm mt-auto">
        <div className="container mx-auto px-4 py-6 max-w-6xl">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-muted-foreground">
            <p>© 2026 ChecklistApp. Todos los derechos reservados.</p>
            <div className="flex items-center gap-4">
              <a
                href="#"
                className="hover:text-foreground transition-colors"
                aria-label="Documentación"
              >
                Documentación
              </a>
              <a
                href="#"
                className="hover:text-foreground transition-colors"
                aria-label="Soporte"
              >
                Soporte
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
