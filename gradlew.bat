@rem Gradle startup script for Windows
@rem SPDX-License-Identifier: Apache-2.0

@if "%DEBUG%"=="" @echo off
set APP_BASE_NAME=%~n0
set APP_HOME=%~dp0

@rem Execute Gradle
"%JAVA_HOME%\bin\java.exe" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
exit /b 1

:mainEnd
exit /b 0
