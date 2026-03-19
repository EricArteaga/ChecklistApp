/**
 * ═══════════════════════════════════════════════════════════════════
 * EXPORT UTILS - CSV and PDF export functionality for tasks
 * ═══════════════════════════════════════════════════════════════════
 *
 * Features:
 * • CSV export with structured data
 * • PDF export with visual formatting
 * • Options: "All tasks" or "Completed only"
 * • Preview modal before download
 * • Progress tracking
 */

/**
 * Generate filename with timestamp
 * @param {string} extension - File extension (csv, pdf)
 * @returns {string} Filename with timestamp
 */
function generateFilename(extension) {
  const now = new Date()
  const dateStr = now.toISOString().split('T')[0] // YYYY-MM-DD
  const timeStr = now.toTimeString().split(' ')[0].replace(/:/g, '-') // HH-MM-SS
  return `checklistapp-tareas-${dateStr}_${timeStr}.${extension}`
}

/**
 * Format date for export
 * @param {string|Date} date - Date to format
 * @returns {string} Formatted date string
 */
function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

/**
 * Escape CSV value
 * @param {string} value - Value to escape
 * @returns {string} Escaped value
 */
function escapeCSV(value) {
  if (value === null || value === undefined) return ''
  const stringValue = String(value)
  // If value contains comma, quote, or newline, wrap in quotes and escape quotes
  if (stringValue.includes(',') || stringValue.includes('"') || stringValue.includes('\n')) {
    return `"${stringValue.replace(/"/g, '""')}"`
  }
  return stringValue
}

/**
 * Export tasks to CSV format
 * @param {Array} tasks - Array of tasks to export
 * @param {boolean} completedOnly - If true, only export completed tasks
 * @returns {void} Downloads CSV file
 */
export function exportToCSV(tasks, completedOnly = false) {
  // Filter tasks if completedOnly is true
  const tasksToExport = completedOnly
    ? tasks.filter(task => task.completada || task.checked || false)
    : tasks

  if (tasksToExport.length === 0) {
    alert('No hay tareas para exportar')
    return
  }

  // CSV Headers
  const headers = [
    'ID',
    'Nombre',
    'Descripción',
    'Estado',
    'Fecha Creación',
    'Fecha Realización',
    'Tipo',
    'Subitems Completados',
    'Total Subitems'
  ]

  // Generate CSV rows
  const rows = tasksToExport.map(task => {
    const completed = task.completada || task.checked || false
    const subitems = task.subitems || []
    const completedSubitems = subitems.filter(s => s.checked || s.completed || false).length

    return [
      task.id,
      escapeCSV(task.nombre || task.title || ''),
      escapeCSV(task.descripcion || task.description || ''),
      completed ? 'Completada' : 'Pendiente',
      formatDate(task.createdAt || task.fechaCreacion),
      formatDate(task.fechaRealizacion),
      escapeCSV(task.tipo?.nombre || ''),
      completedSubitems,
      subitems.length
    ].join(',')
  })

  // Combine headers and rows
  const csvContent = [
    headers.join(','),
    ...rows
  ].join('\n')

  // Add UTF-8 BOM for Excel compatibility
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })

  // Create download link and trigger download
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', generateFilename('csv'))
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * Export tasks to PDF format
 * Note: This is a simplified version. For production, consider using jspdf library
 * @param {Array} tasks - Array of tasks to export
 * @param {boolean} completedOnly - If true, only export completed tasks
 * @returns {void} Downloads PDF file (as HTML/PDF)
 */
export function exportToPDF(tasks, completedOnly = false) {
  // Filter tasks if completedOnly is true
  const tasksToExport = completedOnly
    ? tasks.filter(task => task.completada || task.checked || false)
    : tasks

  if (tasksToExport.length === 0) {
    alert('No hay tareas para exportar')
    return
  }

  // Calculate statistics
  const totalTasks = tasksToExport.length
  const completedTasks = tasksToExport.filter(t => t.completada || t.checked || false).length
  const progressRate = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0

  // Generate HTML content for PDF
  const htmlContent = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <style>
        body {
          font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          margin: 40px;
          color: #333;
        }
        .header {
          text-align: center;
          margin-bottom: 40px;
          border-bottom: 2px solid #326cc3;
          padding-bottom: 20px;
        }
        .logo {
          font-size: 32px;
          font-weight: bold;
          color: #326cc3;
          margin-bottom: 10px;
        }
        .subtitle {
          font-size: 14px;
          color: #666;
        }
        .stats {
          display: flex;
          justify-content: space-around;
          margin: 30px 0;
          padding: 20px;
          background: #f5f5f5;
          border-radius: 8px;
        }
        .stat {
          text-align: center;
        }
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #326cc3;
        }
        .stat-label {
          font-size: 12px;
          color: #666;
          margin-top: 5px;
        }
        table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 20px;
        }
        th {
          background: #326cc3;
          color: white;
          padding: 12px;
          text-align: left;
          font-weight: 600;
        }
        td {
          padding: 10px;
          border-bottom: 1px solid #ddd;
        }
        tr:hover {
          background: #f9f9f9;
        }
        .status-completed {
          color: #3cc1a2;
          font-weight: 600;
        }
        .status-pending {
          color: #f59e0b;
          font-weight: 600;
        }
        .footer {
          margin-top: 40px;
          padding-top: 20px;
          border-top: 1px solid #ddd;
          text-align: center;
          font-size: 12px;
          color: #666;
        }
      </style>
    </head>
    <body>
      <div class="header">
        <div class="logo">✓ ChecklistApp</div>
        <div class="subtitle">Reporte de Tareas - ${new Date().toLocaleDateString('es-ES')}</div>
      </div>

      <div class="stats">
        <div class="stat">
          <div class="stat-value">${totalTasks}</div>
          <div class="stat-label">Total Tareas</div>
        </div>
        <div class="stat">
          <div class="stat-value">${completedTasks}</div>
          <div class="stat-label">Completadas</div>
        </div>
        <div class="stat">
          <div class="stat-value">${progressRate}%</div>
          <div class="stat-label">Progreso</div>
        </div>
      </div>

      <table>
        <thead>
          <tr>
            <th>Tarea</th>
            <th>Estado</th>
            <th>Tipo</th>
            <th>Fecha Creación</th>
          </tr>
        </thead>
        <tbody>
          ${tasksToExport.map(task => {
            const completed = task.completada || task.checked || false
            return `
              <tr>
                <td>${escapeCSV(task.nombre || task.title || 'Sin título')}</td>
                <td class="${completed ? 'status-completed' : 'status-pending'}">
                  ${completed ? '✓ Completada' : '○ Pendiente'}
                </td>
                <td>${escapeCSV(task.tipo?.nombre || 'Sin tipo')}</td>
                <td>${formatDate(task.createdAt || task.fechaCreacion)}</td>
              </tr>
            `
          }).join('')}
        </tbody>
      </table>

      <div class="footer">
        <p>Generado por ChecklistApp - ${new Date().toLocaleString('es-ES')}</p>
      </div>
    </body>
    </html>
  `

  // Create a blob and open in new window for printing/saving as PDF
  const blob = new Blob([htmlContent], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const printWindow = window.open('', '_blank')

  if (printWindow) {
    printWindow.document.write(htmlContent)
    printWindow.document.close()
    printWindow.focus()

    // Trigger print dialog after a short delay
    setTimeout(() => {
      printWindow.print()
    }, 250)

    // Note: The user can then "Save as PDF" from the print dialog
  } else {
    alert('No se pudo abrir la ventana de impresión. Por favor, permite las ventanas emergentes para esta función.')
  }

  URL.revokeObjectURL(url)
}

/**
 * Show export preview modal
 * @param {Array} tasks - Array of tasks to export
 * @param {Function} onConfirm - Callback when user confirms export
 * @returns {void} Shows modal dialog
 */
export function showExportPreview(tasks, onConfirm) {
  const completedCount = tasks.filter(t => t.completada || t.checked || false).length
  const totalCount = tasks.length

  const message = `
Vas a exportar ${totalCount} tarea${totalCount !== 1 ? 's' : ''}.
${completedCount > 0 ? `- ${completedCount} completada${completedCount !== 1 ? 's' : ''}` : ''}
${totalCount - completedCount > 0 ? `- ${totalCount - completedCount} pendiente${totalCount - completedCount !== 1 ? 's' : ''}` : ''}

¿Qué deseas exportar?
  `

  // Create a simple modal for export options
  const modal = document.createElement('div')
  modal.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  `

  modal.innerHTML = `
    <div style="
      background: white;
      padding: 24px;
      border-radius: 12px;
      max-width: 400px;
      width: 90%;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
    ">
      <h2 style="font-size: 20px; font-weight: bold; margin-bottom: 16px; color: #333;">
        Exportar Tareas
      </h2>
      <p style="color: #666; margin-bottom: 20px; white-space: pre-line;">
        ${message}
      </p>
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <button id="export-all-csv" style="
          padding: 12px;
          background: #326cc3;
          color: white;
          border: none;
          border-radius: 8px;
          cursor: pointer;
          font-weight: 600;
          transition: background 0.2s;
        ">
          📄 Exportar TODO a CSV
        </button>
        <button id="export-completed-csv" style="
          padding: 12px;
          background: #55a2d2;
          color: white;
          border: none;
          border-radius: 8px;
          cursor: pointer;
          font-weight: 600;
          transition: background 0.2s;
        ">
          ✅ Exportar COMPLETADAS a CSV
        </button>
        <button id="export-all-pdf" style="
          padding: 12px;
          background: #3cc1a2;
          color: white;
          border: none;
          border-radius: 8px;
          cursor: pointer;
          font-weight: 600;
          transition: background 0.2s;
        ">
          📊 Exportar TODO a PDF
        </button>
        <button id="export-cancel" style="
          padding: 12px;
          background: transparent;
          color: #666;
          border: 1px solid #ddd;
          border-radius: 8px;
          cursor: pointer;
          font-weight: 600;
          transition: background 0.2s;
        ">
          Cancelar
        </button>
      </div>
    </div>
  `

  // Add event listeners
  modal.querySelector('#export-all-csv').addEventListener('click', () => {
    onConfirm('csv', false)
    document.body.removeChild(modal)
  })

  modal.querySelector('#export-completed-csv').addEventListener('click', () => {
    onConfirm('csv', true)
    document.body.removeChild(modal)
  })

  modal.querySelector('#export-all-pdf').addEventListener('click', () => {
    onConfirm('pdf', false)
    document.body.removeChild(modal)
  })

  modal.querySelector('#export-cancel').addEventListener('click', () => {
    document.body.removeChild(modal)
  })

  // Close on background click
  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      document.body.removeChild(modal)
    }
  })

  document.body.appendChild(modal)
}

export default {
  exportToCSV,
  exportToPDF,
  showExportPreview
}
