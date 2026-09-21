<#
.SYNOPSIS
    Cloud Infrastructure Monitoring & Logging Agent - Windows PowerShell Installer
.DESCRIPTION
    Installs Python dependencies, sets up the agent directory, downloads agent code if needed, and runs the agent process.
#>
param (
    [string]$Server = "http://localhost:8080",
    [string]$ServerUrl = "",
    [Parameter(Mandatory=$true)]
    [string]$Token,
    [string]$Uuid = "",
    [string]$DeviceUuid = "",
    [int]$Port = 9100,
    [string]$InstallDir = "$env:ProgramData\CloudMonitoringAgent"
)

# Resolve parameter aliases
if ($ServerUrl -ne "") {
    $Server = $ServerUrl
}
if ($DeviceUuid -ne "") {
    $Uuid = $DeviceUuid
}

if ([string]::IsNullOrWhiteSpace($Uuid)) {
    Write-Host "==================================================" -ForegroundColor Red
    Write-Host "[!] Error: -Uuid (device UUID) is required for authentication." -ForegroundColor Red
    Write-Host "==================================================" -ForegroundColor Red
    exit 1
}

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " Installing Cloud Monitoring Agent (Windows)     " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "[*] Target Server: $Server"
Write-Host "[*] Device UUID:   $Uuid"
Write-Host "[*] Metrics Port:  $Port"
Write-Host "[*] Install Path:  $InstallDir"

# Ensure Python is installed
if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Host "[!] Error: Python is not installed or not in PATH." -ForegroundColor Red
    Write-Host "    Please install Python 3.9+ from https://www.python.org/downloads/ and check 'Add python.exe to PATH'." -ForegroundColor Yellow
    exit 1
}

# Create installation directory
if (-not (Test-Path $InstallDir)) {
    New-Item -ItemType Directory -Path $InstallDir -Force | Out-Null
}

# Check if running next to source files or need to download bundle
# Prevent copying onto itself if executed from $InstallDir
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ResolvedScriptDir = if ($ScriptDir) { (Resolve-Path $ScriptDir -ErrorAction SilentlyContinue).Path.TrimEnd('\') } else { "" }
$ResolvedInstallDir = (Resolve-Path $InstallDir -ErrorAction SilentlyContinue)
$ResolvedInstallDirPath = if ($ResolvedInstallDir) { $ResolvedInstallDir.Path.TrimEnd('\') } else { $InstallDir.TrimEnd('\') }

if ($ResolvedScriptDir -and ($ResolvedScriptDir -ne $ResolvedInstallDirPath) -and (Test-Path "$ScriptDir\agent.py")) {
    Write-Host "[*] Copying agent files from $ScriptDir to $InstallDir..." -ForegroundColor Green
    Copy-Item -Path "$ScriptDir\*" -Destination $InstallDir -Recurse -Force
} elseif (-not (Test-Path "$InstallDir\agent.py")) {
    Write-Host "[*] Downloading agent package from $Server/agent.zip..." -ForegroundColor Green
    $ZipPath = "$InstallDir\agent.zip"
    try {
        Invoke-WebRequest -Uri "$Server/agent.zip" -OutFile $ZipPath -UseBasicParsing
        Expand-Archive -Path $ZipPath -DestinationPath "$InstallDir\temp" -Force
        if (Test-Path "$InstallDir\temp\monitoring-agent") {
            Copy-Item -Path "$InstallDir\temp\monitoring-agent\*" -Destination $InstallDir -Recurse -Force
        } else {
            Copy-Item -Path "$InstallDir\temp\*" -Destination $InstallDir -Recurse -Force
        }
        Remove-Item -Path "$InstallDir\temp" -Recurse -Force
        Remove-Item -Path $ZipPath -Force
    } catch {
        Write-Host "[!] Warning: Could not download agent.zip ($($_.Exception.Message)). Checking local files..." -ForegroundColor Yellow
    }
}

Set-Location $InstallDir

Write-Host "[*] Installing Python dependencies..." -ForegroundColor Green
python -m pip install --upgrade pip --quiet
python -m pip install -r requirements.txt --quiet

# Cleanly stop any existing agent instances to handle repeated installations
Get-CimInstance Win32_Process -Filter "CommandLine LIKE '%agent.py%'" -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "[*] Terminating prior agent instance (PID: $($_.ProcessId))..." -ForegroundColor Yellow
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep -Seconds 1

Write-Host "[+] Cloud Monitoring Agent is ready to start!" -ForegroundColor Green
Write-Host "[*] Starting agent in background..." -ForegroundColor Yellow

$AgentArgs = "agent.py --server `"$Server`" --token `"$Token`" --uuid `"$Uuid`" --port $Port"
$process = Start-Process -FilePath "python" -ArgumentList $AgentArgs -WorkingDirectory $InstallDir -PassThru -WindowStyle Hidden

# Wait a brief moment and verify the process is alive
Start-Sleep -Seconds 3

if (-not $process -or $process.HasExited) {
    Write-Host "==================================================" -ForegroundColor Red
    Write-Host "[!] ERROR: Agent failed to start or terminated unexpectedly (ExitCode: $($process.ExitCode))." -ForegroundColor Red
    Write-Host "    Please check your credentials, device UUID, and backend server URL ($Server)." -ForegroundColor Red
    Write-Host "==================================================" -ForegroundColor Red
    exit 1
}

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "[+] Agent started successfully in background (PID: $($process.Id))!" -ForegroundColor Green
Write-Host "    Target Server: $Server" -ForegroundColor Cyan
Write-Host "    Device UUID:   $Uuid" -ForegroundColor Cyan
Write-Host "    To verify metrics: Open http://localhost:$Port/metrics" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan