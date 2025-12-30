@echo off
setlocal

set SCRIPT_DIR=%~dp0
set WRAPPER_DIR=%SCRIPT_DIR%.mvn\wrapper
set PROPS_FILE=%WRAPPER_DIR%\maven-wrapper.properties
set MAVEN_ZIP=%WRAPPER_DIR%\apache-maven.zip
set MAVEN_HOME=

if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"

if not exist "%PROPS_FILE%" (
  echo [ERROR] %PROPS_FILE% not found. Create it with distributionUrl property.
  exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%PROPS_FILE%") do (
  if /i "%%~A"=="distributionUrl" set DISTRIBUTION_URL=%%~B
)

if "%DISTRIBUTION_URL%"=="" (
  echo [ERROR] distributionUrl not defined in %PROPS_FILE%
  exit /b 1
)

for /d %%D in ("%WRAPPER_DIR%\apache-maven-*") do (
  set MAVEN_HOME=%%~fD
  goto :foundMaven
)

:downloadMaven
if not exist "%MAVEN_ZIP%" (
  powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_ZIP%'" || (
    echo [ERROR] Failed to download Maven from %DISTRIBUTION_URL%
    exit /b 1
  )
)

powershell -NoProfile -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%WRAPPER_DIR%' -Force" || (
  echo [ERROR] Failed to expand Maven archive
  exit /b 1
)

for /d %%D in ("%WRAPPER_DIR%\apache-maven-*") do (
  set MAVEN_HOME=%%~fD
  goto :foundMaven
)

echo [ERROR] Maven home not found after extraction
exit /b 1

:foundMaven
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  goto :downloadMaven
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%

