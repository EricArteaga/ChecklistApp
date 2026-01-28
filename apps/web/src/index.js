import React from 'react' // núcleo de React necesario para JSX
import { createRoot } from 'react-dom/client' // API moderna para hidratar el root
import App from './app/App' // componente raíz de la aplicación
import './index.css' // estilos globales incluyendo Tailwind CSS

// Creación del root y renderizado de la aplicación.
const container = document.getElementById('root'); // obtiene el elemento DOM donde se montará la app
const root = createRoot(container); // crea el root de React para el contenedor

root.render(
    <React.StrictMode>
        <App /> {/* renderiza el componente raíz dentro de StrictMode para detectar problemas */}
    </React.StrictMode>
);