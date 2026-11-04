pipeline {
    agent any

    options {
        timeout(time: 90, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }

    environment {
        // The PATH intentionally includes the actual local tool locations used by this project.
        PATH = "C:\\Teraform;C:\\apache-maven-3.9.11\\bin;C:\\Program Files\\Java\\jdk-25.0.4\\bin;C:\\Program Files\\nodejs;C:\\Program Files\\Amazon\\AWSCLIV2;C:\\Program Files\\Docker\\Docker\\resources\\bin;C:\\Program Files\\Git\\cmd;C:\\WINDOWS\\System32\\OpenSSH;${env.PATH}"
        JAVA_HOME = 'C:\\Program Files\\Java\\jdk-25.0.4'
        MAVEN_HOME = 'C:\\apache-maven-3.9.11'
        MAVEN_OPTS = '-Dmaven.repo.local=C:\\Users\\khushboo\\.m2\\repository'
        NPM_CONFIG_CACHE = 'C:\\Users\\khushboo\\AppData\\Local\\npm-cache'
        DOCKER_HOST = 'npipe:////./pipe/docker_engine'
        TF_IN_AUTOMATION = 'true'
        TF_PLUGIN_CACHE_DIR = 'C:\\ProgramData\\Jenkins\\.jenkins\\cache\\terraform-plugins'
        AWS_DEFAULT_REGION = 'ap-south-1'
        AWS_EC2_METADATA_DISABLED = 'true'
        PROJECT_NAME = 'medibook'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        MEDIBOOK_SERVER_IP = ''
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== Stage 1: Checkout Repository ==='
                checkout scm
                powershell(script: '''
                    Write-Host "Checked out branch: $env:GIT_BRANCH"
                    Write-Host "Commit SHA: $env:GIT_COMMIT"
                    git log -1 --oneline
                ''')
            }
        }

        stage('Environment Validation') {
            steps {
                echo '=== Stage 2: Environment and Tool Validation ==='
                powershell(script: '''
                    $ErrorActionPreference = 'Continue'
                    Write-Host 'Validating required build and deployment tools...'

                    $javaVersion = java -version 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'Java is missing or not configured in PATH.' }
                    Write-Host "Java: $($javaVersion | Select-Object -First 1)"

                    $mvnVersion = mvn.cmd -v 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'Maven is missing or not configured in PATH.' }
                    Write-Host "Maven: $($mvnVersion | Select-String 'Apache Maven' | Select-Object -First 1)"

                    $nodeVersion = node -v 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'Node.js is missing or not configured in PATH.' }
                    $npmVersion = npm.cmd -v 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'npm is missing or not configured in PATH.' }
                    Write-Host "Node: $nodeVersion | npm: $npmVersion"

                    $tfVersion = terraform -version 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'Terraform is missing or not configured in PATH.' }
                    Write-Host "Terraform: $($tfVersion | Select-Object -First 1)"

                    $awsVersion = aws --version 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'AWS CLI is missing or not configured in PATH.' }
                    Write-Host "AWS CLI: $awsVersion"

                    $gitVersion = git --version 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'Git is missing or not configured in PATH.' }
                    Write-Host "Git: $gitVersion"

                    $sshVersion = ssh -V 2>&1
                    if ($LASTEXITCODE -ne 0) { throw 'OpenSSH is missing or not configured in PATH.' }
                    Write-Host "SSH: $sshVersion"

                    docker info 2>&1 | Out-Null
                    if ($LASTEXITCODE -ne 0) { throw 'Docker daemon is not reachable from the Jenkins agent. Check DOCKER_HOST and Docker Desktop access.' }
                    Write-Host 'Docker daemon: reachable'
                ''')
            }
        }

        stage('Backend Build') {
            steps {
                echo '=== Stage 3: Backend Build (Maven) ==='
                dir('backend') {
                    powershell(script: '''
                        $ErrorActionPreference = 'Continue'
                        Write-Host 'Compiling and packaging the Spring Boot application...'
                        mvn.cmd -B clean package -DskipTests
                        if ($LASTEXITCODE -ne 0) { throw 'Backend build failed.' }
                    ''')
                }
            }
        }

        stage('Backend Testing') {
            steps {
                echo '=== Stage 4: Backend Automated Tests (JUnit) ==='
                dir('backend') {
                    powershell(script: '''
                        $ErrorActionPreference = 'Continue'
                        Write-Host 'Executing the backend unit test suite...'
                        mvn.cmd -B test
                        if ($LASTEXITCODE -ne 0) { throw 'Backend tests failed.' }
                    ''')
                }
            }
        }

        stage('Frontend Build') {
            steps {
                echo '=== Stage 5: Frontend Build (Vite/React) ==='
                dir('frontend') {
                    powershell(script: '''
                        $ErrorActionPreference = 'Continue'
                        Write-Host 'Installing frontend dependencies and creating the production bundle...'
                        if (Test-Path package-lock.json) {
                            npm.cmd ci --no-audit --no-fund
                        } else {
                            npm.cmd install --no-audit --no-fund
                        }
                        if ($LASTEXITCODE -ne 0) { throw 'Frontend dependency installation failed.' }
                        npm.cmd run build
                        if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed.' }
                    ''')
                }
            }
        }

        stage('Frontend Testing') {
            steps {
                echo '=== Stage 6: Frontend Automated Tests ==='
                dir('frontend') {
                    powershell(script: '''
                        $ErrorActionPreference = 'Continue'
                        Write-Host 'Executing the frontend unit tests...'
                        npm.cmd test
                        if ($LASTEXITCODE -ne 0) { throw 'Frontend tests failed.' }
                    ''')
                }
            }
        }

        stage('Docker Image Build') {
            steps {
                echo '=== Stage 7: Docker Container Images Build ==='
                powershell(script: '''
                    $ErrorActionPreference = 'Continue'
                    Write-Host 'Verifying Docker daemon access...'
                    docker info 2>&1 | Out-Null
                    if ($LASTEXITCODE -ne 0) { throw 'Docker daemon is not reachable. Refusing to report a false-successful image build.' }

                    Write-Host "Building backend image medibook-backend:$env:IMAGE_TAG ..."
                    docker build -t "medibook-backend:$env:IMAGE_TAG" -t 'medibook-backend:latest' ./backend
                    if ($LASTEXITCODE -ne 0) { throw 'Backend Docker image build failed.' }

                    Write-Host "Building frontend image medibook-frontend:$env:IMAGE_TAG ..."
                    docker build -t "medibook-frontend:$env:IMAGE_TAG" -t 'medibook-frontend:latest' ./frontend
                    if ($LASTEXITCODE -ne 0) { throw 'Frontend Docker image build failed.' }

                    Write-Host 'Built images:'
                    docker images 'medibook-*' --format 'table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.Size}}'
                ''')
            }
        }

        stage('Terraform Init & Validate') {
            steps {
                echo '=== Stage 8: Terraform Format, Init and Validate ==='
                dir('terraform') {
                    powershell(script: '''
                        $ErrorActionPreference = 'Continue'
                        Write-Host 'Checking Terraform formatting...'
                        terraform fmt -check -recursive
                        if ($LASTEXITCODE -ne 0) { throw 'Terraform formatting check failed. Run terraform fmt.' }

                        Write-Host 'Initializing Terraform providers...'
                        terraform init -input=false
                        if ($LASTEXITCODE -ne 0) { throw 'Terraform init failed.' }

                        Write-Host 'Validating Terraform configuration...'
                        terraform validate
                        if ($LASTEXITCODE -ne 0) { throw 'Terraform validate failed.' }
                    ''')
                }
            }
        }

        stage('Terraform Plan') {
            steps {
                echo '=== Stage 9: Terraform Plan (dry run, no infrastructure created) ==='
                withCredentials([
                    string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'medibook-ssh-cidr', variable: 'MEDIBOOK_SSH_CIDR')
                ]) {
                    dir('terraform') {
                        powershell(script: '''
                            $ErrorActionPreference = 'Continue'
                            if (-not $env:MEDIBOOK_SSH_CIDR) { throw 'medibook-ssh-cidr credential is empty.' }
                            $env:TF_VAR_trusted_ssh_cidr = "[`"$env:MEDIBOOK_SSH_CIDR`"]"

                            Write-Host 'Confirming AWS identity for the selected account...'
                            aws sts get-caller-identity
                            if ($LASTEXITCODE -ne 0) { throw 'AWS credentials are not valid.' }

                            Write-Host 'Generating the Terraform execution plan...'
                            terraform plan -out=tfplan -input=false
                            if ($LASTEXITCODE -ne 0) { throw 'Terraform plan failed.' }

                            terraform show -no-color tfplan | Out-File -FilePath terraform-plan.txt -Encoding UTF8
                            Write-Host 'Terraform plan saved to terraform-plan.txt'
                        ''')
                    }
                }
            }
        }

        stage('Approval Gate') {
            steps {
                echo '=== Stage 10: Infrastructure Provisioning Approval Gate ==='
                script {
                    input message: 'Terraform plan is ready. Approve provisioning and deployment of AWS infrastructure?',
                          ok: 'Approve and provision',
                          submitterParameter: 'APPROVER'
                }
            }
        }

        stage('Terraform Apply') {
            steps {
                echo '=== Stage 11: Terraform Apply (approved infrastructure provisioning) ==='
                withCredentials([
                    string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'medibook-ssh-cidr', variable: 'MEDIBOOK_SSH_CIDR')
                ]) {
                    dir('terraform') {
                        powershell(script: '''
                            $ErrorActionPreference = 'Continue'
                            if (-not $env:MEDIBOOK_SSH_CIDR) { throw 'medibook-ssh-cidr credential is empty.' }
                            $env:TF_VAR_trusted_ssh_cidr = "[`"$env:MEDIBOOK_SSH_CIDR`"]"

                            Write-Host 'Applying the reviewed Terraform plan...'
                            terraform apply -input=false tfplan
                            if ($LASTEXITCODE -ne 0) { throw 'Terraform apply failed.' }

                            $publicIp = (terraform output -raw public_ip).Trim()
                            if (-not $publicIp) { throw 'Terraform did not return a public IP.' }
                            Set-Content -Path public_ip.txt -Value $publicIp -Encoding ASCII
                            Write-Host "Provisioned MediBook EC2 public IP: $publicIp"
                        ''')
                    }
                }
                script {
                    // Read the persisted output at workspace scope. Assigning env inside
                    // withCredentials/dir did not survive into the deployment stage.
                    env.MEDIBOOK_SERVER_IP = readFile('terraform/public_ip.txt').trim()
                    if (!env.MEDIBOOK_SERVER_IP) {
                        error 'Terraform did not expose a public IP for deployment.'
                    }
                    echo "Deployment target: ${env.MEDIBOOK_SERVER_IP}"
                }
            }
        }

        stage('Application Deployment') {
            steps {
                echo '=== Stage 12: MediBook Application Deployment on AWS EC2 ==='
                withCredentials([
                    string(credentialsId: 'medibook-postgres-password', variable: 'MEDIBOOK_POSTGRES_PASSWORD'),
                    string(credentialsId: 'medibook-jwt-secret', variable: 'MEDIBOOK_JWT_SECRET')
                ]) {
                    powershell(script: '''
                        $ErrorActionPreference = 'Continue'
                        if (-not $env:MEDIBOOK_SERVER_IP -and (Test-Path 'terraform/public_ip.txt')) {
                            $env:MEDIBOOK_SERVER_IP = (Get-Content 'terraform/public_ip.txt' -Raw).Trim()
                        }
                        if (-not $env:MEDIBOOK_SERVER_IP) { throw 'MEDIBOOK_SERVER_IP is not set.' }
                        if (-not (Test-Path 'terraform/medibook-key.pem')) { throw 'Terraform SSH private key not found.' }

                        $envFile = Join-Path $env:TEMP "medibook-deploy-$env:BUILD_NUMBER.env"
                        try {
                            @(
                                'POSTGRES_DB=medibook',
                                'POSTGRES_USER=medibook',
                                "POSTGRES_PASSWORD=$env:MEDIBOOK_POSTGRES_PASSWORD",
                                "JWT_SECRET=$env:MEDIBOOK_JWT_SECRET",
                                'SPRING_PROFILES_ACTIVE=production',
                                "FRONTEND_ORIGIN=http://$env:MEDIBOOK_SERVER_IP",
                                'CANCELLATION_HOURS=24',
                                'TIMEZONE=Asia/Kolkata',
                                'EMAIL_ENABLED=false'
                            ) | Set-Content -Path $envFile -Encoding ASCII

                            powershell -ExecutionPolicy Bypass -File scripts/deploy-ec2.ps1 `
                                -ServerIp $env:MEDIBOOK_SERVER_IP `
                                -KeyPath terraform/medibook-key.pem `
                                -EnvFile $envFile
                            if ($LASTEXITCODE -ne 0) { throw 'EC2 deployment script failed.' }
                        } finally {
                            Remove-Item $envFile -Force -ErrorAction SilentlyContinue
                        }
                    ''')
                }
            }
        }

        stage('Post-Deployment Verification') {
            steps {
                echo '=== Stage 13: Application and API Health Verification ==='
                powershell(script: '''
                    $ErrorActionPreference = 'Continue'
                    if (-not $env:MEDIBOOK_SERVER_IP -and (Test-Path 'terraform/public_ip.txt')) {
                        $env:MEDIBOOK_SERVER_IP = (Get-Content 'terraform/public_ip.txt' -Raw).Trim()
                    }
                    if (-not $env:MEDIBOOK_SERVER_IP) { throw 'MEDIBOOK_SERVER_IP is not set.' }

                    Write-Host "Verifying frontend at http://$env:MEDIBOOK_SERVER_IP/ ..."
                    $frontendResponse = Invoke-WebRequest -Uri "http://$env:MEDIBOOK_SERVER_IP/" -UseBasicParsing -TimeoutSec 15
                    if ($frontendResponse.StatusCode -ne 200) { throw "Frontend returned HTTP $($frontendResponse.StatusCode)." }
                    Write-Host 'Frontend web UI is accessible (HTTP 200).'

                    Write-Host 'Verifying Spring Boot health endpoint...'
                    $health = Invoke-RestMethod -Uri "http://$env:MEDIBOOK_SERVER_IP/actuator/health" -TimeoutSec 15
                    if ($health.status -ne 'UP') { throw "Backend health is not UP: $($health | ConvertTo-Json -Compress)" }
                    Write-Host 'Spring Boot Actuator health status: UP.'

                    Write-Host 'Verifying a public API endpoint...'
                    $specialties = Invoke-WebRequest -Uri "http://$env:MEDIBOOK_SERVER_IP/api/specialties" -UseBasicParsing -TimeoutSec 15
                    if ($specialties.StatusCode -ne 200) { throw "API returned HTTP $($specialties.StatusCode)." }
                    Write-Host 'API communication is working (HTTP 200).'
                ''')
            }
        }
    }

    post {
        always {
            echo '=== Post Actions and Reporting ==='
            junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'backend/target/*.jar, frontend/dist/**, terraform/tfplan, terraform/terraform-plan.txt, terraform/public_ip.txt'
        }
        success {
            echo 'MediBook CI/CD pipeline completed successfully.'
        }
        failure {
            echo 'MediBook CI/CD pipeline failed. Review the console log for the failed stage.'
        }
        cleanup {
            powershell(script: '''
                Write-Host 'Build workspace retained so Terraform local state is not lost between builds.'
            ''')
        }
    }
}
