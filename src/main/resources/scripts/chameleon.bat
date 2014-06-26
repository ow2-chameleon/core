@echo off

rem
rem #%L
rem OW2 Chameleon - Core
rem %%
rem Copyright (C) 2009 - 2014 OW2 Chameleon
rem %%
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem #L%
rem


if not "%ECHO%" == "" echo %ECHO%

setlocal
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%
set ARGS=%*

rem Sourcing environment settings similar to tomcats setenv
if exist "%DIRNAME%setenv.bat" (
  call "%DIRNAME%setenv.bat"
)

goto BEGIN

:warn
    echo %PROGNAME%: %*
goto :EOF

:BEGIN

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

set CHAMELEON_HOME=%DIRNAME%
if not exist "%CHAMELEON_HOME%" (
    call :warn CHAMELEON_HOME is not valid: "%CHAMELEON_HOME%"
    goto END
)

rem Setup the Java Virtual Machine
if not "%JAVA%" == "" goto :Check_JAVA_END
    set JAVA=java
    if "%JAVA_HOME%" == "" call :warn JAVA_HOME not set; results may vary
    if not "%JAVA_HOME%" == "" set JAVA=%JAVA_HOME%\bin\java
    if not exist "%JAVA_HOME%" (
        call :warn JAVA_HOME is not valid: "%JAVA_HOME%"
        goto END
    )
:Check_JAVA_END

set DEFAULT_JAVA_OPTS=
if "%JVM_ARGS%" == "" set JVM_ARGS=%DEFAULT_JAVA_OPTS%

for %%G in (%DIRNAME%bin\*.jar) do call:APPEND_TO_CLASSPATH %%G
goto CLASSPATH_END

: APPEND_TO_CLASSPATH
set filename=%~1
set CLASSPATH=%CLASSPATH%;%filename%
goto :EOF

:CLASSPATH_END

:EXECUTE
    if "%SHIFT%" == "true" SET ARGS=%2 %3 %4 %5 %6 %7 %8
    if not "%SHIFT%" == "true" SET ARGS=%1 %2 %3 %4 %5 %6 %7 %8
    rem Execute the Java Virtual Machine
    "%JAVA%" -cp "%CLASSPATH%" %JVM_ARGS%  -Dchameleon.home=%CHAMELEON_HOME% org.ow2.chameleon.core.Main %ARGS%

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END

endlocal
