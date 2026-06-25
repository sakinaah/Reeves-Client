@rem Gradle startup script for Windows.
@rem Do not edit manually.

@if "%DEBUG%"=="" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.

set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve JAVA_HOME
set JAVA_EXE=java.exe
if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Execute Gradle
"%JAVA_EXE%" -Xmx64m -Xms64m -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End of main script
if "%ERRORLEVEL%"=="0" goto mainEnd
exit /b %ERRORLEVEL%
:mainEnd
if "%OS%"=="Windows_NT" endlocal
