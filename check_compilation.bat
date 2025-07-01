@echo off
echo Checking for basic compilation issues...
echo.
echo Checking if Java is available:
java -version
echo.
echo Checking if Kotlin compiler is available:
kotlinc -version
echo.
echo Checking Gradle Wrapper:
dir gradlew.bat
echo.
echo Attempting to run gradle daemon status:
gradlew.bat --status
echo.
echo Done.
pause