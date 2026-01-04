@echo off
REM Build script for AethorNPCS
REM This script builds the entire project and shows the output

echo ========================================
echo Building AethorNPCS...
echo ========================================
echo.

REM Build the project
call mvn clean package -pl aethornpcs-plugin -am

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build Successful!
    echo ========================================
    echo.
    echo Output JAR:
    echo   aethornpcs-plugin\target\AethorNPCS-1.0.0-SNAPSHOT.jar
    echo.
    echo API JAR for quest plugins:
    echo   aethornpcs-api\target\aethornpcs-api-1.0.0-SNAPSHOT.jar
    echo.
    echo Install the plugin JAR to your Paper server's plugins folder.
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Build Failed!
    echo ========================================
    echo Check the error messages above.
    echo.
    echo Common issues:
    echo   - Java 21 not found
    echo   - MythicMobs/ModelEngine not in Maven repo
    echo   - Network connection issues
    echo.
)

pause
