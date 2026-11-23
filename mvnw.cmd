@echo off
setlocal
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot"
) else (
    rem If JAVA_HOME points to java8, override with JDK 21
    echo %JAVA_HOME% | findstr /i "1.8" >nul && set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot"
    echo %JAVA_HOME% | findstr /i "jre" >nul && set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot"
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

set "MAVEN_BIN=C:\tools\apache-maven-3.9.9\bin\mvn.cmd"
if not exist "%MAVEN_BIN%" (
    where mvn >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        set "MAVEN_BIN=mvn"
    ) else (
        echo [ERROR] Maven not found at %MAVEN_BIN% and not in PATH.
        exit /b 1
    )
)

call "%MAVEN_BIN%" %*
exit /b %ERRORLEVEL%
