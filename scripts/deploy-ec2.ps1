[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ServerIp,

    [Parameter(Mandatory = $false)]
    [string]$KeyPath = "terraform/medibook-key.pem",

    [Parameter(Mandatory = $true)]
    [string]$EnvFile,

    [Parameter(Mandatory = $false)]
    [string]$User = "ubuntu",

    [Parameter(Mandatory = $false)]
    [int]$MaxRetries = 30
)

$ErrorActionPreference = "Continue"

Write-Host "=============================================="
Write-Host " MediBook Automated EC2 Deployment & Health Check"
Write-Host " Target Host: $ServerIp"
Write-Host " SSH Key: $KeyPath"
Write-Host " Environment file: $EnvFile"
Write-Host "=============================================="

if (-not (Test-Path $KeyPath)) {
    throw "SSH private key not found at $KeyPath. Run terraform apply first."
}
if (-not (Test-Path $EnvFile)) {
    throw "Deployment environment file not found at $EnvFile."
}

# Fix Windows permissions on the private key if needed for OpenSSH.
try {
    icacls.exe $KeyPath /inheritance:r /grant:r "$($env:USERNAME):(R)" | Out-Null
} catch {
    Write-Warning "Could not tighten private key ACL automatically. Continuing with existing permissions."
}

$sshOpts = @(
    "-o", "StrictHostKeyChecking=no",
    "-o", "UserKnownHostsFile=/dev/null",
    "-o", "ConnectTimeout=10",
    "-i", $KeyPath
)

# Step 1: Wait for SSH and Docker to be ready.
Write-Host "--- Checking SSH readiness on $ServerIp ---"
$sshReady = $false
for ($i = 1; $i -le $MaxRetries; $i++) {
    Write-Host "Waiting for SSH connection (attempt $i/$MaxRetries)..."
    $check = & ssh.exe @sshOpts "$User@$ServerIp" "docker --version 2>&1"
    if ($LASTEXITCODE -eq 0 -and $check -match "Docker") {
        Write-Host "SSH and Docker ready: $check"
        $sshReady = $true
        break
    }
    Start-Sleep -Seconds 10
}

if (-not $sshReady) {
    throw "Timeout waiting for SSH and Docker on $ServerIp"
}

# Step 2: Transfer deployment assets.
Write-Host "--- Transferring deployment assets to $ServerIp ---"
& ssh.exe @sshOpts "$User@$ServerIp" "mkdir -p /opt/medibook/backend /opt/medibook/frontend"
if ($LASTEXITCODE -ne 0) { throw "Failed to create remote deployment directories." }

$copies = @(
    @{ Source = "docker-compose.prod.yml"; Destination = "/opt/medibook/docker-compose.prod.yml" },
    @{ Source = "backend/pom.xml"; Destination = "/opt/medibook/backend/pom.xml" },
    @{ Source = "backend/Dockerfile"; Destination = "/opt/medibook/backend/Dockerfile" },
    @{ Source = "backend/.dockerignore"; Destination = "/opt/medibook/backend/.dockerignore" },
    @{ Source = "frontend/package.json"; Destination = "/opt/medibook/frontend/package.json" },
    @{ Source = "frontend/package-lock.json"; Destination = "/opt/medibook/frontend/package-lock.json" },
    @{ Source = "frontend/Dockerfile"; Destination = "/opt/medibook/frontend/Dockerfile" },
    @{ Source = "frontend/.dockerignore"; Destination = "/opt/medibook/frontend/.dockerignore" },
    @{ Source = "frontend/nginx.conf"; Destination = "/opt/medibook/frontend/nginx.conf" },
    @{ Source = "frontend/index.html"; Destination = "/opt/medibook/frontend/index.html" },
    @{ Source = "frontend/vite.config.js"; Destination = "/opt/medibook/frontend/vite.config.js" }
)

foreach ($item in $copies) {
    if (-not (Test-Path $item.Source)) { throw "Required deployment file is missing: $($item.Source)" }
    & scp.exe @sshOpts $item.Source "$User@${ServerIp}:$($item.Destination)"
    if ($LASTEXITCODE -ne 0) { throw "Failed to copy $($item.Source) to the EC2 host." }
}

& scp.exe @sshOpts -r "backend/src" "$User@${ServerIp}:/opt/medibook/backend/"
if ($LASTEXITCODE -ne 0) { throw "Failed to copy backend source to the EC2 host." }

& scp.exe @sshOpts -r "frontend/src" "$User@${ServerIp}:/opt/medibook/frontend/"
if ($LASTEXITCODE -ne 0) { throw "Failed to copy frontend source to the EC2 host." }

# Transfer the deployment environment file separately and restrict its permissions.
& scp.exe @sshOpts $EnvFile "$User@${ServerIp}:/opt/medibook/.env"
if ($LASTEXITCODE -ne 0) { throw "Failed to copy the deployment environment file to the EC2 host." }

& ssh.exe @sshOpts "$User@$ServerIp" "chmod 600 /opt/medibook/.env"
if ($LASTEXITCODE -ne 0) { throw "Failed to restrict permissions on the remote environment file." }

# Step 3: Run Docker Compose on the remote host.
Write-Host "--- Launching MediBook containers on $ServerIp ---"
$deployCmd = "cd /opt/medibook && docker compose --env-file /opt/medibook/.env -f docker-compose.prod.yml up -d --build"
& ssh.exe @sshOpts "$User@$ServerIp" $deployCmd
if ($LASTEXITCODE -ne 0) { throw "Docker Compose deployment failed on the EC2 host." }

# Step 4: Verify frontend, backend health and API communication.
Write-Host "--- Verifying application, health endpoint and API ---"
$frontendUrl = "http://${ServerIp}/"
$healthUrl = "http://${ServerIp}/actuator/health"
$apiUrl = "http://${ServerIp}/api/specialties"
$isHealthy = $false

for ($j = 1; $j -le 30; $j++) {
    Write-Host "Health probe $j/30 on $healthUrl ..."
    try {
        $frontend = Invoke-WebRequest -Uri $frontendUrl -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5 -ErrorAction Stop
        $api = Invoke-WebRequest -Uri $apiUrl -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($frontend.StatusCode -eq 200 -and $health.status -eq "UP" -and $api.StatusCode -eq 200) {
            Write-Host "Frontend HTTP status: $($frontend.StatusCode)"
            Write-Host "Backend health status: $($health.status)"
            Write-Host "API HTTP status: $($api.StatusCode)"
            $isHealthy = $true
            break
        }
    } catch {
        Write-Host "Waiting for services to become healthy..."
    }
    Start-Sleep -Seconds 10
}

if (-not $isHealthy) {
    throw "Application failed the frontend, health or API verification after deployment."
}

Write-Host "=============================================="
Write-Host " Deployment Successful"
Write-Host " Application URL: $frontendUrl"
Write-Host " Actuator Health: $healthUrl"
Write-Host "=============================================="
