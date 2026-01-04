# Simple Maven wrapper for PowerShell

Write-Host "AethorNPCS Build Script" -ForegroundColor Cyan
Write-Host "======================="
Write-Host ""

# Check if Java is available
$javaCheck = Get-Command java -ErrorAction SilentlyContinue
if ($javaCheck) {
    Write-Host "Java found at: $($javaCheck.Source)" -ForegroundColor Green
} else {
    Write-Host "Error: Java not found. Please install Java 21." -ForegroundColor Red
    exit 1
}

# Maven wrapper jar location
$wrapperJar = ".mvn\wrapper\maven-wrapper.jar"
$wrapperUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

# Download wrapper jar if not present
if (-not (Test-Path $wrapperJar)) {
    Write-Host "Downloading Maven wrapper..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path ".mvn\wrapper" | Out-Null
    try {
        Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar
        Write-Host "Maven wrapper downloaded successfully." -ForegroundColor Green
    } catch {
        Write-Host "Error downloading Maven wrapper: $_" -ForegroundColor Red
        exit 1
    }
}

# Run Maven
Write-Host ""
Write-Host "Building AethorNPCS..." -ForegroundColor Cyan
Write-Host ""

$mavenArgs = $args
if ($mavenArgs.Length -eq 0) {
    $mavenArgs = @("clean", "package", "-pl", "aethornpcs-plugin", "-am")
}

& java -classpath $wrapperJar "-Dmaven.multiModuleProjectDirectory=$PWD" org.apache.maven.wrapper.MavenWrapperMain $mavenArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Build Successful!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Output JAR:" -ForegroundColor Cyan
    Write-Host "  aethornpcs-plugin\target\AethorNPCS-1.0.0-SNAPSHOT.jar" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "Build Failed!" -ForegroundColor Red
    Write-Host ""
    exit $LASTEXITCODE
}
