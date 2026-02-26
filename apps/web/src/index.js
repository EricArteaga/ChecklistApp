import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
// Load shared styles FIRST (from packages/ui)
import '@checklist/ui/styles/global.css'
// Then load app-specific styles (web-only)
import './index.css'
import App from './app/App'

const container = document.getElementById('root')
const root = createRoot(container)

root.render(
    <React.StrictMode>
        <BrowserRouter>
            <App />
        </BrowserRouter>
    </React.StrictMode>
);