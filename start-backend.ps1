# Script para iniciar el backend de ChecklistApp
# Carga las variables del archivo .env y ejecuta el servidor

# Leer el archivo .env y configurar las variables de entorno
Get-Content .env | Where-Object { $_ -notmatch '^#' } | ForEach-Object {
    if ($_ -match '^(.+?)=(.+)$') {
        $name = $matches[1]
        $value = $matches[2]
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

# Navegar al directorio del backend
cd services/api

# Ejecutar el servidor
Write-Host "Iniciando backend..." -ForegroundColor Green
.\gradlew.bat bootRun
