import React from 'react' // núcleo de React necesario para JSX
import { createRoot } from 'react-dom/client' // API moderna para hidratar el root (importada pero no usada abajo)
import App from './app/App' // componente raíz de la aplicación

// Creación del root y renderizado de la aplicación.
// Nota: se importó `createRoot` arriba; sin embargo el código usa `ReactDOM.createRoot`,
// lo que puede ser una inconsistencia en la referencia a la API de `react-dom`.
const root = ReactDOM.createRoot(
    document.getElementById('root')
);

root.render(
    <React.StrictMode>
        <App /> {/* renderiza el componente raíz dentro de StrictMode para detectar problemas */}
    </React.StrictMode>
);