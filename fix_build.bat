@echo off
echo ========================================
echo Build Fix Script for Shuigongrizhi
echo ========================================
echo.

echo Step 1: Cleaning project...
call gradlew.bat clean
if %ERRORLEVEL% neq 0 (
    echo ERROR: Clean failed. Check your Gradle installation.
    pause
    exit /b 1
)

echo.
echo Step 2: Syncing Gradle...
call gradlew.bat --refresh-dependencies
if %ERRORLEVEL% neq 0 (
    echo WARNING: Dependency refresh failed, continuing...
)

echo.
echo Step 3: Building debug APK...
call gradlew.bat assembleDebug
if %ERRORLEVEL% neq 0 (
    echo ERROR: Build failed. Check the error messages above.
    echo.
    echo Common solutions:
    echo 1. Check internet connection for dependency downloads
    echo 2. Ensure Java 11+ is installed
    echo 3. Try running: gradlew.bat assembleDebug --info
    echo 4. Check KAPT_TO_KSP_MIGRATION.md for migration details
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo The APK has been generated in:
echo app\build\outputs\apk\debug\
echo.
pause