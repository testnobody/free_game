@rem Gradle startup script for Windows
@echo off
set APP_NAME=Gradle
set APP_BASE_NAME=%~n0

if not defined GRADLE_USER_HOME set GRADLE_USER_HOME=%USERPROFILE%\.gradle

set WRAPPER_PROPS=%~dp0gradle\wrapper\gradle-wrapper.properties
for /f "tokens=1,* delims==" %%a in ('findstr /b "distributionUrl=" "%WRAPPER_PROPS%"') do set DISTRIBUTION_URL=%%b
set DISTRIBUTION_URL=%DISTRIBUTION_URL:\:=%

for %%i in ("%DISTRIBUTION_URL%") do set DIST_ZIP=%%~nxi
set DIST_NAME=%DIST_ZIP:.zip=%
set DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\%DIST_NAME%

if not exist "%DIST_DIR%\unpacked" (
  echo Downloading %DISTRIBUTION_URL% ...
  mkdir "%DIST_DIR%" 2>nul
  powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%DIST_DIR%\gradle.zip'"
  mkdir "%DIST_DIR%\unpacked" 2>nul
  powershell -NoProfile -Command "Expand-Archive -Path '%DIST_DIR%\gradle.zip' -DestinationPath '%DIST_DIR%\unpacked' -Force"
)

for /d %%d in ("%DIST_DIR%\unpacked\gradle-*") do set GRADLE_HOME=%%d

if defined JAVA_HOME (
  set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
  set JAVACMD=java.exe
)

"%JAVACMD%" -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%GRADLE_HOME%\lib\gradle-launcher-*.jar" org.gradle.launcher.GradleMain %*
