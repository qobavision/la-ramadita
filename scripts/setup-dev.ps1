#Requires -Version 5.1
<#
.SYNOPSIS
  Configura una sola vez las variables de entorno locales para el backend.

.DESCRIPTION
  Guarda valores en el usuario de Windows. Spring Boot las lee al arrancar.
  No escribe secretos en archivos del repositorio y no imprime JWT_SECRET.

  Uso (desde la raiz de la-ramadita):
    powershell -ExecutionPolicy Bypass -File .\scripts\setup-dev.ps1
#>
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$UrlJdbcPredeterminada = 'jdbc:sqlserver://localhost:1433;databaseName=LaRamadita;encrypt=true;trustServerCertificate=true'
$UsuarioBdPredeterminado = 'laramadita_app'
$BytesMinimosJwt = 32

function Es-Interactivo {
	try {
		return [Environment]::UserInteractive -and -not [Console]::IsInputRedirected
	} catch {
		return $false
	}
}

function Obtener-Usuario([string]$Nombre) {
	$valor = [Environment]::GetEnvironmentVariable($Nombre, 'User')
	if ([string]::IsNullOrWhiteSpace($valor)) {
		return $null
	}
	return $valor
}

function Guardar-Usuario([string]$Nombre, [string]$Valor) {
	[Environment]::SetEnvironmentVariable($Nombre, $Valor, 'User')
}

function Confirmar-Definida([string]$Nombre) {
	$existe = -not [string]::IsNullOrWhiteSpace((Obtener-Usuario $Nombre))
	if ($existe) {
		Write-Host "$Nombre ya estaba configurada. No se reemplaza."
	}
	return $existe
}

function Nuevo-JwtSecret {
	$bytes = New-Object byte[] 48
	$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
	try {
		$rng.GetBytes($bytes)
	} finally {
		$rng.Dispose()
	}
	return [Convert]::ToBase64String($bytes)
}

function Pedir-Texto([string]$Etiqueta, [string]$Predeterminado) {
	if (-not (Es-Interactivo)) {
		return $Predeterminado
	}
	$sufijo = if ($Predeterminado) { " [$Predeterminado]" } else { '' }
	$entrada = Read-Host "$Etiqueta$sufijo"
	if ([string]::IsNullOrWhiteSpace($entrada)) {
		return $Predeterminado
	}
	return $entrada.Trim()
}

function Pedir-Secreto([string]$Etiqueta) {
	if (-not (Es-Interactivo)) {
		return $null
	}
	$seguro = Read-Host $Etiqueta -AsSecureString
	$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($seguro)
	try {
		$texto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
		if ([string]::IsNullOrWhiteSpace($texto)) {
			return $null
		}
		return $texto
	} finally {
		[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
	}
}

Write-Host ''
Write-Host 'Configuracion local de La Ramadita'
Write-Host 'Las variables se guardan en el usuario de Windows. Ningun secreto se escribe en Git.'
Write-Host ''

if (Confirmar-Definida 'JWT_SECRET') {
	# Ya existe: no se genera otra.
} else {
	Guardar-Usuario 'JWT_SECRET' (Nuevo-JwtSecret)
	Write-Host 'JWT_SECRET configurada correctamente.'
}

if (-not (Confirmar-Definida 'DB_URL')) {
	$url = Pedir-Texto 'DB_URL (cadena JDBC de SQL Server)' $UrlJdbcPredeterminada
	if ([string]::IsNullOrWhiteSpace($url)) {
		$url = $UrlJdbcPredeterminada
	}
	Guardar-Usuario 'DB_URL' $url
	Write-Host 'DB_URL configurada.'
}

if (-not (Confirmar-Definida 'DB_USERNAME')) {
	$usuarioBd = Pedir-Texto 'DB_USERNAME (usuario de SQL Server)' $UsuarioBdPredeterminado
	if ([string]::IsNullOrWhiteSpace($usuarioBd)) {
		$usuarioBd = $UsuarioBdPredeterminado
	}
	Guardar-Usuario 'DB_USERNAME' $usuarioBd
	Write-Host 'DB_USERNAME configurada.'
}

if (-not (Confirmar-Definida 'DB_PASSWORD')) {
	$passwordBd = Pedir-Secreto 'DB_PASSWORD (no se muestra al escribir)'
	if ([string]::IsNullOrWhiteSpace($passwordBd)) {
		Write-Host 'DB_PASSWORD no se configuro. El backend no arrancara hasta definirla.'
	} else {
		Guardar-Usuario 'DB_PASSWORD' $passwordBd
		Write-Host 'DB_PASSWORD configurada.'
	}
}

Write-Host ''
Write-Host 'Administrador inicial (opcional). Solo se usa si la tabla usuarios esta vacia.'
if ((Confirmar-Definida 'APP_ADMIN_USERNAME') -and (Confirmar-Definida 'APP_ADMIN_PASSWORD')) {
	# Ya estan las dos.
} elseif (Es-Interactivo) {
	$adminUser = Pedir-Texto 'APP_ADMIN_USERNAME (Enter para omitir)' ''
	if ([string]::IsNullOrWhiteSpace($adminUser)) {
		Write-Host 'Administrador inicial omitido.'
	} else {
		$adminPass = Pedir-Secreto 'APP_ADMIN_PASSWORD (minimo 8 caracteres, no se muestra)'
		if ([string]::IsNullOrWhiteSpace($adminPass)) {
			Write-Host 'APP_ADMIN_PASSWORD vacia. No se guardo el administrador inicial.'
		} elseif ($adminPass.Length -lt 8) {
			Write-Host 'APP_ADMIN_PASSWORD debe tener al menos 8 caracteres. No se guardo.'
		} else {
			Guardar-Usuario 'APP_ADMIN_USERNAME' $adminUser
			Guardar-Usuario 'APP_ADMIN_PASSWORD' $adminPass
			Write-Host 'APP_ADMIN_USERNAME y APP_ADMIN_PASSWORD configuradas.'
		}
	}
} else {
	Write-Host 'APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD no se pidieron (sesion no interactiva).'
}

Write-Host ''
Write-Host 'JWT_SECRET configurada correctamente.'
Write-Host 'Cierra y vuelve a abrir la terminal antes de iniciar el backend.'
Write-Host ''
Write-Host 'Luego:'
Write-Host '  cd backend'
Write-Host '  .\mvnw.cmd spring-boot:run'
Write-Host ''
Write-Host 'Opcionales (no hace falta configurarlas si los valores por defecto sirven):'
Write-Host '  JWT_EXPIRATION_MINUTES  (480 si no se define)'
Write-Host '  CORS_ALLOWED_ORIGINS    (http://localhost:4200 si no se define)'
Write-Host ''
